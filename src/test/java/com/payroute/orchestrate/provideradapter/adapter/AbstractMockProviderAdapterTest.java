package com.payroute.orchestrate.provideradapter.adapter;

import com.payroute.orchestrate.domain.dto.PaymentRequest;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.enums.ProviderName;
import com.payroute.orchestrate.domain.enums.TransactionStatus;
import com.payroute.orchestrate.domain.exception.ProviderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AbstractMockProviderAdapter behaviour using StripeAdapter
 * as the concrete instance under test.
 */
class AbstractMockProviderAdapterTest {

    private StripeAdapter adapter;

    private PaymentRequest sampleRequest() {
        return new PaymentRequest(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                "USD",
                "Test payment",
                Map.of("key", "value")
        );
    }

    @BeforeEach
    void setUp() {
        adapter = new StripeAdapter();
    }

    @Test
    void charge_returnsSuccessResponse_whenProviderSucceeds() {
        // Force 100% success rate
        ReflectionTestUtils.setField(adapter, "successRate", 1.0);

        PaymentResponse response = adapter.charge(sampleRequest());

        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS, response.status());
        assertEquals("STRIPE", response.providerName());
        assertNotNull(response.providerTransactionId());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals("USD", response.currency());
        assertNotNull(response.processedAt());
        assertNull(response.errorMessage());
    }

    @Test
    void charge_throwsProviderException_whenProviderFails() {
        // Force 0% success rate
        ReflectionTestUtils.setField(adapter, "successRate", 0.0);

        ProviderException exception = assertThrows(
                ProviderException.class,
                () -> adapter.charge(sampleRequest())
        );

        assertTrue(exception.getMessage().contains("STRIPE declined transaction"));
    }

    @Test
    void charge_statisticalSuccessRate_over100Iterations() {
        // Use default 97% success rate
        int successes = 0;
        for (int i = 0; i < 100; i++) {
            try {
                PaymentResponse response = adapter.charge(sampleRequest());
                if (response.status() == TransactionStatus.SUCCESS) {
                    successes++;
                }
            } catch (ProviderException ignored) {
                // Expected for some iterations
            }
        }
        // At 97% success rate, at least 85 out of 100 should succeed
        assertTrue(successes >= 85,
                "Expected at least 85 successes but got " + successes);
    }

    @Test
    void isHealthy_returnsBooleanWithoutThrowing() {
        assertDoesNotThrow(() -> adapter.isHealthy());
    }

    @Test
    void getProviderName_returnsStripe() {
        assertEquals(ProviderName.STRIPE, adapter.getProviderName());
    }

    @Test
    void charge_withNullRequest_throwsException() {
        ReflectionTestUtils.setField(adapter, "successRate", 1.0);
        assertThrows(NullPointerException.class, () -> adapter.charge(null));
    }
}
