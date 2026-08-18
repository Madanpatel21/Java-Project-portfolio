package com.java700.workforce.common.audit;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
}
