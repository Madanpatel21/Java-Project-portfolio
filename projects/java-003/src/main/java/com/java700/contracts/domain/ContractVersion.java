package com.java700.contracts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Immutable contract version: clauses as JSON, versioned with the author. */
@Entity
@Table(name = "contract_versions")
public class ContractVersion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "contract_id", nullable = false, length = 36)
    private String contractId;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(name = "content_json", nullable = false, length = 8192)
    private String contentJson;

    @Column(name = "created_by", nullable = false, length = 120)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ContractVersion() {
    }

    public ContractVersion(String id, String contractId, int versionNo, String contentJson,
                           String createdBy, Instant createdAt) {
        this.id = id;
        this.contractId = contractId;
        this.versionNo = versionNo;
        this.contentJson = contentJson;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getContractId() {
        return contractId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public String getContentJson() {
        return contentJson;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
