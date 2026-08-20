package com.java700.p2p.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {
    Page<Invoice> findByStatus(String status, Pageable pageable);

        List<Invoice> findByStatus(String status);

        Optional<Invoice> findByInvoiceNumberAndSupplierId(String invoiceNumber, String supplierId);

        List<Invoice> findBySupplierId(String supplierId);
}
