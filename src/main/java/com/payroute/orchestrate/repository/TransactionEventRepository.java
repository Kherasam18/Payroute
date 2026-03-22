package com.payroute.orchestrate.repository;

import com.payroute.orchestrate.domain.entity.TransactionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionEventRepository extends JpaRepository<TransactionEvent, UUID> {
    List<TransactionEvent> findByTransactionId(UUID transactionId);
    List<TransactionEvent> findByTransactionIdOrderByCreatedAtAsc(UUID transactionId);
}
