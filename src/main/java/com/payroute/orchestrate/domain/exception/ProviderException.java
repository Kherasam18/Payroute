package com.payroute.orchestrate.domain.exception;

/**
 * Thrown when a payment provider encounters an error during charge processing.
 */
public class ProviderException extends PayRouteException {

    public ProviderException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ProviderException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
