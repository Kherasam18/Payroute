package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock adapter for Braintree with 88% simulated success rate.
 */
@Slf4j
@Component
public class BraintreeAdapter extends AbstractMockProviderAdapter {

    public BraintreeAdapter() {
        super(ProviderName.BRAINTREE, 0.88);
    }
}
