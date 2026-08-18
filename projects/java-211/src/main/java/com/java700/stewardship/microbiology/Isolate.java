package com.java700.stewardship.microbiology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Organism isolated from a culture. */
@Entity
@Table(name = "isolates")
public class Isolate {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "culture_id", nullable = false, length = 36)
    private String cultureId;

    @Column(name = "organism", nullable = false, length = 120)
    private String organism;

    @Column(name = "collected_at", nullable = false)
    private Instant collectedAt;

    protected Isolate() {
    }

    public Isolate(String id, String cultureId, String organism, Instant collectedAt) {
        this.id = id;
        this.cultureId = cultureId;
        this.organism = organism;
        this.collectedAt = collectedAt;
    }

    public String getId() {
        return id;
    }

    public String getCultureId() {
        return cultureId;
    }

    public String getOrganism() {
        return organism;
    }

    public Instant getCollectedAt() {
        return collectedAt;
    }
}
