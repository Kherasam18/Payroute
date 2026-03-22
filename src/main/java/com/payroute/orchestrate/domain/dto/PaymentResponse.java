package com.payroute.orchestrate.domain.dto;

import com.payroute.orchestrate.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID transactionId,
        TransactionStatus status,
        String providerName,
        String providerTransactionId,
        BigDecimal amount,
        String currency,
        LocalDateTime processedAt,
        String errorMessage
) {}
