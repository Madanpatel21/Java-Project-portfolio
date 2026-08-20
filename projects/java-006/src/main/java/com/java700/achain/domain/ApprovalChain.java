package com.java700.achain.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A reusable multi-step approval chain definition (JSON steps). */
@Entity
@Table(name = "approval_chains")
public class ApprovalChain {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "chain_code", nullable = false, unique = true, length = 64)
    private String chainCode;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "steps_json", nullable = false, length = 4096)
    private String stepsJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ApprovalChain() {
    }

    public ApprovalChain(String id, String chainCode, String name, String stepsJson,
                         Instant createdAt) {
        this.id = id;
        this.chainCode = chainCode;
        this.name = name;
        this.stepsJson = stepsJson;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getChainCode() {
        return chainCode;
    }

    public String getName() {
        return name;
    }

    public String getStepsJson() {
        return stepsJson;
    }
}
