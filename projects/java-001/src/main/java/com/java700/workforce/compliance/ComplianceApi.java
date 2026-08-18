package com.java700.workforce.compliance;

import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ComplianceApi {

    private ComplianceApi() {
    }

    public record ViolationView(String id, String userId, String policyCode, String ruleType,
                                String severity, String status, String description, Long evidenceSeq,
                                Instant detectedAt, Instant acknowledgedAt, Instant remediatedAt,
                                Instant closedAt, String remediationNote) {

        static ViolationView from(Violation v) {
            return new ViolationView(v.getId(), v.getUserId(), v.getPolicyCode(), v.getRuleType(),
                    v.getSeverity(), v.getStatus().name(), v.getDescription(), v.getEvidenceSeq(),
                    v.getDetectedAt(), v.getAcknowledgedAt(), v.getRemediatedAt(), v.getClosedAt(),
                    v.getRemediationNote());
        }
    }

    public record RemediateRequest(@Size(max = 2000) String note) {
    }

    public record RunResult(int violationsCreated, int violationsClosed, long durationMs) {
    }
}
