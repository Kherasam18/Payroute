package com.payroute.orchestrate.provideradapter;

import com.payroute.orchestrate.domain.enums.ProviderName;
import com.payroute.orchestrate.domain.exception.ProviderException;
import com.payroute.orchestrate.provideradapter.adapter.PaymentProviderAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Central registry for all payment provider adapters.
 *
 * <p>Provides lookup-by-name so the orchestrator can retrieve the correct
 * adapter without hardcoding if/else chains.</p>
 */
@Slf4j
@Component
public class ProviderAdapterRegistry {

    private final Map<ProviderName, PaymentProviderAdapter> adapterMap;

    public ProviderAdapterRegistry(List<PaymentProviderAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .filter(a -> a.getProviderName() != null)
                .collect(Collectors.toMap(
                        PaymentProviderAdapter::getProviderName,
                        Function.identity()
                ));
        log.info("Registered {} provider adapters: {}", adapterMap.size(), adapterMap.keySet());
    }

    /**
     * Retrieves the adapter for the given provider name.
     *
     * @throws ProviderException if no adapter is registered for the name
     */
    public PaymentProviderAdapter getAdapter(ProviderName name) {
        PaymentProviderAdapter adapter = adapterMap.get(name);
        if (adapter == null) {
            throw new ProviderException(
                    "No adapter registered for provider: " + name,
                    "ADAPTER_NOT_FOUND"
            );
        }
        return adapter;
    }

    /**
     * Returns all adapters that are currently reporting healthy.
     */
    public List<PaymentProviderAdapter> getHealthyAdapters() {
        return adapterMap.values().stream()
                .filter(PaymentProviderAdapter::isHealthy)
                .collect(Collectors.toList());
    }

    /**
     * Returns the health status of every registered adapter (for monitoring).
     */
    public Map<ProviderName, Boolean> getHealthStatus() {
        return adapterMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().isHealthy()
                ));
    }
}
