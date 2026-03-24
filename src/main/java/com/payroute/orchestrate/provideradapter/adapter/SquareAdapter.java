package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Mock adapter for Square with 85% simulated success rate.
 */
@Slf4j
@Component
public class SquareAdapter extends AbstractMockProviderAdapter {

    public SquareAdapter() {
        super(ProviderName.SQUARE, 0.85);
    }
}
