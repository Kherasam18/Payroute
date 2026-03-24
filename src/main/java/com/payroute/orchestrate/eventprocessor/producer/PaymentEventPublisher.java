package com.payroute.orchestrate.eventprocessor.producer;

import com.payroute.orchestrate.domain.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub Kafka event publisher. Logs events instead of publishing to Kafka.
 * Real Kafka integration happens in Phase 5.
 */
@Slf4j
@Component
public class PaymentEventPublisher {

    public void publishPaymentInitiated(Transaction transaction) {
        log.info("[KAFKA-STUB] Event: PAYMENT_INITIATED | txnId: {} | provider: {} | status: {}",
                transaction.getId(),
                transaction.getProvider() != null ? transaction.getProvider().getName() : "N/A",
                transaction.getStatus());
    }

    public void publishPaymentSuccess(Transaction transaction) {
        log.info("[KAFKA-STUB] Event: PAYMENT_SUCCESS | txnId: {} | provider: {} | status: {}",
                transaction.getId(),
                transaction.getProvider() != null ? transaction.getProvider().getName() : "N/A",
                transaction.getStatus());
    }

    public void publishPaymentFailed(Transaction transaction, String reason) {
        log.info("[KAFKA-STUB] Event: PAYMENT_FAILED | txnId: {} | provider: {} | status: {} | reason: {}",
                transaction.getId(),
                transaction.getProvider() != null ? transaction.getProvider().getName() : "N/A",
                transaction.getStatus(),
                reason);
    }

    public void publishPaymentRetry(Transaction transaction, int attemptNumber) {
        log.info("[KAFKA-STUB] Event: PAYMENT_RETRY | txnId: {} | provider: {} | attempt: {}",
                transaction.getId(),
                transaction.getProvider() != null ? transaction.getProvider().getName() : "N/A",
                attemptNumber);
    }
}
