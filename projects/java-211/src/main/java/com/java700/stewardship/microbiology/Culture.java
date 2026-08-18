package com.java700.stewardship.microbiology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Microbiology culture specimen. */
@Entity
@Table(name = "cultures")
public class Culture {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "specimen_type", nullable = false, length = 32)
    private String specimenType;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    @Column(name = "reported_at")
    private Instant reportedAt;

    protected Culture() {
    }

    public Culture(String id, String patientId, String specimenType, Instant collectedAt) {
        this.id = id;
        this.patientId = patientId;
        this.specimenType = specimenType;
        this.collectedAt = collectedAt;
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getSpecimenType() {
        return specimenType;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public void report(Instant at) {
        this.reportedAt = at;
    }
}
