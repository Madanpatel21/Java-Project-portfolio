package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** WorkOrder. */
@Entity
@Table(name = "work_orders")
public class WorkOrder {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_PARTS_HOLD = "PARTS_HOLD";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_REJECTED = "REJECTED";

    public void transition(String newStatus) {
        this.status = newStatus;
    }

    public void holdForParts(String reason) {
        this.status = STATUS_PARTS_HOLD;
        this.shortfallReason = reason;
    }

    public void complete(String newMechanic, BigDecimal newLaborHours, BigDecimal newLaborCost,
                         BigDecimal newPartsCost, Integer newOdometer, Instant at) {
        this.status = STATUS_COMPLETED;
        this.mechanic = newMechanic;
        this.laborHours = newLaborHours;
        this.laborCost = newLaborCost;
        this.partsCost = newPartsCost;
        this.odometerAtService = newOdometer;
        this.completedAt = at;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "wo_no", length = 32, nullable = false, unique = true)
    private String woNo;
    @Column(name = "task_id", length = 36, nullable = false)
    private String taskId;
    @Column(name = "vehicle_id", length = 36, nullable = false)
    private String vehicleId;
    @Column(length = 24, nullable = false)
    private String status;
    @Column(name = "opened_by", length = 64, nullable = false)
    private String openedBy;
    @Column(length = 120)
    private String mechanic;
    @Column(name = "labor_hours", precision = 6, scale = 2)
    private BigDecimal laborHours;
    @Column(name = "labor_cost", precision = 12, scale = 2)
    private BigDecimal laborCost;
    @Column(name = "parts_cost", precision = 12, scale = 2)
    private BigDecimal partsCost;
    @Column(length = 1000)
    private String notes;
    @Column(name = "shortfall_reason", length = 500)
    private String shortfallReason;
    @Column(name = "odometer_at_service")
    private Integer odometerAtService;
    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    protected WorkOrder() {
    }

    public WorkOrder(String id, String woNo, String taskId, String vehicleId, String status, String openedBy, String mechanic, BigDecimal laborHours, BigDecimal laborCost, BigDecimal partsCost, String notes, String shortfallReason, Integer odometerAtService, Instant openedAt, Instant completedAt) {
        this.id = id;
        this.woNo = woNo;
        this.taskId = taskId;
        this.vehicleId = vehicleId;
        this.status = status;
        this.openedBy = openedBy;
        this.mechanic = mechanic;
        this.laborHours = laborHours;
        this.laborCost = laborCost;
        this.partsCost = partsCost;
        this.notes = notes;
        this.shortfallReason = shortfallReason;
        this.odometerAtService = odometerAtService;
        this.openedAt = openedAt;
        this.completedAt = completedAt;

    }

    public String getId() {
        return id;
    }

    public String getWoNo() {
        return woNo;
    }
    public String getTaskId() {
        return taskId;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public String getStatus() {
        return status;
    }
    public String getOpenedBy() {
        return openedBy;
    }
    public String getMechanic() {
        return mechanic;
    }
    public BigDecimal getLaborHours() {
        return laborHours;
    }
    public BigDecimal getLaborCost() {
        return laborCost;
    }
    public BigDecimal getPartsCost() {
        return partsCost;
    }
    public String getNotes() {
        return notes;
    }
    public String getShortfallReason() {
        return shortfallReason;
    }
    public Integer getOdometerAtService() {
        return odometerAtService;
    }
    public Instant getOpenedAt() {
        return openedAt;
    }
    public Instant getCompletedAt() {
        return completedAt;
    }
}
