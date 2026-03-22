package com.payroute.orchestrate.domain.enums;

/**
 * Kafka event types emitted during the payment lifecycle.
 */
public enum EventType {
    PAYMENT_INITIATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    PAYMENT_RETRY
}
