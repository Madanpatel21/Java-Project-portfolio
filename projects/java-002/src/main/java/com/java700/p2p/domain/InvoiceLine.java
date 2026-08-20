package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Invoice line item. */
@Entity
@Table(name = "invoice_lines")
public class InvoiceLine {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "invoice_id", nullable = false, length = 36)
    private String invoiceId;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    protected InvoiceLine() {
    }

    public InvoiceLine(String id, String invoiceId, String itemCode, BigDecimal quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = lineTotal;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
