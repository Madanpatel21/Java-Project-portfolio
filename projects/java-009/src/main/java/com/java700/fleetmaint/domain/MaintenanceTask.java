package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** MaintenanceTask. */
@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask {

    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_DUE = "DUE";
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String PRIORITY_SAFETY = "SAFETY";
    public static final String PRIORITY_COMPLIANCE = "COMPLIANCE";
    public static final String PRIORITY_ROUTINE = "ROUTINE";

    public void assignWorkOrder(String newWorkOrderId) {
        this.workOrderId = newWorkOrderId;
        this.status = STATUS_IN_PROGRESS;
    }

    public void complete(Instant at) {
        this.status = STATUS_COMPLETED;
        this.completedAt = at;
    }

    public void markDue(String newStatus) {
        this.status = newStatus;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "task_no", length = 32, nullable = false, unique = true)
    private String taskNo;
    @Column(name = "vehicle_id", length = 36, nullable = false)
    private String vehicleId;
    @Column(name = "plan_id", length = 36, nullable = false)
    private String planId;
    @Column(name = "due_type", length = 16, nullable = false)
    private String dueType;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "due_odometer")
    private Integer dueOdometer;
    @Column(length = 24, nullable = false)
    private String status;
    @Column(length = 24, nullable = false)
    private String priority;
    @Column(name = "forecast_at")
    private Instant forecastAt;
    @Column(name = "work_order_id", length = 36)
    private String workOrderId;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MaintenanceTask() {
    }

    public MaintenanceTask(String id, String taskNo, String vehicleId, String planId, String dueType, LocalDate dueDate, Integer dueOdometer, String status, String priority, Instant forecastAt, String workOrderId, Instant completedAt, Instant createdAt) {
        this.id = id;
        this.taskNo = taskNo;
        this.vehicleId = vehicleId;
        this.planId = planId;
        this.dueType = dueType;
        this.dueDate = dueDate;
        this.dueOdometer = dueOdometer;
        this.status = status;
        this.priority = priority;
        this.forecastAt = forecastAt;
        this.workOrderId = workOrderId;
        this.completedAt = completedAt;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getTaskNo() {
        return taskNo;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public String getPlanId() {
        return planId;
    }
    public String getDueType() {
        return dueType;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public Integer getDueOdometer() {
        return dueOdometer;
    }
    public String getStatus() {
        return status;
    }
    public String getPriority() {
        return priority;
    }
    public Instant getForecastAt() {
        return forecastAt;
    }
    public String getWorkOrderId() {
        return workOrderId;
    }
    public Instant getCompletedAt() {
        return completedAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
