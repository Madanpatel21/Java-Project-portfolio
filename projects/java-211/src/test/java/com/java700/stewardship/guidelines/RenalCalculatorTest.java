package com.java700.stewardship.guidelines;

import static org.assertj.core.api.Assertions.assertThat;

import com.java700.stewardship.patients.Patient;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RenalCalculatorTest {

    private final RenalCalculator calculator = new RenalCalculator();

    private Patient patient(String sex, String weight, int birthYear) {
        return new Patient("p1", "MRN-1", "Test", LocalDate.of(birthYear, 1, 1), sex,
                new BigDecimal(weight));
    }

    @Test
    void maleCockcroftGault() {
        Patient p = patient("M", "70", 1960); // age ~66 in 2026
        BigDecimal crcl = calculator.creatinineClearance(p, new BigDecimal("1.0"));
        // ((140-66)*70)/(72*1.0) ≈ 71.9
        assertThat(crcl).isEqualByComparingTo("71.9");
    }

    @Test
    void femaleAdjustmentFactorApplied() {
        Patient p = patient("F", "64", 1942); // age ~84
        // ((140-84)*64)/72 = 49.78 → ×0.85 ≈ 42.3
        BigDecimal crcl = calculator.creatinineClearance(p, new BigDecimal("1.0"));
        assertThat(crcl).isEqualByComparingTo("42.3");
    }

    @Test
    void renalImpairmentLowersClearance() {
        Patient p = patient("M", "70", 1960);
        BigDecimal impaired = calculator.creatinineClearance(p, new BigDecimal("2.5"));
        BigDecimal normal = calculator.creatinineClearance(p, new BigDecimal("1.0"));
        assertThat(impaired).isLessThan(normal);
    }

    @Test
    void missingDataYieldsNegative() {
        Patient p = patient("M", "70", 1960);
        assertThat(calculator.creatinineClearance(p, null).signum()).isNegative();
        assertThat(calculator.creatinineClearance(p, BigDecimal.ZERO).signum()).isNegative();
    }
}
