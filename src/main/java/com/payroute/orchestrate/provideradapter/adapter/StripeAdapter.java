package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock adapter for Stripe with 97% simulated success rate.
 */
@Slf4j
@Component
public class StripeAdapter extends AbstractMockProviderAdapter {

    public StripeAdapter() {
        super(ProviderName.STRIPE, 0.97);
    }
}
