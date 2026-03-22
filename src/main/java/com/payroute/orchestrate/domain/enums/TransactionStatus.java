package com.payroute.orchestrate.domain.enums;

/**
 * Represents the lifecycle status of a payment transaction.
 */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    RETRYING
}
