package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** RosterRule. */
@Entity
@Table(name = "roster_rules")
public class RosterRule {


    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 40, nullable = false, unique = true)
    private String code;
    @Column(length = 160, nullable = false)
    private String name;
    @Column(length = 16, nullable = false)
    private String level;
    @Column(nullable = false)
    private int threshold;
    @Column(nullable = false)
    private int weight;
    @Column(nullable = false)
    private boolean active;

    protected RosterRule() {
    }

    public RosterRule(String id, String code, String name, String level, int threshold, int weight, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.level = level;
        this.threshold = threshold;
        this.weight = weight;
        this.active = active;

    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }
    public String getName() {
        return name;
    }
    public String getLevel() {
        return level;
    }
    public int getThreshold() {
        return threshold;
    }
    public int getWeight() {
        return weight;
    }
    public boolean getActive() {
        return active;
    }
}
