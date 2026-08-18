package com.java700.stewardship.patients;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Patient master record. PHI is masked at the API boundary, never in the database. */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "mrn", nullable = false, unique = true, length = 32)
    private String mrn;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Column(name = "sex", nullable = false, length = 8)
    private String sex;

    @Column(name = "weight_kg", nullable = false, precision = 6, scale = 2)
    private BigDecimal weightKg;

    protected Patient() {
    }

    public Patient(String id, String mrn, String name, LocalDate dob, String sex, BigDecimal weightKg) {
        this.id = id;
        this.mrn = mrn;
        this.name = name;
        this.dob = dob;
        this.sex = sex;
        this.weightKg = weightKg;
    }

    public String getId() {
        return id;
    }

    public String getMrn() {
        return mrn;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getSex() {
        return sex;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }
}
