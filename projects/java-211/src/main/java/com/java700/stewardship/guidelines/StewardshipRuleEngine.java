package com.java700.stewardship.guidelines;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.guidelines.StewardshipFinding.FindingType;
import com.java700.stewardship.guidelines.StewardshipFinding.Severity;
import com.java700.stewardship.microbiology.SusceptibilityResult;
import com.java700.stewardship.patients.Patient;
import com.java700.stewardship.prescriptions.Prescription;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Evaluates the active guideline rule set (and culture evidence) against a prescription
 * in its clinical context. Pure functions over a snapshot — deterministic and unit-testable.
 */
@Component
public class StewardshipRuleEngine {

    private static final List<String> REDUNDANCY_PRONE_TAGS =
            List.of("ANAEROBE", "MRSA", "VRE", "ESBL", "FUNGAL", "PSEUDOMONAS");

    private final ObjectMapper mapper;
    private final RenalCalculator renal;

    public StewardshipRuleEngine(ObjectMapper mapper, RenalCalculator renal) {
        this.mapper = mapper;
        this.renal = renal;
    }

    /**
     * @param rulesJson active guideline rule set
     * @param rx the prescription under evaluation
     * @param drug the prescribed antimicrobial
     * @param patient the patient (for renal dosing)
     * @param serumCreatinineMgDl latest creatinine, nullable
     * @param activeSiblingPrescriptions other ACTIVE prescriptions for the same patient
     * @param siblingDrugs their drugs (parallel list)
     * @param susceptibilityResults all susceptibility rows for the patient's current cultures
     * @param now evaluation instant
     */
    public List<StewardshipFinding> evaluate(String rulesJson, Prescription rx, AntimicrobialDrug drug,
                                             Patient patient, BigDecimal serumCreatinineMgDl,
                                             List<Prescription> activeSiblingPrescriptions,
                                             List<AntimicrobialDrug> siblingDrugs,
                                             List<SusceptibilityResult> susceptibilityResults,
                                             Instant now) {
        JsonNode rules = parse(rulesJson);
        List<StewardshipFinding> out = new ArrayList<>();
        if (rx.getStatus() != Prescription.Status.ACTIVE) {
            return out;
        }

        // 1. MAX_DURATION
        JsonNode maxDuration = rule(rules, RuleType.MAX_DURATION);
        if (maxDuration != null) {
            int defaultDays = maxDuration.path("params").path("defaultDays").asInt(7);
            int limit = defaultDays;
            for (JsonNode per : maxDuration.path("params").path("perIndication")) {
                if (per.path("indication").asText().equals(rx.getIndication())) {
                    limit = per.path("days").asInt();
                }
            }
            if (Duration.between(rx.getStartAt(), now).toDays() > limit) {
                out.add(new StewardshipFinding(FindingType.DURATION_EXCEEDED, Severity.WARNING,
                        "Duration exceeds guideline",
                        "Therapy has run " + Duration.between(rx.getStartAt(), now).toDays()
                                + " days; guideline limit is " + limit + " days for "
                                + rx.getIndication(),
                        "Review indication and consider stop or switch to oral step-down"));
            }
        }

        // 2. IV_TO_PO eligibility
        JsonNode ivToPo = rule(rules, RuleType.IV_TO_PO_ELIGIBILITY);
        if (ivToPo != null && "IV".equals(rx.getRoute()) && drug.isPoAvailable()) {
            long afebrileHours = ivToPo.path("params").path("afebrileHours").asLong(48);
            boolean requiresGi = ivToPo.path("params").path("requiresGiFunction").asBoolean(true);
            boolean requiresImprovement =
                    ivToPo.path("params").path("requiresClinicalImprovement").asBoolean(true);
            if (Duration.between(rx.getStartAt(), now).toHours() >= afebrileHours
                    && requiresGi && requiresImprovement) {
                out.add(new StewardshipFinding(FindingType.IV_TO_PO_ELIGIBLE, Severity.INFO,
                        "IV-to-PO switch candidate",
                        drug.getName() + " has been running IV for over " + afebrileHours
                                + " hours and an oral form is available",
                        "Propose IV_TO_PO intervention"));
            }
        }

        // 3. RENAL_ADJUSTMENT
        JsonNode renalRule = rule(rules, RuleType.RENAL_ADJUSTMENT);
        if (renalRule != null && serumCreatinineMgDl != null) {
            for (JsonNode drugRule : renalRule.path("params").path("drugs")) {
                if (drugRule.path("code").asText().equals(drug.getCode())) {
                    BigDecimal crcl = renal.creatinineClearance(patient, serumCreatinineMgDl);
                    int threshold = drugRule.path("thresholdCrCl").asInt(50);
                    if (crcl.signum() > 0 && crcl.intValue() < threshold) {
                        out.add(new StewardshipFinding(FindingType.RENAL_ADJUSTMENT_NEEDED,
                                Severity.WARNING, "Renal dose adjustment needed",
                                "CrCl " + crcl + " mL/min is below the " + threshold
                                        + " threshold for " + drug.getName(),
                                drugRule.path("advice").asText()));
                    }
                }
            }
        }

        // 4. DRUG_BUG_MISMATCH — culture shows resistance to the current drug
        for (SusceptibilityResult s : susceptibilityResults) {
            if (s.getDrugId().equals(rx.getDrugId()) && "R".equals(s.getResult())) {
                out.add(new StewardshipFinding(FindingType.DRUG_BUG_MISMATCH, Severity.CRITICAL,
                        "Drug-bug mismatch: resistant organism",
                        "Current therapy " + drug.getName()
                                + " is resistant for a cultured isolate — immediate review required",
                        "Escalate to ID physician; switch therapy per susceptibility"));
            }
        }

        // 5. DE_ESCALATION_CANDIDATE — narrower agent is susceptible
        int currentRank = spectrumRank(drug.getSpectrum());
        for (SusceptibilityResult s : susceptibilityResults) {
            if (!"S".equals(s.getResult()) || s.getDrugId().equals(rx.getDrugId())) {
                continue;
            }
            int candidateRank = spectrumRank(drugById(s.getDrugId(), siblingDrugs, drug).getSpectrum());
            if (candidateRank < currentRank) {
                out.add(new StewardshipFinding(FindingType.DE_ESCALATION_CANDIDATE, Severity.INFO,
                        "De-escalation candidate",
                        "A narrower-spectrum agent is susceptible for the cultured isolate",
                        "Propose DE_ESCALATE intervention"));
            }
        }

        // 6. REDUNDANT_COVERAGE — overlapping duplicate coverage beyond tolerance
        Map<String, List<Long>> tagWindows = new HashMap<>();
        collectTagWindows(rx, drug, tagWindows);
        for (int i = 0; i < activeSiblingPrescriptions.size(); i++) {
            collectTagWindows(activeSiblingPrescriptions.get(i), siblingDrugs.get(i), tagWindows);
        }
        for (String tag : REDUNDANCY_PRONE_TAGS) {
            List<Long> windows = tagWindows.get(tag);
            if (windows != null && windows.size() > 1) {
                long overlapStart = windows.stream().mapToLong(Long::longValue).max().orElse(0);
                long overlapHours = (now.toEpochMilli() - overlapStart) / 3_600_000L;
                if (overlapHours > 24) {
                    out.add(new StewardshipFinding(FindingType.REDUNDANT_COVERAGE, Severity.WARNING,
                            "Redundant " + tag + " coverage",
                            "Two or more agents with " + tag
                                    + " coverage overlap for more than 24 hours",
                            "Review combination therapy; stop the redundant agent"));
                }
            }
        }
        return out;
    }

    /** Time-based review trigger (empiric prescriptions older than N hours). */
    public boolean reviewDue(Prescription rx, JsonNode reviewTriggerRule, Instant now) {
        if (reviewTriggerRule == null || !rx.isEmpiric()) {
            return false;
        }
        long hours = reviewTriggerRule.path("params").path("empiricHours").asLong(48);
        return Duration.between(rx.getStartAt(), now).compareTo(Duration.of(hours, ChronoUnit.HOURS)) > 0;
    }

    public JsonNode reviewTriggerRule(String rulesJson) {
        return rule(parse(rulesJson), RuleType.REVIEW_TRIGGER);
    }

    private JsonNode parse(String rulesJson) {
        try {
            return mapper.readTree(rulesJson);
        } catch (Exception e) {
            throw new Problems.BadRequest("Malformed guideline rules JSON: " + e.getMessage());
        }
    }

    private static JsonNode rule(JsonNode rules, RuleType type) {
        for (JsonNode r : rules) {
            if (type.name().equals(r.path("type").asText())) {
                return r;
            }
        }
        return null;
    }

    private static int spectrumRank(String spectrum) {
        return switch (spectrum) {
            case "NARROW" -> 1;
            case "MEDIUM" -> 2;
            case "BROAD" -> 3;
            default -> 2;
        };
    }

    private static int spectrumRankById(String drugId, List<AntimicrobialDrug> siblings,
                                        AntimicrobialDrug self) {
        if (drugId.equals(self.getId())) {
            return spectrumRank(self.getSpectrum());
        }
        return siblings.stream()
                .filter(d -> d.getId().equals(drugId))
                .findFirst()
                .map(d -> spectrumRank(d.getSpectrum()))
                .orElse(2);
    }

    private static AntimicrobialDrug drugById(String drugId, List<AntimicrobialDrug> siblings,
                                              AntimicrobialDrug self) {
        if (drugId.equals(self.getId())) {
            return self;
        }
        return siblings.stream().filter(d -> d.getId().equals(drugId)).findFirst().orElse(self);
    }

    private static void collectTagWindows(Prescription rx, AntimicrobialDrug drug,
                                          Map<String, List<Long>> tagWindows) {
        if (rx.getStatus() != Prescription.Status.ACTIVE) {
            return;
        }
        for (String tag : drug.getCoverageTags()) {
            if (REDUNDANCY_PRONE_TAGS.contains(tag)) {
                tagWindows.computeIfAbsent(tag, k -> new ArrayList<>())
                        .add(rx.getStartAt().toEpochMilli());
            }
        }
    }
}
