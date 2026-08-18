package com.java700.stewardship.prescriptions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

/** Antimicrobial order with a controlled lifecycle. */
@Entity
@Table(name = "prescriptions")
public class Prescription {

    public enum Status {
        PENDING_AUTHORIZATION, ACTIVE, STOPPED, COMPLETED, EXPIRED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "admission_id", nullable = false, length = 36)
    private String admissionId;

    @Column(name = "drug_id", nullable = false, length = 36)
    private String drugId;

    @Column(name = "indication", nullable = false, length = 200)
    private String indication;

    @Column(name = "route", nullable = false, length = 8)
    private String route;

    @Column(name = "dose_mg", nullable = false, precision = 10, scale = 2)
    private BigDecimal doseMg;

    @Column(name = "frequency_hours", nullable = false)
    private int frequencyHours;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "stop_at")
    private Instant stopAt;

    @Column(name = "status", nullable = false, length = 24)
    private String status;

    @Column(name = "empiric", nullable = false)
    private boolean empiric;

    @Column(name = "prescribed_by", nullable = false, length = 120)
    private String prescribedBy;

    @Column(name = "guideline_version_id", length = 36)
    private String guidelineVersionId;

    @Column(name = "restricted_auth_id", length = 36)
    private String restrictedAuthId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Prescription() {
    }

    public Prescription(String id, String patientId, String admissionId, String drugId,
                        String indication, String route, BigDecimal doseMg, int frequencyHours,
                        Instant startAt, boolean empiric, String prescribedBy,
                        String guidelineVersionId, Instant createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.admissionId = admissionId;
        this.drugId = drugId;
        this.indication = indication;
        this.route = route;
        this.doseMg = doseMg;
        this.frequencyHours = frequencyHours;
        this.startAt = startAt;
        this.empiric = empiric;
        this.prescribedBy = prescribedBy;
        this.guidelineVersionId = guidelineVersionId;
        this.createdAt = createdAt;
        this.status = Status.PENDING_AUTHORIZATION.name();
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getAdmissionId() {
        return admissionId;
    }

    public String getDrugId() {
        return drugId;
    }

    public String getIndication() {
        return indication;
    }

    public String getRoute() {
        return route;
    }

    public BigDecimal getDoseMg() {
        return doseMg;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getStopAt() {
        return stopAt;
    }

    public Status getStatus() {
        return status == null ? null : Status.valueOf(status);
    }

    public boolean isEmpiric() {
        return empiric;
    }

    public String getPrescribedBy() {
        return prescribedBy;
    }

    public String getGuidelineVersionId() {
        return guidelineVersionId;
    }

    public String getRestrictedAuthId() {
        return restrictedAuthId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void activate() {
        this.status = Status.ACTIVE.name();
    }

    void linkAuthorization(String newRestrictedAuthId) {
        this.restrictedAuthId = newRestrictedAuthId;
    }

    public void stop(Instant at) {
        this.status = Status.STOPPED.name();
        this.stopAt = at;
    }

    public void expire(Instant at) {
        this.status = Status.EXPIRED.name();
        this.stopAt = at;
    }

    void complete(Instant at) {
        this.status = Status.COMPLETED.name();
        this.stopAt = at;
    }

    void applyRouteChange(String newRoute, BigDecimal newDoseMg, int newFrequencyHours) {
        this.route = newRoute;
        this.doseMg = newDoseMg;
        this.frequencyHours = newFrequencyHours;
    }

    void applyDoseChange(BigDecimal newDoseMg, int newFrequencyHours) {
        this.doseMg = newDoseMg;
        this.frequencyHours = newFrequencyHours;
    }
}
