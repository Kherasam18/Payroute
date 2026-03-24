package com.payroute.orchestrate.orchestrator.strategy;

import com.payroute.orchestrate.domain.enums.RoutingStrategy;
import com.payroute.orchestrate.domain.exception.PayRouteException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves the correct {@link RoutingStrategySelector} by enum value.
 */
@Slf4j
@Component
public class RoutingStrategyFactory {

    private final Map<RoutingStrategy, RoutingStrategySelector> strategyMap;

    public RoutingStrategyFactory(
            PriorityRoutingStrategy priorityRoutingStrategy,
            CostOptimizedRoutingStrategy costOptimizedRoutingStrategy,
            SuccessRateRoutingStrategy successRateRoutingStrategy) {

        this.strategyMap = new EnumMap<>(RoutingStrategy.class);
        strategyMap.put(RoutingStrategy.PRIORITY, priorityRoutingStrategy);
        strategyMap.put(RoutingStrategy.COST_OPTIMIZED, costOptimizedRoutingStrategy);
        strategyMap.put(RoutingStrategy.SUCCESS_RATE, successRateRoutingStrategy);
    }

    public RoutingStrategySelector getStrategy(RoutingStrategy strategy) {
        RoutingStrategySelector selector = strategyMap.get(strategy);
        if (selector == null) {
            throw new PayRouteException(
                    "Unknown routing strategy: " + strategy,
                    "INVALID_ROUTING_STRATEGY"
            );
        }
        log.debug("Resolved routing strategy: {}", strategy);
        return selector;
    }
}
