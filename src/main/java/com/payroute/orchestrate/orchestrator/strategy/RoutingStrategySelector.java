package com.payroute.orchestrate.orchestrator.strategy;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.entity.PaymentProvider;

import java.util.List;

/**
 * Strategy interface for selecting a payment provider based on the
 * incoming payment request and available providers.
 *
 * <p>Implementations should correspond to the {@link com.payroute.orchestrate.domain.enums.RoutingStrategy}
 * enum values: PRIORITY, COST_OPTIMIZED, SUCCESS_RATE.</p>
 */
public interface RoutingStrategySelector {

    /**
     * Selects the most appropriate payment provider for the given request.
     *
     * @param request   the incoming payment request
     * @param providers the list of active, healthy payment providers
     * @return the selected payment provider
     */
    PaymentProvider selectProvider(PaymentRequest request, List<PaymentProvider> providers);
}
