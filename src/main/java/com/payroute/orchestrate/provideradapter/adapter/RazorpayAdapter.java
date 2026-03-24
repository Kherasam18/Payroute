package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock adapter for Razorpay with 94% simulated success rate.
 */
@Slf4j
@Component
public class RazorpayAdapter extends AbstractMockProviderAdapter {

    public RazorpayAdapter() {
        super(ProviderName.RAZORPAY, 0.94);
    }
}
