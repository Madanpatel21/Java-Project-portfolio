package com.java700.stewardship.guidelines;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.guidelines.StewardshipFinding.FindingType;
import com.java700.stewardship.microbiology.SusceptibilityResult;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.prescriptions.Prescription;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StewardshipRuleEngineTest {

    private static final Instant NOW = Instant.parse("2026-08-18T00:00:00Z");
    private static final String RULES = """
            [
             {"type":"MAX_DURATION","params":{"defaultDays":7,"perIndication":[{"indication":"COMMUNITY_PNEUMONIA","days":5}]}},
             {"type":"IV_TO_PO_ELIGIBILITY","params":{"afebrileHours":48,"requiresGiFunction":true,"requiresClinicalImprovement":true}},
             {"type":"RENAL_ADJUSTMENT","params":{"drugs":[{"code":"PIPERACILLIN_TAZOBACTAM","thresholdCrCl":40,"advice":"Extend interval below CrCl 40"}]}},
             {"type":"REVIEW_TRIGGER","params":{"empiricHours":48,"targetedDays":5}}
            ]""";

    private StewardshipRuleEngine engine;
    private AntimicrobialDrug ceftriaxone;
    private AntimicrobialDrug pipTazo;
    private AntimicrobialDrug cefazolin;
    private Patient patient;

    @BeforeEach
    void setUp() {
        engine = new StewardshipRuleEngine(new ObjectMapper(), new RenalCalculator());
        ceftriaxone = drug("CEFTRIAXONE", "MEDIUM", List.of("GRAM_NEG", "GRAM_POS"), true, true, false);
        pipTazo = drug("PIPERACILLIN_TAZOBACTAM", "BROAD",
                List.of("GRAM_NEG", "GRAM_POS", "ANAEROBE", "PSEUDOMONAS"), true, false, false);
        cefazolin = drug("CEFAZOLIN", "NARROW", List.of("GRAM_POS"), true, true, false);
        patient = new Patient("p1", "MRN-1", "Test", LocalDate.of(1960, 1, 1), "M",
                new BigDecimal("70"));
    }

    private static AntimicrobialDrug drug(String code, String spectrum, List<String> tags,
                                          boolean iv, boolean po, boolean restricted) {
        return new AntimicrobialDrugStub(code, spectrum, tags, iv, po, restricted);
    }

    private static final class AntimicrobialDrugStub extends AntimicrobialDrug {
        private final String code;
        private final String spectrum;
        private final List<String> tags;
        private final boolean iv;
        private final boolean po;
        private final boolean restricted;

        AntimicrobialDrugStub(String code, String spectrum, List<String> tags,
                              boolean iv, boolean po, boolean restricted) {
            this.code = code;
            this.spectrum = spectrum;
            this.tags = tags;
            this.iv = iv;
            this.po = po;
            this.restricted = restricted;
        }

        @Override
        public String getId() {
            return code;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getName() {
            return code;
        }

        @Override
        public String getSpectrum() {
            return spectrum;
        }

        @Override
        public boolean isIvAvailable() {
            return iv;
        }

        @Override
        public boolean isPoAvailable() {
            return po;
        }

        @Override
        public boolean isRestricted() {
            return restricted;
        }

        @Override
        public java.util.List<String> getCoverageTags() {
            return tags;
        }
    }

    private Prescription activeRx(String drugId, String route, Instant startAt, boolean empiric,
                                  String indication) {
        Prescription rx = new Prescription("rx1", "p1", "a1", drugId, indication, route,
                new BigDecimal("1000"), 8, startAt, empiric, "dr", null, startAt);
        rx.activate();
        return rx;
    }

    private SusceptibilityResult susc(String isolateId, String drugId, String result) {
        return new SusceptibilityResult("s1", isolateId, drugId, result, null);
    }

    @Test
    void durationExceededFiresPerIndicationLimit() {
        Prescription rx = activeRx("CEFTRIAXONE", "IV",
                NOW.minus(6, ChronoUnit.DAYS), false, "COMMUNITY_PNEUMONIA");
        var findings = engine.evaluate(RULES, rx, ceftriaxone, patient, BigDecimal.ONE,
                List.of(), List.of(), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .contains(FindingType.DURATION_EXCEEDED);
    }

    @Test
    void ivToPoCandidateAfter48hWhenOralFormExists() {
        Prescription rx = activeRx("CEFTRIAXONE", "IV",
                NOW.minus(3, ChronoUnit.DAYS), true, "COMMUNITY_PNEUMONIA");
        var findings = engine.evaluate(RULES, rx, ceftriaxone, patient, BigDecimal.ONE,
                List.of(), List.of(), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .contains(FindingType.IV_TO_PO_ELIGIBLE);
    }

    @Test
    void noIvToPoWhenNoOralForm() {
        Prescription rx = activeRx("PIPERACILLIN_TAZOBACTAM", "IV",
                NOW.minus(3, ChronoUnit.DAYS), true, "SEPSIS");
        var findings = engine.evaluate(RULES, rx, pipTazo, patient, BigDecimal.ONE,
                List.of(), List.of(), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .doesNotContain(FindingType.IV_TO_PO_ELIGIBLE);
    }

    @Test
    void renalAdjustmentFiresBelowThreshold() {
        Prescription rx = activeRx("PIPERACILLIN_TAZOBACTAM", "IV",
                NOW.minus(1, ChronoUnit.DAYS), false, "SEPSIS");
        // CrCl = ((140-66)*70)/(72*2.5) ≈ 28.8 < 40
        var findings = engine.evaluate(RULES, rx, pipTazo, patient,
                new BigDecimal("2.5"), List.of(), List.of(), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .contains(FindingType.RENAL_ADJUSTMENT_NEEDED);
    }

    @Test
    void renalAdjustmentSilentAboveThreshold() {
        Prescription rx = activeRx("PIPERACILLIN_TAZOBACTAM", "IV",
                NOW.minus(1, ChronoUnit.DAYS), false, "SEPSIS");
        var findings = engine.evaluate(RULES, rx, pipTazo, patient,
                new BigDecimal("0.8"), List.of(), List.of(), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .doesNotContain(FindingType.RENAL_ADJUSTMENT_NEEDED);
    }

    @Test
    void drugBugMismatchIsCritical() {
        Prescription rx = activeRx("CEFTRIAXONE", "IV",
                NOW.minus(2, ChronoUnit.DAYS), false, "SEPSIS");
        var findings = engine.evaluate(RULES, rx, ceftriaxone, patient, BigDecimal.ONE,
                List.of(), List.of(),
                List.of(susc("iso1", "CEFTRIAXONE", "R")), NOW);
        StewardshipFinding f = findings.stream()
                .filter(x -> x.type() == FindingType.DRUG_BUG_MISMATCH).findFirst().orElseThrow();
        assertThat(f.severity()).isEqualTo(StewardshipFinding.Severity.CRITICAL);
    }

    @Test
    void deEscalationCandidateWhenNarrowerAgentSusceptible() {
        Prescription rx = activeRx("PIPERACILLIN_TAZOBACTAM", "IV",
                NOW.minus(2, ChronoUnit.DAYS), false, "SEPSIS");
        var findings = engine.evaluate(RULES, rx, pipTazo, patient, BigDecimal.ONE,
                List.of(), List.of(cefazolin),
                List.of(susc("iso1", "CEFAZOLIN", "S")), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .contains(FindingType.DE_ESCALATION_CANDIDATE);
    }

    @Test
    void redundantAnaerobicCoverageDetected() {
        Prescription rx = activeRx("PIPERACILLIN_TAZOBACTAM", "IV",
                NOW.minus(3, ChronoUnit.DAYS), false, "SEPSIS");
        AntimicrobialDrug metro = drug("METRONIDAZOLE", "NARROW", List.of("ANAEROBE"),
                true, true, false);
        Prescription sibling = new Prescription("rx2", "p1", "a1", "METRONIDAZOLE", "SEPSIS",
                "IV", new BigDecimal("500"), 8, NOW.minus(2, ChronoUnit.DAYS), false, "dr",
                null, NOW.minus(2, ChronoUnit.DAYS));
        sibling.activate();
        var findings = engine.evaluate(RULES, rx, pipTazo, patient, BigDecimal.ONE,
                List.of(sibling), List.of(metro), List.of(), NOW);
        assertThat(findings).extracting(StewardshipFinding::type)
                .contains(FindingType.REDUNDANT_COVERAGE);
    }

    @Test
    void reviewDueAfterEmpiricWindow() {
        Prescription rx = activeRx("CEFTRIAXONE", "IV",
                NOW.minus(3, ChronoUnit.DAYS), true, "SEPSIS");
        assertThat(engine.reviewDue(rx, engine.reviewTriggerRule(RULES), NOW)).isTrue();
        Prescription fresh = activeRx("CEFTRIAXONE", "IV",
                NOW.minus(10, ChronoUnit.HOURS), true, "SEPSIS");
        assertThat(engine.reviewDue(fresh, engine.reviewTriggerRule(RULES), NOW)).isFalse();
    }
}
