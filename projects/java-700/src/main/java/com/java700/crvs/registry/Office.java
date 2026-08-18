package com.java700.crvs.registry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** District registry office that captures registrations. */
@Entity
@Table(name = "offices")
public class Office {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 16)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "region", nullable = false, length = 64)
    private String region;

    protected Office() {
    }

    public Office(String id, String code, String name, String region) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.region = region;
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

    public String getRegion() {
        return region;
    }
}
