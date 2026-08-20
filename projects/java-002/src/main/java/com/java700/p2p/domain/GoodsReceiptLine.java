package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Goods receipt line: received quantity against a PO line. */
@Entity
@Table(name = "goods_receipt_lines")
public class GoodsReceiptLine {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "gr_id", nullable = false, length = 36)
    private String grId;

    @Column(name = "po_line_id", nullable = false, length = 36)
    private String poLineId;

    @Column(name = "quantity_received", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityReceived;

    protected GoodsReceiptLine() {
    }

    public GoodsReceiptLine(String id, String grId, String poLineId, BigDecimal quantityReceived) {
        this.id = id;
        this.grId = grId;
        this.poLineId = poLineId;
        this.quantityReceived = quantityReceived;
    }

    public String getPoLineId() {
        return poLineId;
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }
}
