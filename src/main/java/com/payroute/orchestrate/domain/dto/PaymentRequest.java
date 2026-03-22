package com.payroute.orchestrate.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Idempotency key is required")
        UUID idempotencyKey,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        String currency,

        String description,

        Map<String, String> metadata
) {}
