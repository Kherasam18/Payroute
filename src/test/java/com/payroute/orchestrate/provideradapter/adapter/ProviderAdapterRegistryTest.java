package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import com.payroute.orchestrate.domain.exception.ProviderException;
import com.payroute.orchestrate.provideradapter.ProviderAdapterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProviderAdapterRegistry.
 */
class ProviderAdapterRegistryTest {

    private ProviderAdapterRegistry registry;

    @BeforeEach
    void setUp() {
        List<PaymentProviderAdapter> adapters = List.of(
                new StripeAdapter(),
                new RazorpayAdapter(),
                new PayPalAdapter(),
                new BraintreeAdapter(),
                new SquareAdapter()
        );
        registry = new ProviderAdapterRegistry(adapters);
    }

    @Test
    void registry_containsExactly5Adapters() {
        Map<ProviderName, Boolean> status = registry.getHealthStatus();
        assertEquals(5, status.size());
    }

    @Test
    void getAdapter_returnsStripeAdapter() {
        PaymentProviderAdapter adapter = registry.getAdapter(ProviderName.STRIPE);
        assertNotNull(adapter);
        assertInstanceOf(StripeAdapter.class, adapter);
    }

    @Test
    void getAdapter_returnsCorrectAdapterForEachProvider() {
        assertInstanceOf(RazorpayAdapter.class, registry.getAdapter(ProviderName.RAZORPAY));
        assertInstanceOf(PayPalAdapter.class, registry.getAdapter(ProviderName.PAYPAL));
        assertInstanceOf(BraintreeAdapter.class, registry.getAdapter(ProviderName.BRAINTREE));
        assertInstanceOf(SquareAdapter.class, registry.getAdapter(ProviderName.SQUARE));
    }

    @Test
    void getAdapter_throwsProviderException_forUnknownName() {
        // Remove all adapters, create registry with empty list
        ProviderAdapterRegistry emptyRegistry = new ProviderAdapterRegistry(List.of());

        assertThrows(ProviderException.class,
                () -> emptyRegistry.getAdapter(ProviderName.STRIPE));
    }

    @Test
    void getHealthStatus_returnsMapWith5Entries() {
        Map<ProviderName, Boolean> status = registry.getHealthStatus();
        assertEquals(5, status.size());
        assertTrue(status.containsKey(ProviderName.STRIPE));
        assertTrue(status.containsKey(ProviderName.RAZORPAY));
        assertTrue(status.containsKey(ProviderName.PAYPAL));
        assertTrue(status.containsKey(ProviderName.BRAINTREE));
        assertTrue(status.containsKey(ProviderName.SQUARE));
    }

    @Test
    void getHealthyAdapters_returnsNonEmptyList() {
        // With 95% health probability per adapter, at least some should be healthy
        List<PaymentProviderAdapter> healthy = registry.getHealthyAdapters();
        assertNotNull(healthy);
        // Statistically very unlikely all 5 are unhealthy at once
        assertFalse(healthy.isEmpty(), "Expected at least one healthy adapter");
    }
}
