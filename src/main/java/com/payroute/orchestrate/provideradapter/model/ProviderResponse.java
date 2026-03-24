package com.payroute.orchestrate.provideradapter.model;

import java.time.LocalDateTime;

/**
 * Raw response from a provider adapter before mapping to PaymentResponse.
 */
public record ProviderResponse(
        boolean success,
        String providerTransactionId,
        String providerName,
        String errorCode,
        String errorMessage,
        LocalDateTime respondedAt
) {}
