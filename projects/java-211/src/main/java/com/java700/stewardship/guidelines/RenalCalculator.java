package com.java700.stewardship.guidelines;

import com.java700.stewardship.patients.Patient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import org.springframework.stereotype.Component;

/**
 * Cockcroft-Gault creatinine clearance estimation from patient demographics and the
 * most recent serum creatinine (mg/dL).
 */
@Component
public class RenalCalculator {

    /** Returns CrCl in mL/min, or -1 when insufficient data. */
    public BigDecimal creatinineClearance(Patient patient, BigDecimal serumCreatinineMgDl) {
        if (patient.getWeightKg() == null || serumCreatinineMgDl == null
                || serumCreatinineMgDl.signum() <= 0) {
            return BigDecimal.valueOf(-1);
        }
        int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
        if (age < 1) {
            age = 1;
        }
        BigDecimal numerator = BigDecimal.valueOf(140L - age).multiply(patient.getWeightKg());
        BigDecimal denominator = BigDecimal.valueOf(72).multiply(serumCreatinineMgDl);
        BigDecimal crcl = numerator.divide(denominator, 1, RoundingMode.HALF_UP);
        if ("F".equalsIgnoreCase(patient.getSex())) {
            crcl = crcl.multiply(new BigDecimal("0.85"));
        }
        return crcl.setScale(1, RoundingMode.HALF_UP);
    }
}
