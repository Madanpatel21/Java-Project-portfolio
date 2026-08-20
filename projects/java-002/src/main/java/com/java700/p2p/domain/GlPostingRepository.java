package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlPostingRepository extends JpaRepository<GlPosting, String> {
    List<GlPosting> findByInvoiceId(String invoiceId);
}
