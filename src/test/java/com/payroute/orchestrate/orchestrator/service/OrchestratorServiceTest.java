package com.payroute.orchestrate.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payroute.orchestrate.apigateway.filter.IdempotencyFilter;
import com.payroute.orchestrate.common.idempotency.IdempotencyService;
import com.payroute.orchestrate.config.RetryProperties;
import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.entity.PaymentProvider;
import com.payroute.orchestrate.domain.entity.Transaction;
import com.payroute.orchestrate.domain.enums.ProviderName;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrchestratorService.
 */
@ExtendWith(MockitoExtension.class)
class OrchestratorServiceTest {

    @Mock private ProviderAdapterRegistry adapterRegistry;
    @Mock private IdempotencyService idempotencyService;
    @Mock private IdempotencyFilter idempotencyFilter;
    @Mock private RoutingStrategyFactory routingStrategyFactory;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionEventRepository transactionEventRepository;
    @Mock private PaymentProviderRepository paymentProviderRepository;
    @Mock private PaymentEventPublisher eventPublisher;
    @Mock private RetryProperties retryProperties;
    @Mock private RoutingStrategySelector routingStrategySelector;
    @Mock private PaymentProviderAdapter providerAdapter;

    private OrchestratorService orchestratorService;

    private PaymentProvider provider1;
    private PaymentProvider provider2;

    private PaymentRequest sampleRequest() {
        return new PaymentRequest(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "USD",
                "Test payment",
                Map.of()
        );
    }

    private PaymentResponse sampleResponse() {
        return new PaymentResponse(
                UUID.randomUUID(),
                TransactionStatus.SUCCESS,
                "STRIPE",
                UUID.randomUUID().toString(),
                new BigDecimal("100.00"),
                "USD",
                LocalDateTime.now(),
                null
        );
    }

    @BeforeEach
    void setUp() {
        orchestratorService = new OrchestratorService(
                adapterRegistry,
                idempotencyService,
                idempotencyFilter,
                routingStrategyFactory,
                transactionRepository,
                transactionEventRepository,
                paymentProviderRepository,
                eventPublisher,
                retryProperties,
                new ObjectMapper(),
                "PRIORITY"
        );

        provider1 = PaymentProvider.builder()
                .id(UUID.randomUUID())
                .name("STRIPE")
                .priority(1)
                .successRate(new BigDecimal("97.00"))
                .isActive(true)
                .build();

        provider2 = PaymentProvider.builder()
                .id(UUID.randomUUID())
                .name("RAZORPAY")
                .priority(2)
                .successRate(new BigDecimal("94.00"))
                .isActive(true)
                .build();
    }

    @Test
    void processPayment_returnsCachedResponse_onIdempotencyHit() {
        PaymentResponse cached = sampleResponse();
        PaymentRequest request = sampleRequest();

        when(idempotencyService.getIfPresent(request.idempotencyKey().toString()))
                .thenReturn(Optional.of(cached));

        PaymentResponse result = orchestratorService.processPayment(request);

        assertEquals(cached, result);
        verify(providerAdapter, never()).charge(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_throwsIdempotencyException_onLockContention() {
        PaymentRequest request = sampleRequest();

        when(idempotencyService.getIfPresent(anyString())).thenReturn(Optional.empty());
        when(idempotencyService.acquireLock(anyString())).thenReturn(false);

        IdempotencyException exception = assertThrows(
                IdempotencyException.class,
                () -> orchestratorService.processPayment(request)
        );
        assertTrue(exception.getMessage().contains("Concurrent request"));
    }

    @Test
    void processPayment_succeeds_onFirstAttempt() {
        PaymentRequest request = sampleRequest();
        PaymentResponse expected = sampleResponse();

        // Common stubs
        when(idempotencyService.getIfPresent(anyString())).thenReturn(Optional.empty());
        when(idempotencyService.acquireLock(anyString())).thenReturn(true);
        when(retryProperties.getMaxAttempts()).thenReturn(3);
        lenient().when(retryProperties.getDelayMs()).thenReturn(0L);
        when(paymentProviderRepository.findByIsActiveTrueOrderByPriorityAsc())
                .thenReturn(List.of(provider1, provider2));
        when(routingStrategyFactory.getStrategy(any())).thenReturn(routingStrategySelector);
        when(routingStrategySelector.selectProvider(any(), any())).thenReturn(provider1);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    if (t.getId() == null) t.setId(UUID.randomUUID());
                    return t;
                });
        when(adapterRegistry.getAdapter(ProviderName.STRIPE)).thenReturn(providerAdapter);
        when(providerAdapter.charge(request)).thenReturn(expected);

        PaymentResponse result = orchestratorService.processPayment(request);

        assertEquals(expected, result);
        verify(transactionRepository, atLeast(2)).save(any(Transaction.class));
        verify(idempotencyService).store(eq(request.idempotencyKey().toString()), eq(expected));
        verify(eventPublisher).publishPaymentSuccess(any());
    }

    @Test
    void processPayment_fallsBackToSecondProvider_onFirstFailure() {
        PaymentRequest request = sampleRequest();
        PaymentResponse expected = sampleResponse();

        when(idempotencyService.getIfPresent(anyString())).thenReturn(Optional.empty());
        when(idempotencyService.acquireLock(anyString())).thenReturn(true);
        when(retryProperties.getMaxAttempts()).thenReturn(3);
        when(retryProperties.getDelayMs()).thenReturn(0L);
        when(paymentProviderRepository.findByIsActiveTrueOrderByPriorityAsc())
                .thenReturn(new ArrayList<>(List.of(provider1, provider2)));
        when(routingStrategyFactory.getStrategy(any())).thenReturn(routingStrategySelector);

        // First call selects provider1, second call selects provider2
        when(routingStrategySelector.selectProvider(any(), any()))
                .thenReturn(provider1)
                .thenReturn(provider2);

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    if (t.getId() == null) t.setId(UUID.randomUUID());
                    return t;
                });

        // First adapter fails, second succeeds
        PaymentProviderAdapter failingAdapter = mock(PaymentProviderAdapter.class);
        PaymentProviderAdapter succeedingAdapter = mock(PaymentProviderAdapter.class);
        when(failingAdapter.charge(request))
                .thenThrow(new ProviderException("STRIPE declined", "DECLINED"));
        when(succeedingAdapter.charge(request)).thenReturn(expected);

        when(adapterRegistry.getAdapter(ProviderName.STRIPE)).thenReturn(failingAdapter);
        when(adapterRegistry.getAdapter(ProviderName.RAZORPAY)).thenReturn(succeedingAdapter);

        PaymentResponse result = orchestratorService.processPayment(request);

        assertEquals(expected, result);
        verify(eventPublisher).publishPaymentFailed(any(), anyString());
        verify(eventPublisher).publishPaymentSuccess(any());
    }

    @Test
    void processPayment_throwsAllProvidersFailedException_whenAllFail() {
        PaymentRequest request = sampleRequest();

        when(idempotencyService.getIfPresent(anyString())).thenReturn(Optional.empty());
        when(idempotencyService.acquireLock(anyString())).thenReturn(true);
        when(retryProperties.getMaxAttempts()).thenReturn(3);
        lenient().when(retryProperties.getDelayMs()).thenReturn(0L);
        when(paymentProviderRepository.findByIsActiveTrueOrderByPriorityAsc())
                .thenReturn(new ArrayList<>(List.of(provider1)));
        when(routingStrategyFactory.getStrategy(any())).thenReturn(routingStrategySelector);
        when(routingStrategySelector.selectProvider(any(), any())).thenReturn(provider1);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> {
                    Transaction t = invocation.getArgument(0);
                    if (t.getId() == null) t.setId(UUID.randomUUID());
                    return t;
                });
        when(adapterRegistry.getAdapter(ProviderName.STRIPE)).thenReturn(providerAdapter);
        when(providerAdapter.charge(request))
                .thenThrow(new ProviderException("STRIPE declined", "DECLINED"));

        assertThrows(
                AllProvidersFailedException.class,
                () -> orchestratorService.processPayment(request)
        );
        verify(idempotencyService).releaseLock(request.idempotencyKey().toString());
    }

    @Test
    void processPayment_throwsAllProvidersFailedException_whenNoProviders() {
        PaymentRequest request = sampleRequest();

        when(idempotencyService.getIfPresent(anyString())).thenReturn(Optional.empty());
        when(idempotencyService.acquireLock(anyString())).thenReturn(true);
        when(paymentProviderRepository.findByIsActiveTrueOrderByPriorityAsc())
                .thenReturn(List.of());

        AllProvidersFailedException exception = assertThrows(
                AllProvidersFailedException.class,
                () -> orchestratorService.processPayment(request)
        );
        assertTrue(exception.getMessage().contains("No active payment providers"));
    }
}
