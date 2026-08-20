package com.java700.expfraud.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.common.api.Problems;
import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.domain.ExpenseClaimRepository;
import com.java700.expfraud.domain.FraudCase;
import com.java700.expfraud.domain.FraudCaseRepository;
import com.java700.expfraud.messaging.DomainEventBus;
import com.java700.expfraud.messaging.FraudEvents;
import com.java700.expfraud.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Four-eyes fraud case workflow.
 *
 * <p>A case is opened automatically for every high-risk claim. It moves OPEN -&gt; REVIEWED
 * (first investigator records a recommendation) -&gt; CONFIRMED_FRAUD | CLEARED (a second,
 * different investigator makes the binding decision). Self-confirmation is rejected, and the
 * claim status follows the case decision.</p>
 */
@Service
public class CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseService.class);

    public static final String RECOMMEND_FRAUD = "RECOMMEND_FRAUD";
    public static final String RECOMMEND_CLEAR = "RECOMMEND_CLEAR";
    public static final String DECISION_CONFIRM_FRAUD = "CONFIRM_FRAUD";
    public static final String DECISION_CLEAR = "CLEAR";

    private final FraudCaseRepository cases;
    private final ExpenseClaimRepository claims;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final ObjectMapper mapper;

    public CaseService(FraudCaseRepository cases, ExpenseClaimRepository claims,
                       DomainEventBus events, AuditLogService audit, Metrics metrics,
                       Clock clock, ObjectMapper mapper) {
        this.cases = cases;
        this.claims = claims;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.mapper = mapper;
    }

    /** Opens an investigation case for a high-risk claim (idempotent per claim). */
    @Transactional
    public FraudCase open(ExpenseClaim claim, String openedBy) {
        FraudCase existing = cases.findByClaimId(claim.getId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        FraudCase fraudCase = new FraudCase(UUID.randomUUID().toString(), nextCaseNo(),
                claim.getId(), claim.getRiskScore(), claim.getReasonsJson(), null,
                FraudCase.STATUS_OPEN, openedBy, Instant.now(clock));
        FraudCase saved = cases.save(fraudCase);
        metrics.caseOpened();
        audit.record("CASE_OPENED", "fraud_case", saved.getCaseNo(),
                "claim=" + claim.getClaimNo() + " score=" + claim.getRiskScore());
        events.publish(new FraudEvents.CaseOpened(UUID.randomUUID().toString(), Instant.now(clock),
                saved.getId(), saved.getCaseNo(), claim.getId(), claim.getRiskScore()));
        return saved;
    }

    /** First investigator records a recommendation (OPEN -&gt; REVIEWED). */
    @Transactional
    public FraudCase review(String caseId, String recommendation, String note, String username) {
        if (!RECOMMEND_FRAUD.equals(recommendation) && !RECOMMEND_CLEAR.equals(recommendation)) {
            throw new Problems.BadRequest("recommendation must be RECOMMEND_FRAUD or RECOMMEND_CLEAR");
        }
        FraudCase fraudCase = load(caseId);
        if (!FraudCase.STATUS_OPEN.equals(fraudCase.getStatus())) {
            throw new Problems.Conflict("case " + fraudCase.getCaseNo() + " is already "
                    + fraudCase.getStatus());
        }
        fraudCase.firstReview(username, note, Instant.now(clock));
        audit.record("CASE_REVIEWED", "fraud_case", fraudCase.getCaseNo(),
                "reviewer=" + username + " recommendation=" + recommendation);
        return cases.save(fraudCase);
    }

    /** Second, different investigator makes the binding decision (four-eyes). */
    @Transactional
    public FraudCase decide(String caseId, String decision, String note, String username) {
        if (!DECISION_CONFIRM_FRAUD.equals(decision) && !DECISION_CLEAR.equals(decision)) {
            throw new Problems.BadRequest("decision must be CONFIRM_FRAUD or CLEAR");
        }
        FraudCase fraudCase = load(caseId);
        if (!FraudCase.STATUS_REVIEWED.equals(fraudCase.getStatus())) {
            throw new Problems.Conflict("case " + fraudCase.getCaseNo() + " must be REVIEWED first");
        }
        if (username.equals(fraudCase.getReviewerOne())) {
            throw new Problems.Conflict(
                    "four-eyes violated: the deciding investigator must differ from the reviewer");
        }
        fraudCase.finalDecision(username, decision, note, Instant.now(clock));
        ExpenseClaim claim = claims.findById(fraudCase.getClaimId())
                .orElseThrow(() -> new Problems.NotFound("claim of case " + fraudCase.getCaseNo()));
        if (DECISION_CONFIRM_FRAUD.equals(decision)) {
            claim.transition(ExpenseClaim.STATUS_CONFIRMED_FRAUD);
            metrics.caseConfirmedFraud();
        } else {
            claim.transition(ExpenseClaim.STATUS_APPROVED);
            metrics.caseCleared();
        }
        claims.save(claim);
        FraudCase saved = cases.save(fraudCase);
        audit.record("CASE_DECIDED", "fraud_case", saved.getCaseNo(),
                "decision=" + decision + " by=" + username);
        events.publish(new FraudEvents.CaseDecided(UUID.randomUUID().toString(), Instant.now(clock),
                saved.getId(), saved.getCaseNo(), decision, username));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FraudCase> openCases() {
        return cases.findByStatusOrderByOpenedAtAsc(FraudCase.STATUS_OPEN);
    }

    @Transactional(readOnly = true)
    public List<FraudCase> pendingDecisions() {
        return cases.findByStatusOrderByOpenedAtAsc(FraudCase.STATUS_REVIEWED);
    }

    @Transactional(readOnly = true)
    public FraudCase load(String caseId) {
        return cases.findById(caseId)
                .orElseThrow(() -> new Problems.NotFound("fraud case " + caseId));
    }

    @Transactional(readOnly = true)
    public Api.CaseView view(FraudCase fraudCase) {
        ExpenseClaim claim = claims.findById(fraudCase.getClaimId()).orElse(null);
        return new Api.CaseView(fraudCase.getId(), fraudCase.getCaseNo(), fraudCase.getClaimId(),
                claim == null ? "?" : claim.getClaimNo(), fraudCase.getRiskScore(),
                parseReasons(fraudCase.getReasonsJson()), fraudCase.getEvidenceJson(),
                fraudCase.getStatus(), fraudCase.getOpenedBy(), fraudCase.getOpenedAt(),
                fraudCase.getReviewerOne(), fraudCase.getReviewerOneNote(), fraudCase.getReviewedAt(),
                fraudCase.getReviewerTwo(), fraudCase.getDecision(), fraudCase.getDecisionNote(),
                fraudCase.getDecidedAt());
    }

    private List<Api.ScoreReason> parseReasons(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Api.ScoreReason>>() {
            });
        } catch (JsonProcessingException ex) {
            log.warn("Unparseable reasons JSON on case {}", json, ex);
            return List.of();
        }
    }

    private synchronized String nextCaseNo() {
        long count = cases.count();
        return "CASE-2026-" + String.format("%05d", count + 1);
    }
}
