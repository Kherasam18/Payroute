package com.payroute.orchestrate.repository;

import com.payroute.orchestrate.domain.entity.PaymentProvider;
import com.payroute.orchestrate.domain.enums.ProviderName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentProviderRepository extends JpaRepository<PaymentProvider, UUID> {
    List<PaymentProvider> findByIsActiveTrueOrderByPriorityAsc();
    Optional<PaymentProvider> findByName(String name); // Note: DB schema stores name as String
}
