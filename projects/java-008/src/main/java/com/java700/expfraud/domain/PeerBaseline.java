package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/** Peer spending baseline per department + category, recomputed by the scheduler. */
@Entity
@Table(name = "peer_baselines")
public class PeerBaseline {

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 64, nullable = false)
    private String department;

    @Column(length = 32, nullable = false)
    private String category;

    @Column(name = "mean_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal meanAmount;

    @Column(name = "median_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal medianAmount;

    @Column(name = "p90_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal p90Amount;

    @Column(name = "std_dev", nullable = false, precision = 12, scale = 2)
    private BigDecimal stdDev;

    @Column(name = "sample_count", nullable = false)
    private int sampleCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PeerBaseline() {
    }

    public PeerBaseline(String id, String department, String category, BigDecimal meanAmount,
                        BigDecimal medianAmount, BigDecimal p90Amount, BigDecimal stdDev,
                        int sampleCount, Instant updatedAt) {
        this.id = id;
        this.department = department;
        this.category = category;
        this.meanAmount = meanAmount;
        this.medianAmount = medianAmount;
        this.p90Amount = p90Amount;
        this.stdDev = stdDev;
        this.sampleCount = sampleCount;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getDepartment() {
        return department;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getMeanAmount() {
        return meanAmount;
    }

    public BigDecimal getMedianAmount() {
        return medianAmount;
    }

    public BigDecimal getP90Amount() {
        return p90Amount;
    }

    public BigDecimal getStdDev() {
        return stdDev;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
