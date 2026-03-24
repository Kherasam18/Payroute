package com.payroute.orchestrate.orchestrator.strategy;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.entity.PaymentProvider;
import com.payroute.orchestrate.domain.exception.AllProvidersFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PriorityRoutingStrategy.
 */
class PriorityRoutingStrategyTest {

    private PriorityRoutingStrategy strategy;

    private PaymentRequest sampleRequest() {
        return new PaymentRequest(
                UUID.randomUUID(),
                new BigDecimal("50.00"),
                "USD",
                "Test",
                Map.of()
        );
    }

    @BeforeEach
    void setUp() {
        strategy = new PriorityRoutingStrategy();
    }

    @Test
    void selectProvider_returnsLowestPriorityNumber() {
        PaymentProvider p1 = PaymentProvider.builder()
                .name("B").priority(2).isActive(true)
                .successRate(new BigDecimal("90.00")).build();
        PaymentProvider p2 = PaymentProvider.builder()
                .name("A").priority(1).isActive(true)
                .successRate(new BigDecimal("95.00")).build();
        PaymentProvider p3 = PaymentProvider.builder()
                .name("C").priority(3).isActive(true)
                .successRate(new BigDecimal("85.00")).build();

        PaymentProvider selected = strategy.selectProvider(sampleRequest(), List.of(p1, p2, p3));

        assertEquals("A", selected.getName());
        assertEquals(1, selected.getPriority());
    }

    @Test
    void selectProvider_throwsWhenNoActiveProviders() {
        PaymentProvider inactive = PaymentProvider.builder()
                .name("A").priority(1).isActive(false)
                .successRate(new BigDecimal("95.00")).build();

        assertThrows(
                AllProvidersFailedException.class,
                () -> strategy.selectProvider(sampleRequest(), List.of(inactive))
        );
    }
}
