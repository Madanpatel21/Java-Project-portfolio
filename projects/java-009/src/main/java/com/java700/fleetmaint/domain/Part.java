package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Part. */
@Entity
@Table(name = "parts")
public class Part {

    public void restock(int quantity) {
        this.quantityOnHand += quantity;
    }

    public void issue(int quantity) {
        this.quantityOnHand -= quantity;
        this.reservedQty = Math.max(0, this.reservedQty - quantity);
    }

    public void reserve(int quantity) {
        this.reservedQty += quantity;
    }

    public void release(int quantity) {
        this.reservedQty = Math.max(0, this.reservedQty - quantity);
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "part_code", length = 40, nullable = false, unique = true)
    private String partCode;
    @Column(length = 120, nullable = false)
    private String name;
    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;
    @Column(name = "reserved_qty", nullable = false)
    private int reservedQty;
    @Column(name = "reorder_point", nullable = false)
    private int reorderPoint;
    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    protected Part() {
    }

    public Part(String id, String partCode, String name, int quantityOnHand, int reservedQty, int reorderPoint, BigDecimal unitCost) {
        this.id = id;
        this.partCode = partCode;
        this.name = name;
        this.quantityOnHand = quantityOnHand;
        this.reservedQty = reservedQty;
        this.reorderPoint = reorderPoint;
        this.unitCost = unitCost;

    }

    public String getId() {
        return id;
    }

    public String getPartCode() {
        return partCode;
    }
    public String getName() {
        return name;
    }
    public int getQuantityOnHand() {
        return quantityOnHand;
    }
    public int getReservedQty() {
        return reservedQty;
    }
    public int getReorderPoint() {
        return reorderPoint;
    }
    public BigDecimal getUnitCost() {
        return unitCost;
    }
}
