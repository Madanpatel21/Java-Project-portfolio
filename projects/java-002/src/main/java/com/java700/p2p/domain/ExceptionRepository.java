package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExceptionRepository extends JpaRepository<MatchException, String> {
    List<MatchException> findByInvoiceIdOrderByCreatedAtAsc(String invoiceId);

        List<MatchException> findByInvoiceIdAndExceptionType(String invoiceId, String type);

        Page<MatchException> findByStatus(String status, Pageable pageable);

        long countByStatus(String status);
}
