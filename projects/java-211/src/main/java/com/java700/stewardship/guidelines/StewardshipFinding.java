package com.java700.stewardship.guidelines;

/** A rule hit against a prescription; drives review tasks and intervention suggestions. */
public record StewardshipFinding(
        FindingType type,
        Severity severity,
        String title,
        String detail,
        String suggestedAction) {

    public enum FindingType {
        DURATION_EXCEEDED,
        IV_TO_PO_ELIGIBLE,
        RENAL_ADJUSTMENT_NEEDED,
        DRUG_BUG_MISMATCH,
        DE_ESCALATION_CANDIDATE,
        REDUNDANT_COVERAGE,
        REVIEW_DUE
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }
}
