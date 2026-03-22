package com.payroute.orchestrate.domain.exception;

import com.payroute.orchestrate.domain.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for all PayRoute REST endpoints.
 * Translates domain exceptions into structured HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IdempotencyException.class)
    public ResponseEntity<ApiErrorResponse> handleIdempotencyException(IdempotencyException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(ProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleProviderException(ProviderException ex) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(AllProvidersFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAllProvidersFailedException(AllProvidersFailedException ex) {
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(PayRouteException.class)
    public ResponseEntity<ApiErrorResponse> handlePayRouteException(PayRouteException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", ex.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(
            HttpStatus status, String errorCode, String message) {
        ApiErrorResponse body = new ApiErrorResponse(
                errorCode,
                message,
                LocalDateTime.now(),
                null
        );
        return ResponseEntity.status(status).body(body);
    }
}
