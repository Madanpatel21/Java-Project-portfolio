package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Purchase order line item. */
@Entity
@Table(name = "purchase_order_lines")
public class PurchaseOrderLine {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "po_id", nullable = false, length = 36)
    private String poId;

    @Column(name = "line_no", nullable = false)
    private int lineNo;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "received_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal receivedQty;

    @Column(name = "invoiced_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal invoicedQty;

    protected PurchaseOrderLine() {
    }

    public PurchaseOrderLine(String id, String poId, int lineNo, String itemCode,
                             String description, BigDecimal quantity, BigDecimal unitPrice) {
        this.id = id;
        this.poId = poId;
        this.lineNo = lineNo;
        this.itemCode = itemCode;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.receivedQty = BigDecimal.ZERO;
        this.invoicedQty = BigDecimal.ZERO;
    }

    public String getId() {
        return id;
    }

    public String getPoId() {
        return poId;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getReceivedQty() {
        return receivedQty;
    }

    public BigDecimal getInvoicedQty() {
        return invoicedQty;
    }

    public void creditReceipt(BigDecimal qty) {
        this.receivedQty = this.receivedQty.add(qty);
    }

    public void creditInvoice(BigDecimal qty) {
        this.invoicedQty = this.invoicedQty.add(qty);
    }
}
