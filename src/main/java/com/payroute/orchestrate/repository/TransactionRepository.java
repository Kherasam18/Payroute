package com.payroute.orchestrate.repository;

import com.payroute.orchestrate.domain.entity.Transaction;
import com.payroute.orchestrate.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    List<Transaction> findByStatus(TransactionStatus status);
}
