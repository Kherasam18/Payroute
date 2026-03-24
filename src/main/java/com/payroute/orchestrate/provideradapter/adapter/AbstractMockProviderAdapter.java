package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.enums.ProviderName;
import com.payroute.orchestrate.domain.enums.TransactionStatus;
import com.payroute.orchestrate.domain.exception.ProviderException;
import com.payroute.orchestrate.provideradapter.model.ProviderResponse;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Base class for mock payment provider adapters.
 *
 * <p>Simulates real-world provider behaviour using configurable success/failure
 * rates and random outcomes. No real HTTP calls are made.</p>
 */
@Slf4j
public abstract class AbstractMockProviderAdapter implements PaymentProviderAdapter {

    private static final List<String> ERROR_CODES = List.of(
            "INSUFFICIENT_FUNDS", "CARD_DECLINED", "TIMEOUT", "GATEWAY_ERROR"
    );

    protected final ProviderName providerName;
    protected final double successRate;
    protected final Random random = new Random();

    protected AbstractMockProviderAdapter(ProviderName providerName, double successRate) {
        this.providerName = providerName;
        this.successRate = successRate;
    }

    @Override
    public PaymentResponse charge(PaymentRequest request) {
        log.info("Charging {} {} via {}", request.amount(), request.currency(), providerName);

        // Simulate network latency (50–200ms)
        try {
            Thread.sleep(random.nextInt(150) + 50);
        } catch (InterruptedException e) {
            log.warn("Sleep interrupted during {} charge simulation", providerName);
            Thread.currentThread().interrupt();
        }

        // Simulate success/failure based on successRate
        if (random.nextDouble() < successRate) {
            String providerTransactionId = UUID.randomUUID().toString();
            log.info("Success: txn {}", providerTransactionId);

            ProviderResponse providerResponse = new ProviderResponse(
                    true,
                    providerTransactionId,
                    providerName.name(),
                    null,
                    null,
                    LocalDateTime.now()
            );

            return toPaymentResponse(request, providerResponse, UUID.randomUUID());
        } else {
            String errorCode = ERROR_CODES.get(random.nextInt(ERROR_CODES.size()));
            log.info("Failed: {}", errorCode);

            throw new ProviderException(
                    providerName + " declined transaction: " + errorCode,
                    errorCode
            );
        }
    }

    @Override
    public boolean isHealthy() {
        return random.nextDouble() < 0.95;
    }

    @Override
    public ProviderName getProviderName() {
        return this.providerName;
    }

    /**
     * Maps a raw ProviderResponse to the unified PaymentResponse DTO.
     */
    protected PaymentResponse toPaymentResponse(PaymentRequest request,
                                                 ProviderResponse providerResponse,
                                                 UUID transactionId) {
        return new PaymentResponse(
                transactionId,
                providerResponse.success() ? TransactionStatus.SUCCESS : TransactionStatus.FAILED,
                providerResponse.providerName(),
                providerResponse.providerTransactionId(),
                request.amount(),
                request.currency(),
                providerResponse.respondedAt(),
                providerResponse.errorMessage()
        );
    }
}
