package com.java700.expfraud.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** API request/response records (immutable, defensive copies on collections). */
public final class Api {

    private Api() {
    }

    public record SubmitClaimRequest(String employeeId, String employeeName, String department,
                                     String category, BigDecimal amount, String currency,
                                     String merchant, LocalDate expenseDate, String description,
                                     String receiptRef) {
    }

    public record ApproveRequest(String note) {
    }

    public record ScoreReason(String code, int points, String severity, String message) {
    }

    public record ScoreResult(int score, String tier, boolean autoCase, List<ScoreReason> reasons) {
        public ScoreResult {
            reasons = List.copyOf(reasons);
        }

        @Override
        public List<ScoreReason> reasons() {
            return List.copyOf(reasons);
        }
    }

    public record ViolationView(String ruleCode, String ruleMessage, String observed,
                                String expected, String severity, int points) {
    }

    public record ClaimView(String id, String claimNo, String employeeName, String department,
                            String category, BigDecimal amount, String currency, String merchant,
                            LocalDate expenseDate, String description, String status, int riskScore,
                            String riskTier, Instant submittedAt, List<ScoreReason> reasons,
                            List<ViolationView> violations) {
        public ClaimView {
            reasons = List.copyOf(reasons);
            violations = List.copyOf(violations);
        }

        @Override
        public List<ScoreReason> reasons() {
            return List.copyOf(reasons);
        }

        @Override
        public List<ViolationView> violations() {
            return List.copyOf(violations);
        }
    }

    public record CaseReviewRequest(String recommendation, String note) {
    }

    public record CaseDecisionRequest(String decision, String note) {
    }

    public record CaseView(String id, String caseNo, String claimId, String claimNo, int riskScore,
                           List<ScoreReason> reasons, String evidence, String status,
                           String openedBy, Instant openedAt, String reviewerOne,
                           String reviewerOneNote, Instant reviewedAt, String reviewerTwo,
                           String decision, String decisionNote, Instant decidedAt) {
        public CaseView {
            reasons = List.copyOf(reasons);
        }

        @Override
        public List<ScoreReason> reasons() {
            return List.copyOf(reasons);
        }
    }

    public record TipSubmitRequest(String channel, String subject, String description,
                                   String relatedClaimNo) {
    }

    public record TipView(String id, String tipNo, String channel, String subject,
                          String description, String relatedClaimNo, String status,
                          String outcome, Instant submittedAt, Instant reviewedAt) {
    }

    public record TipReviewRequest(String outcome) {
    }

    public record BaselineView(String department, String category, BigDecimal mean,
                               BigDecimal median, BigDecimal p90, BigDecimal stdDev,
                               int sampleCount, Instant updatedAt) {
    }

    public record RuleView(String code, String category, String comparator, BigDecimal threshold,
                           String pattern, String severity, String message, boolean active) {
    }

    public record DuplicateGroupView(String id, String groupKey, List<String> claimNos,
                                     String merchant, BigDecimal amount, BigDecimal confidence,
                                     int size, String status, Instant createdAt) {
        public DuplicateGroupView {
            claimNos = List.copyOf(claimNos);
        }

        @Override
        public List<String> claimNos() {
            return List.copyOf(claimNos);
        }
    }

    public record StatsView(long claimsSubmitted, long claimsApproved, long claimsRejected,
                            long claimsUnderReview, long claimsConfirmedFraud, long casesOpen,
                            long duplicateGroupsOpen, long tipsNew, double avgRiskScore,
                            long baselines) {
    }
}
