package com.java700.crvs.registry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Lifetime identity record. Names are disclosed on certificates/verification by design;
 *  list endpoints mask them. */
@Entity
@Table(name = "persons")
public class Person {

    public enum Status {
        ACTIVE, DECEASED
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "national_id", nullable = false, unique = true, length = 16)
    private String nationalId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "sex", nullable = false, length = 8)
    private String sex;

    @Column(name = "place_of_birth", nullable = false, length = 120)
    private String placeOfBirth;

    @Column(name = "parent_names", length = 300)
    private String parentNames;

    @Column(name = "region", nullable = false, length = 64)
    private String region;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(name = "deceased_at")
    private Instant deceasedAt;

    protected Person() {
    }

    public Person(String id, String nationalId, String fullName, LocalDate dob, String sex,
                  String placeOfBirth, String parentNames, String region, Instant registeredAt) {
        this.id = id;
        this.nationalId = nationalId;
        this.fullName = fullName;
        this.dob = dob;
        this.sex = sex;
        this.placeOfBirth = placeOfBirth;
        this.parentNames = parentNames;
        this.region = region;
        this.status = Status.ACTIVE.name();
        this.registeredAt = registeredAt;
    }

    public String getId() {
        return id;
    }

    public String getNationalId() {
        return nationalId;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getSex() {
        return sex;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getParentNames() {
        return parentNames;
    }

    public String getRegion() {
        return region;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public Instant getDeceasedAt() {
        return deceasedAt;
    }

    public void markDeceased(Instant at) {
        this.status = Status.DECEASED.name();
        this.deceasedAt = at;
    }

    public void applyCorrection(String newFullName, LocalDate newDob, String newPlaceOfBirth,
                         String newParentNames) {
        this.fullName = newFullName;
        this.dob = newDob;
        this.placeOfBirth = newPlaceOfBirth;
        this.parentNames = newParentNames;
    }
}
