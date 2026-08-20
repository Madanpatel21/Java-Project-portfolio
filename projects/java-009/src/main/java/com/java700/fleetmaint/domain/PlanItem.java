package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** PlanItem. */
@Entity
@Table(name = "plan_items")
public class PlanItem {


    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "plan_id", length = 36, nullable = false)
    private String planId;
    @Column(name = "part_code", length = 40, nullable = false)
    private String partCode;
    @Column(name = "part_name", length = 120, nullable = false)
    private String partName;
    @Column(nullable = false)
    private int quantity;
    @Column(name = "estimated_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    protected PlanItem() {
    }

    public PlanItem(String id, String planId, String partCode, String partName, int quantity, BigDecimal estimatedCost) {
        this.id = id;
        this.planId = planId;
        this.partCode = partCode;
        this.partName = partName;
        this.quantity = quantity;
        this.estimatedCost = estimatedCost;

    }

    public String getId() {
        return id;
    }

    public String getPlanId() {
        return planId;
    }
    public String getPartCode() {
        return partCode;
    }
    public String getPartName() {
        return partName;
    }
    public int getQuantity() {
        return quantity;
    }
    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }
}
