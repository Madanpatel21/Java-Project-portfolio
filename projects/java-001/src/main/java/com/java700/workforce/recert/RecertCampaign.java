package com.java700.workforce.recert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A periodic access-review campaign. */
@Entity
@Table(name = "recert_campaign")
public class RecertCampaign {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "window_end", nullable = false)
    private Instant windowEnd;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "generated_by", nullable = false, length = 120)
    private String generatedBy;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected RecertCampaign() {
    }

    public RecertCampaign(String id, String name, Instant windowStart, Instant windowEnd,
                          String generatedBy, Instant generatedAt) {
        this.id = id;
        this.name = name;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
        this.status = "OPEN";
        this.generatedBy = generatedBy;
        this.generatedAt = generatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public Instant getWindowEnd() {
        return windowEnd;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    void close() {
        this.status = "CLOSED";
    }
}
