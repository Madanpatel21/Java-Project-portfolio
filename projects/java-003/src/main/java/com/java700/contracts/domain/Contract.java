package com.java700.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

/** Contract header with a controlled lifecycle. */
@Entity
@Table(name = "contracts")
public class Contract {

    public enum Status {
        DRAFT, ACTIVE, EXPIRED, TERMINATED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "contract_no", nullable = false, unique = true, length = 40)
    private String contractNo;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "counterparty", nullable = false, length = 160)
    private String counterparty;

    @Column(name = "owner_id", nullable = false, length = 36)
    private String ownerId;

    @Column(name = "owner_name", nullable = false, length = 120)
    private String ownerName;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Contract() {
    }

    public Contract(String id, String contractNo, String title, String counterparty,
                    String ownerId, String ownerName, LocalDate effectiveFrom,
                    LocalDate effectiveTo, Instant createdAt) {
        this.id = id;
        this.contractNo = contractNo;
        this.title = title;
        this.counterparty = counterparty;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.status = Status.DRAFT.name();
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getContractNo() {
        return contractNo;
    }

    public String getTitle() {
        return title;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void activate() {
        this.status = Status.ACTIVE.name();
    }

    public void expire() {
        this.status = Status.EXPIRED.name();
    }

    public void terminate() {
        this.status = Status.TERMINATED.name();
    }
}
