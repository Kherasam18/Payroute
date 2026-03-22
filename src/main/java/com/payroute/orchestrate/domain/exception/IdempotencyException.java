package com.payroute.orchestrate.domain.exception;

/**
 * Thrown when a duplicate payment request is detected via idempotency key.
 */
public class IdempotencyException extends PayRouteException {

    public IdempotencyException(String message, String errorCode) {
        super(message, errorCode);
    }

    public IdempotencyException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
