package com.payroute.orchestrate.orchestrator.strategy;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.entity.PaymentProvider;
import com.payroute.orchestrate.domain.exception.AllProvidersFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Selects the provider with the highest success rate.
 */
@Slf4j
@Component
public class SuccessRateRoutingStrategy implements RoutingStrategySelector {

    @Override
    public PaymentProvider selectProvider(PaymentRequest request, List<PaymentProvider> providers) {
        PaymentProvider selected = providers.stream()
                .filter(PaymentProvider::getIsActive)
                .max(Comparator.comparing(PaymentProvider::getSuccessRate))
                .orElseThrow(() -> new AllProvidersFailedException(
                        "No active providers available", "NO_ACTIVE_PROVIDERS"));

        log.info("Success-rate strategy selected: {} (successRate={})",
                selected.getName(), selected.getSuccessRate());
        return selected;
    }
}
