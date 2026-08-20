package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A person/organization in the firm's parties graph. */
@Entity
@Table(name = "parties")
public class Party {

    public enum Type {
        CLIENT, OPPONENT, RELATED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 200)
    private String normalizedName;

    @Column(name = "party_type", nullable = false, length = 16)
    private String partyType;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Party() {
    }

    public Party(String id, String name, String normalizedName, Type type, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizedName;
        this.partyType = type.name();
        this.active = true;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNormalizedName() {
        return normalizedName;
    }

    public Type getType() {
        return Type.valueOf(partyType);
    }

    public boolean isActive() {
        return active;
    }
}
