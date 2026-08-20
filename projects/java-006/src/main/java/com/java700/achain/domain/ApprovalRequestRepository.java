package com.java700.achain.domain;

import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, String> {
    Page<ApprovalRequest> findByStatus(String status, Pageable pageable);

        List<ApprovalRequest> findByStatusAndDueAtBefore(String status, Instant at);

        long countByStatus(String status);
}
