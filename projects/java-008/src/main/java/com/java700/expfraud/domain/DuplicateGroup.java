package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** A cluster of claims the engine believes are duplicates or split receipts. */
@Entity
@Table(name = "duplicate_groups")
public class DuplicateGroup {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_REVIEWED = "REVIEWED";
    public static final String STATUS_CLEARED = "CLEARED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "group_key", length = 64, nullable = false, unique = true)
    private String groupKey;

    @Column(name = "claim_ids", length = 2000, nullable = false)
    private String claimIds;

    @Column(length = 160)
    private String merchant;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "match_confidence", nullable = false, precision = 5, scale = 3)
    private BigDecimal matchConfidence;

    @Column(name = "group_size", nullable = false)
    private int groupSize;

    @Column(length = 24, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected DuplicateGroup() {
    }

    public DuplicateGroup(String id, String groupKey, String claimIds, String merchant,
                          BigDecimal amount, BigDecimal matchConfidence, int groupSize,
                          String status, Instant createdAt, Instant resolvedAt) {
        this.id = id;
        this.groupKey = groupKey;
        this.claimIds = claimIds;
        this.merchant = merchant;
        this.amount = amount;
        this.matchConfidence = matchConfidence;
        this.groupSize = groupSize;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public String getId() {
        return id;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public String getClaimIds() {
        return claimIds;
    }

    public String getMerchant() {
        return merchant;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getMatchConfidence() {
        return matchConfidence;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }
}
