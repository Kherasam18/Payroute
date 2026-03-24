package com.payroute.orchestrate.apigateway.filter;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.exception.IdempotencyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Validates the idempotency key present in incoming payment requests.
 *
 * <p>This is a helper component called by the API Gateway controller
 * (not a servlet filter). Full filter-chain integration happens in
 * Phase 4 with the API Gateway.</p>
 */
@Slf4j
@Component
public class IdempotencyFilter {

    /**
     * Validates that the idempotency key is present and is a valid UUID.
     *
     * @param request the incoming payment request
     * @throws IdempotencyException if the key is null or not a valid UUID
     */
    public void validateIdempotencyKey(PaymentRequest request) {
        if (request.idempotencyKey() == null) {
            throw new IdempotencyException(
                    "Idempotency key must not be null",
                    "MISSING_IDEMPOTENCY_KEY"
            );
        }

        try {
            // Validate UUID format by attempting to parse
            java.util.UUID.fromString(request.idempotencyKey().toString());
        } catch (IllegalArgumentException e) {
            throw new IdempotencyException(
                    "Idempotency key must be a valid UUID",
                    "INVALID_IDEMPOTENCY_KEY"
            );
        }

        log.debug("Idempotency key validated: {}", request.idempotencyKey());
    }
}
