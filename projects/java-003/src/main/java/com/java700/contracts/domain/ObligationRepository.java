package com.java700.contracts.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObligationRepository extends JpaRepository<Obligation, String> {
    List<Obligation> findByContractIdOrderByDueAtAsc(String contractId);

        List<Obligation> findByStatus(String status);

        List<Obligation> findByStatusInAndDueAtBefore(List<String> statuses, Instant at);

        Page<Obligation> findByStatus(String status, Pageable pageable);

        long countByStatus(String status);
}
