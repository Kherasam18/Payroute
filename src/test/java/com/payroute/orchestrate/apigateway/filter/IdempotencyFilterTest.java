package com.payroute.orchestrate.apigateway.filter;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.exception.IdempotencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdempotencyFilter.
 */
class IdempotencyFilterTest {

    private IdempotencyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IdempotencyFilter();
    }

    @Test
    void validateIdempotencyKey_passes_forValidUUID() {
        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "USD",
                "Test",
                Map.of()
        );

        assertDoesNotThrow(() -> filter.validateIdempotencyKey(request));
    }

    @Test
    void validateIdempotencyKey_throws_forNullKey() {
        PaymentRequest request = new PaymentRequest(
                null,
                new BigDecimal("50.00"),
                "USD",
                "Test",
                Map.of()
        );

        IdempotencyException exception = assertThrows(
                IdempotencyException.class,
                () -> filter.validateIdempotencyKey(request)
        );

        assertTrue(exception.getMessage().contains("must not be null"));
    }
}
