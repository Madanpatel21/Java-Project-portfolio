package com.java700.stewardship.patients;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Hospital admission (encounter) — the denominator anchor for utilization metrics. */
@Entity
@Table(name = "admissions")
public class Admission {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "ward", nullable = false, length = 64)
    private String ward;

    @Column(name = "admitted_at", nullable = false)
    private Instant admittedAt;

    @Column(name = "discharged_at")
    private Instant dischargedAt;

    protected Admission() {
    }

    public Admission(String id, String patientId, String ward, Instant admittedAt, Instant dischargedAt) {
        this.id = id;
        this.patientId = patientId;
        this.ward = ward;
        this.admittedAt = admittedAt;
        this.dischargedAt = dischargedAt;
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getWard() {
        return ward;
    }

    public Instant getAdmittedAt() {
        return admittedAt;
    }

    public Instant getDischargedAt() {
        return dischargedAt;
    }
}
