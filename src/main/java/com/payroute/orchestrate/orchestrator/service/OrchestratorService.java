package com.payroute.orchestrate.orchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payroute.orchestrate.apigateway.filter.IdempotencyFilter;
import com.payroute.orchestrate.common.idempotency.IdempotencyService;
import com.payroute.orchestrate.config.RetryProperties;
import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.entity.PaymentProvider;
import com.payroute.orchestrate.domain.entity.Transaction;
import com.payroute.orchestrate.domain.entity.TransactionEvent;
import com.payroute.orchestrate.domain.enums.EventType;
import com.payroute.orchestrate.domain.enums.ProviderName;
import com.payroute.orchestrate.domain.enums.RoutingStrategy;
import com.payroute.orchestrate.domain.enums.TransactionStatus;
import com.payroute.orchestrate.domain.exception.AllProvidersFailedException;
import com.payroute.orchestrate.domain.exception.IdempotencyException;
import com.payroute.orchestrate.domain.exception.ProviderException;
import com.payroute.orchestrate.eventprocessor.producer.PaymentEventPublisher;
import com.payroute.orchestrate.orchestrator.strategy.RoutingStrategyFactory;
import com.payroute.orchestrate.orchestrator.strategy.RoutingStrategySelector;
import com.payroute.orchestrate.provideradapter.ProviderAdapterRegistry;
import com.payroute.orchestrate.provideradapter.adapter.PaymentProviderAdapter;
import com.payroute.orchestrate.repository.PaymentProviderRepository;
import com.payroute.orchestrate.repository.TransactionEventRepository;
import com.payroute.orchestrate.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core payment orchestration service.
 *
 * <p>Wires together idempotency, routing, provider adapters, persistence,
 * and event publishing into a single end-to-end payment flow with
 * retry and fallback support.</p>
 *
 * <p>Only loaded when {@link IdempotencyService} is available (requires Redis).</p>
 */
@Slf4j
@Service
@Transactional
@ConditionalOnBean(IdempotencyService.class)
public class OrchestratorService {

    private final ProviderAdapterRegistry adapterRegistry;
    private final IdempotencyService idempotencyService;
    private final IdempotencyFilter idempotencyFilter;
    private final RoutingStrategyFactory routingStrategyFactory;
    private final TransactionRepository transactionRepository;
    private final TransactionEventRepository transactionEventRepository;
    private final PaymentProviderRepository paymentProviderRepository;
    private final PaymentEventPublisher eventPublisher;
    private final RetryProperties retryProperties;
    private final ObjectMapper objectMapper;
    private final String defaultStrategyValue;

    public OrchestratorService(
            ProviderAdapterRegistry adapterRegistry,
            IdempotencyService idempotencyService,
            IdempotencyFilter idempotencyFilter,
            RoutingStrategyFactory routingStrategyFactory,
            TransactionRepository transactionRepository,
            TransactionEventRepository transactionEventRepository,
            PaymentProviderRepository paymentProviderRepository,
            PaymentEventPublisher eventPublisher,
            RetryProperties retryProperties,
            ObjectMapper objectMapper,
            @Value("${payroute.routing.default-strategy}") String defaultStrategyValue) {
        this.adapterRegistry = adapterRegistry;
        this.idempotencyService = idempotencyService;
        this.idempotencyFilter = idempotencyFilter;
        this.routingStrategyFactory = routingStrategyFactory;
        this.transactionRepository = transactionRepository;
        this.transactionEventRepository = transactionEventRepository;
        this.paymentProviderRepository = paymentProviderRepository;
        this.eventPublisher = eventPublisher;
        this.retryProperties = retryProperties;
        this.objectMapper = objectMapper;
        this.defaultStrategyValue = defaultStrategyValue;
    }

    /**
     * Processes a payment request through the full orchestration pipeline:
     * validate → idempotency check → lock → route → charge → persist → cache → release.
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        // A. Validate idempotency key format
        idempotencyFilter.validateIdempotencyKey(request);
        String idempotencyKey = request.idempotencyKey().toString();

        // B. Check idempotency cache
        Optional<PaymentResponse> cached = idempotencyService.getIfPresent(idempotencyKey);
        if (cached.isPresent()) {
            log.info("Returning cached result for idempotencyKey: {}", idempotencyKey);
            return cached.get();
        }

        // C. Acquire distributed lock
        boolean locked = idempotencyService.acquireLock(idempotencyKey);
        if (!locked) {
            throw new IdempotencyException(
                    "Concurrent request detected for idempotencyKey: " + idempotencyKey +
                            ". Please retry in a moment.",
                    "LOCK_CONTENTION"
            );
        }

        try {
            // D. Re-check cache after acquiring lock
            cached = idempotencyService.getIfPresent(idempotencyKey);
            if (cached.isPresent()) {
                log.info("Returning cached result after lock for idempotencyKey: {}", idempotencyKey);
                return cached.get();
            }

            // E. Load active providers from DB
            List<PaymentProvider> activeProviders =
                    paymentProviderRepository.findByIsActiveTrueOrderByPriorityAsc();
            if (activeProviders.isEmpty()) {
                throw new AllProvidersFailedException(
                        "No active payment providers configured",
                        "NO_PROVIDERS"
                );
            }

            // F. Resolve routing strategy
            RoutingStrategy strategy = RoutingStrategy.valueOf(defaultStrategyValue);
            RoutingStrategySelector selector = routingStrategyFactory.getStrategy(strategy);

            // G. Create initial Transaction record
            Transaction transaction = Transaction.builder()
                    .amount(request.amount())
                    .currency(request.currency())
                    .status(TransactionStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .requestPayload(serialize(request))
                    .build();
            transaction = transactionRepository.save(transaction);

            eventPublisher.publishPaymentInitiated(transaction);

            // H. Attempt payment with retry + fallback loop
            return executeWithRetry(request, transaction, selector, activeProviders, idempotencyKey);

        } finally {
            // Lock MUST be released in all exit paths
            idempotencyService.releaseLock(idempotencyKey);
        }
    }

    private PaymentResponse executeWithRetry(
            PaymentRequest request,
            Transaction transaction,
            RoutingStrategySelector selector,
            List<PaymentProvider> activeProviders,
            String idempotencyKey) {

        int attempt = 0;
        List<PaymentProvider> remainingProviders = new ArrayList<>(activeProviders);
        Exception lastException = null;

        while (attempt < retryProperties.getMaxAttempts() && !remainingProviders.isEmpty()) {
            attempt++;

            // Select provider using strategy
            PaymentProvider selectedProvider = selector.selectProvider(request, remainingProviders);
            String providerName = selectedProvider.getName();

            log.info("Attempt {}/{} via {}", attempt, retryProperties.getMaxAttempts(), providerName);

            // Save attempt event
            saveTransactionEvent(transaction, EventType.PAYMENT_INITIATED,
                    "Attempt " + attempt + " via " + providerName);

            try {
                // Update transaction status to PROCESSING
                transaction.setStatus(TransactionStatus.PROCESSING);
                transaction.setProvider(selectedProvider);
                transactionRepository.save(transaction);

                // Call adapter
                PaymentProviderAdapter adapter =
                        adapterRegistry.getAdapter(ProviderName.valueOf(providerName));
                PaymentResponse response = adapter.charge(request);

                // SUCCESS path
                transaction.setStatus(TransactionStatus.SUCCESS);
                transaction.setResponsePayload(serialize(response));
                transactionRepository.save(transaction);

                saveTransactionEvent(transaction, EventType.PAYMENT_SUCCESS,
                        "Payment succeeded via " + providerName);

                eventPublisher.publishPaymentSuccess(transaction);

                // Store in idempotency cache
                idempotencyService.store(idempotencyKey, response);

                return response;

            } catch (ProviderException e) {
                lastException = e;
                log.warn("Attempt {} failed via {}: {}", attempt, providerName, e.getMessage());

                // Update transaction status
                transaction.setStatus(TransactionStatus.RETRYING);
                transactionRepository.save(transaction);

                saveTransactionEvent(transaction, EventType.PAYMENT_FAILED,
                        "Attempt " + attempt + " failed: " + e.getMessage());

                eventPublisher.publishPaymentFailed(transaction, e.getMessage());

                // Remove failed provider from remaining list
                remainingProviders.remove(selectedProvider);

                // Apply retry delay if more attempts remain
                if (attempt < retryProperties.getMaxAttempts() && !remainingProviders.isEmpty()) {
                    eventPublisher.publishPaymentRetry(transaction, attempt);
                    applyRetryDelay();
                }
            }
        }

        // ALL providers failed
        transaction.setStatus(TransactionStatus.FAILED);
        transactionRepository.save(transaction);

        saveTransactionEvent(transaction, EventType.PAYMENT_FAILED,
                "All providers exhausted after " + attempt + " attempts");

        throw new AllProvidersFailedException(
                "Payment failed after " + attempt + " attempt(s). Last error: " +
                        (lastException != null ? lastException.getMessage() : "unknown"),
                "ALL_PROVIDERS_FAILED"
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTransactionEvent(Transaction transaction, EventType eventType, String payload) {
        TransactionEvent event = TransactionEvent.builder()
                .transaction(transaction)
                .eventType(eventType)
                .payload(payload)
                .build();
        transactionEventRepository.save(event);
    }

    private String serialize(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize object: {}", e.getMessage());
            return obj.toString();
        }
    }

    private void applyRetryDelay() {
        if (retryProperties.getDelayMs() > 0) {
            try {
                Thread.sleep(retryProperties.getDelayMs());
            } catch (InterruptedException e) {
                log.warn("Retry delay interrupted");
                Thread.currentThread().interrupt();
            }
        }
    }
}
