package com.java700.stewardship.patients;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Laboratory value (creatinine, inflammatory markers) used by dosing rules. */
@Entity
@Table(name = "lab_values")
public class LabValue {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "reading", nullable = false, precision = 12, scale = 3)
    private BigDecimal value;

    @Column(name = "unit", nullable = false, length = 16)
    private String unit;

    @Column(name = "measured_at", nullable = false)
    private Instant measuredAt;

    protected LabValue() {
    }

    public LabValue(String id, String patientId, String type, BigDecimal value, String unit,
                    Instant measuredAt) {
        this.id = id;
        this.patientId = patientId;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.measuredAt = measuredAt;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public Instant getMeasuredAt() {
        return measuredAt;
    }
}
