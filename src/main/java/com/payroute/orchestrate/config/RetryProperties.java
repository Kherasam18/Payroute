package com.payroute.orchestrate.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds {@code payroute.retry.*} properties from application.yml.
 */
@Slf4j
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "payroute.retry")
public class RetryProperties {

    @Min(1)
    @Max(10)
    private int maxAttempts;

    @Min(0)
    private long delayMs;

    @PostConstruct
    void logConfig() {
        log.info("Retry policy configured: maxAttempts={}, delayMs={}ms", maxAttempts, delayMs);
    }
}
