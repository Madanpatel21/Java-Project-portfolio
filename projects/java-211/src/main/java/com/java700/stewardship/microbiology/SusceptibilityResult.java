package com.java700.stewardship.microbiology;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** CLSI-style susceptibility result (S/I/R) for an isolate-drug pair. */
@Entity
@Table(name = "susceptibility_results")
public class SusceptibilityResult {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "isolate_id", nullable = false, length = 36)
    private String isolateId;

    @Column(name = "drug_id", nullable = false, length = 36)
    private String drugId;

    @Column(name = "result", nullable = false, length = 4)
    private String result;

    @Column(name = "mic_value", precision = 12, scale = 4)
    private BigDecimal micValue;

    protected SusceptibilityResult() {
    }

    public SusceptibilityResult(String id, String isolateId, String drugId, String result,
                                BigDecimal micValue) {
        this.id = id;
        this.isolateId = isolateId;
        this.drugId = drugId;
        this.result = result;
        this.micValue = micValue;
    }

    public String getId() {
        return id;
    }

    public String getIsolateId() {
        return isolateId;
    }

    public String getDrugId() {
        return drugId;
    }

    public String getResult() {
        return result;
    }

    public BigDecimal getMicValue() {
        return micValue;
    }
}
