package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Goods receipt header. */
@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "gr_number", nullable = false, unique = true, length = 40)
    private String grNumber;

    @Column(name = "po_id", nullable = false, length = 36)
    private String poId;

    @Column(name = "supplier_id", nullable = false, length = 36)
    private String supplierId;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    protected GoodsReceipt() {
    }

    public GoodsReceipt(String id, String grNumber, String poId, String supplierId,
                        Instant receivedAt) {
        this.id = id;
        this.grNumber = grNumber;
        this.poId = poId;
        this.supplierId = supplierId;
        this.receivedAt = receivedAt;
        this.status = "POSTED";
    }

    public String getId() {
        return id;
    }

    public String getGrNumber() {
        return grNumber;
    }

    public String getPoId() {
        return poId;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }
}
