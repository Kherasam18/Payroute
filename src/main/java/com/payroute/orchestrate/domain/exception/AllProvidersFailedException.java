package com.payroute.orchestrate.domain.exception;

/**
 * Thrown when all available payment providers have failed for a transaction.
 */
public class AllProvidersFailedException extends PayRouteException {

    public AllProvidersFailedException(String message, String errorCode) {
        super(message, errorCode);
    }

    public AllProvidersFailedException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
