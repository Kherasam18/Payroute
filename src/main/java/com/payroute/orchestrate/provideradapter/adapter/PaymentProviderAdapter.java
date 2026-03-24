package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.enums.ProviderName;

/**
 * Adapter interface that each payment provider must implement.
 *
 * <p>Each adapter translates the internal PaymentRequest model into the
 * provider-specific API contract and returns a unified PaymentResponse.</p>
 */
public interface PaymentProviderAdapter {

    /**
     * Submits a charge request to the payment provider.
     *
     * @param request the unified payment request
     * @return the provider's response mapped to the unified response model
     */
    PaymentResponse charge(PaymentRequest request);

    /**
     * Checks whether this provider adapter is currently healthy and available
     * to process transactions.
     *
     * @return {@code true} if the provider is reachable and operational
     */
    boolean isHealthy();

    /**
     * Returns the provider name this adapter is responsible for.
     * Overridden by concrete adapters to identify themselves.
     *
     * @return the {@link ProviderName}, or {@code null} if not set
     */
    default ProviderName getProviderName() {
        return null;
    }
}

