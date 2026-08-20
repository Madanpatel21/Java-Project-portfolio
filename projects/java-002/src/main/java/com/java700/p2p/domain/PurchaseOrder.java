package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

/** Purchase order header. */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    public enum Status {
        OPEN, CLOSED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "po_number", nullable = false, unique = true, length = 40)
    private String poNumber;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Column(name = "supplier_name", nullable = false, length = 160)
    private String supplierName;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected PurchaseOrder() {
    }

    public PurchaseOrder(String id, String poNumber, String supplierId, String supplierName,
                         String currency, Instant issuedAt, Instant createdAt) {
        this.id = id;
        this.poNumber = poNumber;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.currency = currency;
        this.status = Status.OPEN.name();
        this.issuedAt = issuedAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getPoNumber() {
        return poNumber;
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

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }
}
