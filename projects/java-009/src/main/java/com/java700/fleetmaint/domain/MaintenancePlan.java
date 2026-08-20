package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** MaintenancePlan. */
@Entity
@Table(name = "maintenance_plans")
public class MaintenancePlan {

    public static final String INTERVAL_ODOMETER = "ODOMETER";
    public static final String INTERVAL_CALENDAR = "CALENDAR";

    public void setActive(boolean newActive) {
        this.active = newActive;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 40, nullable = false, unique = true)
    private String code;
    @Column(length = 120, nullable = false)
    private String name;
    @Column(name = "applies_to_category", length = 24, nullable = false)
    private String appliesToCategory;
    @Column(name = "interval_type", length = 24, nullable = false)
    private String intervalType;
    @Column(name = "interval_value", nullable = false)
    private int intervalValue;
    @Column(name = "compliance_required", nullable = false)
    private boolean complianceRequired;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MaintenancePlan() {
    }

    public MaintenancePlan(String id, String code, String name, String appliesToCategory, String intervalType, int intervalValue, boolean complianceRequired, boolean active, Instant createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.appliesToCategory = appliesToCategory;
        this.intervalType = intervalType;
        this.intervalValue = intervalValue;
        this.complianceRequired = complianceRequired;
        this.active = active;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getAppliesToCategory() {
        return appliesToCategory;
    }
    public String getIntervalType() {
        return intervalType;
    }
    public int getIntervalValue() {
        return intervalValue;
    }
    public boolean isComplianceRequired() {
        return complianceRequired;
    }
    public boolean isActive() {
        return active;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
