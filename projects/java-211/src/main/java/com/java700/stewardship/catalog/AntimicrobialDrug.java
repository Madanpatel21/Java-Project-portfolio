package com.java700.stewardship.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Antimicrobial catalog entry (WHO DDD-aligned). */
@Entity
@Table(name = "antimicrobial_drugs")
public class AntimicrobialDrug {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "drug_class", nullable = false, length = 64)
    private String drugClass;

    @Column(name = "spectrum", nullable = false, length = 16)
    private String spectrum;

    @Column(name = "ddd_grams", nullable = false, precision = 10, scale = 3)
    private BigDecimal dddGrams;

    @Column(name = "iv_available", nullable = false)
    private boolean ivAvailable;

    @Column(name = "po_available", nullable = false)
    private boolean poAvailable;

    @Column(name = "restricted", nullable = false)
    private boolean restricted;

    @Column(name = "coverage_tags", nullable = false, length = 200)
    private String coverageTags;

    @Column(name = "iv_cost_per_day", precision = 10, scale = 2)
    private BigDecimal ivCostPerDay;

    @Column(name = "po_cost_per_day", precision = 10, scale = 2)
    private BigDecimal poCostPerDay;

    protected AntimicrobialDrug() {
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

    public String getDrugClass() {
        return drugClass;
    }

    public String getSpectrum() {
        return spectrum;
    }

    public BigDecimal getDddGrams() {
        return dddGrams;
    }

    public boolean isIvAvailable() {
        return ivAvailable;
    }

    public boolean isPoAvailable() {
        return poAvailable;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public java.util.List<String> getCoverageTags() {
        return java.util.Arrays.asList(coverageTags.split(","));
    }

    public BigDecimal getIvCostPerDay() {
        return ivCostPerDay;
    }

    public BigDecimal getPoCostPerDay() {
        return poCostPerDay;
    }
}
