package com.java700.crvs.registration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Four-eyes life-event capture: registrar captures, supervisor decides. */
@Entity
@Table(name = "registrations")
public class Registration {

    public enum Type {
        BIRTH, MARRIAGE, DEATH, CORRECTION
    }

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "type", nullable = false, length = 16)
    private String type;

    @Column(name = "person_id", length = 36)
    private String personId;

    @Column(name = "spouse_person_id", length = 36)
    private String spousePersonId;

    @Column(name = "payload_json", nullable = false, length = 4096)
    private String payloadJson;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "office_id", nullable = false, length = 36)
    private String officeId;

    @Column(name = "registrar_id", nullable = false, length = 36)
    private String registrarId;

    @Column(name = "registrar_name", nullable = false, length = 120)
    private String registrarName;

    @Column(name = "supervisor_id", length = 36)
    private String supervisorId;

    @Column(name = "supervisor_name", length = 120)
    private String supervisorName;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", length = 1000)
    private String decisionNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Registration() {
    }

    public Registration(String id, Type type, String personId, String spousePersonId,
                        String payloadJson, String officeId, String registrarId,
                        String registrarName, Instant createdAt) {
        this.id = id;
        this.type = type.name();
        this.personId = personId;
        this.spousePersonId = spousePersonId;
        this.payloadJson = payloadJson;
        this.status = Status.PENDING.name();
        this.officeId = officeId;
        this.registrarId = registrarId;
        this.registrarName = registrarName;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return Type.valueOf(type);
    }

    public String getPersonId() {
        return personId;
    }

    public String getSpousePersonId() {
        return spousePersonId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public String getOfficeId() {
        return officeId;
    }

    public String getRegistrarId() {
        return registrarId;
    }

    public String getRegistrarName() {
        return registrarName;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    void linkPerson(String newPersonId) {
        this.personId = newPersonId;
    }

    void decide(Status newStatus, String newSupervisorId, String newSupervisorName,
                Instant newDecidedAt, String note) {
        this.status = newStatus.name();
        this.supervisorId = newSupervisorId;
        this.supervisorName = newSupervisorName;
        this.decidedAt = newDecidedAt;
        this.decisionNote = note;
    }
}
