package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock adapter for PayPal with 91% simulated success rate.
 */
@Slf4j
@Component
public class PayPalAdapter extends AbstractMockProviderAdapter {

    public PayPalAdapter() {
        super(ProviderName.PAYPAL, 0.91);
    }
}
