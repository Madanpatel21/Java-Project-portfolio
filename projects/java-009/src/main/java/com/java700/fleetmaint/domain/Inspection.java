package com.java700.fleetmaint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Inspection. */
@Entity
@Table(name = "inspections")
public class Inspection {

    public static final String RESULT_PASS = "PASS";
    public static final String RESULT_FAIL = "FAIL";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "inspection_no", length = 32, nullable = false, unique = true)
    private String inspectionNo;
    @Column(name = "vehicle_id", length = 36, nullable = false)
    private String vehicleId;
    @Column(name = "inspection_type", length = 24, nullable = false)
    private String inspectionType;
    @Column(length = 120, nullable = false)
    private String inspector;
    @Column(length = 16, nullable = false)
    private String result;
    @Column(length = 500)
    private String notes;
    @Column(name = "valid_until")
    private LocalDate validUntil;
    @Column(name = "performed_at", nullable = false)
    private Instant performedAt;

    protected Inspection() {
    }

    public Inspection(String id, String inspectionNo, String vehicleId, String inspectionType, String inspector, String result, String notes, LocalDate validUntil, Instant performedAt) {
        this.id = id;
        this.inspectionNo = inspectionNo;
        this.vehicleId = vehicleId;
        this.inspectionType = inspectionType;
        this.inspector = inspector;
        this.result = result;
        this.notes = notes;
        this.validUntil = validUntil;
        this.performedAt = performedAt;

    }

    public String getId() {
        return id;
    }

    public String getInspectionNo() {
        return inspectionNo;
    }
    public String getVehicleId() {
        return vehicleId;
    }
    public String getInspectionType() {
        return inspectionType;
    }
    public String getInspector() {
        return inspector;
    }
    public String getResult() {
        return result;
    }
    public String getNotes() {
        return notes;
    }
    public LocalDate getValidUntil() {
        return validUntil;
    }
    public Instant getPerformedAt() {
        return performedAt;
    }
}
