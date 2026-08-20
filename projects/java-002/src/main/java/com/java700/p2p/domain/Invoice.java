package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Supplier invoice header with the reconciliation lifecycle. */
@Entity
@Table(name = "invoices")
public class Invoice {

    public enum Status {
        NEW, MATCHED, EXCEPTION, APPROVED, POSTED, REJECTED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "invoice_number", nullable = false, length = 40)
    private String invoiceNumber;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Column(name = "supplier_name", nullable = false, length = 160)
    private String supplierName;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Invoice() {
    }

    public Invoice(String id, String invoiceNumber, String supplierId, String supplierName,
                   String currency, BigDecimal totalAmount, LocalDate invoiceDate,
                   LocalDate dueDate, Instant createdAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.status = Status.NEW.name();
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markMatched() {
        this.status = Status.MATCHED.name();
    }

    public void markException() {
        this.status = Status.EXCEPTION.name();
    }

    public void markApproved() {
        this.status = Status.APPROVED.name();
    }

    public void markPosted() {
        this.status = Status.POSTED.name();
    }

    public void markRejected() {
        this.status = Status.REJECTED.name();
    }
}
