package com.java700.p2p.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceLineRepository extends JpaRepository<InvoiceLine, String> {
    List<InvoiceLine> findByInvoiceId(String invoiceId);
}
