package com.payroute.orchestrate.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Binds {@code payroute.idempotency.*} properties from application.yml.
 */
@Slf4j
@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "payroute.idempotency")
public class IdempotencyProperties {

    @Min(1)
    private long ttlHours;

    @PostConstruct
    void logConfig() {
        log.info("Idempotency TTL configured: {} hours", ttlHours);
    }
}
