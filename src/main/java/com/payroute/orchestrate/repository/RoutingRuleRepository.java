package com.payroute.orchestrate.repository;

import com.payroute.orchestrate.domain.entity.RoutingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RoutingRuleRepository extends JpaRepository<RoutingRule, UUID> {
    List<RoutingRule> findByIsActiveTrueOrderByPriorityAsc();
    List<RoutingRule> findByCurrencyAndIsActiveTrue(String currency);
}
