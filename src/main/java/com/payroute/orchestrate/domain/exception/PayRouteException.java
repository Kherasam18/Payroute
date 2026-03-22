package com.payroute.orchestrate.domain.exception;

import lombok.Getter;

/**
 * Base runtime exception for all PayRoute business errors.
 * Carries an error code alongside the standard message.
 */
@Getter
public class PayRouteException extends RuntimeException {

    private final String errorCode;

    public PayRouteException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public PayRouteException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
