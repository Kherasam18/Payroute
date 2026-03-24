package com.payroute.orchestrate.orchestrator.strategy;

import com.payroute.orchestrate.domain.enums.RoutingStrategy;
import com.payroute.orchestrate.domain.exception.PayRouteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoutingStrategyFactory.
 */
@ExtendWith(MockitoExtension.class)
class RoutingStrategyFactoryTest {

    @Mock private PriorityRoutingStrategy priorityRoutingStrategy;
    @Mock private CostOptimizedRoutingStrategy costOptimizedRoutingStrategy;
    @Mock private SuccessRateRoutingStrategy successRateRoutingStrategy;

    private RoutingStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RoutingStrategyFactory(
                priorityRoutingStrategy,
                costOptimizedRoutingStrategy,
                successRateRoutingStrategy
        );
    }

    @Test
    void getStrategy_returnsPriorityStrategy() {
        RoutingStrategySelector result = factory.getStrategy(RoutingStrategy.PRIORITY);
        assertSame(priorityRoutingStrategy, result);
    }

    @Test
    void getStrategy_returnsCostOptimizedStrategy() {
        RoutingStrategySelector result = factory.getStrategy(RoutingStrategy.COST_OPTIMIZED);
        assertSame(costOptimizedRoutingStrategy, result);
    }

    @Test
    void getStrategy_returnsSuccessRateStrategy() {
        RoutingStrategySelector result = factory.getStrategy(RoutingStrategy.SUCCESS_RATE);
        assertSame(successRateRoutingStrategy, result);
    }

    @Test
    void getStrategy_throwsForNullStrategy() {
        assertThrows(
                PayRouteException.class,
                () -> factory.getStrategy(null)
        );
    }
}
