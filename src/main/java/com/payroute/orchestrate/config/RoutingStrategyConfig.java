package com.payroute.orchestrate.config;

import com.payroute.orchestrate.domain.enums.RoutingStrategy;
import com.payroute.orchestrate.domain.exception.PayRouteException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validates the default routing strategy at startup.
 */
@Slf4j
@Configuration
public class RoutingStrategyConfig {

    @Value("${payroute.routing.default-strategy}")
    private String defaultStrategy;

    @PostConstruct
    void validateStrategy() {
        try {
            RoutingStrategy.valueOf(defaultStrategy);
            log.info("Default routing strategy: {}", defaultStrategy);
        } catch (IllegalArgumentException e) {
            throw new PayRouteException(
                    "Invalid default routing strategy: " + defaultStrategy +
                            ". Valid values: PRIORITY, COST_OPTIMIZED, SUCCESS_RATE",
                    "INVALID_CONFIG"
            );
        }
    }
}
