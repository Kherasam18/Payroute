package com.payroute.orchestrate.domain.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp,
        Map<String, String> details
) {}
