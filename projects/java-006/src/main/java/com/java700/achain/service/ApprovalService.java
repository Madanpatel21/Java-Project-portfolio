package com.java700.achain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.achain.common.api.PageResponse;
import com.java700.achain.common.api.Problems;
import com.java700.achain.common.audit.AuditLogService;
import com.java700.achain.common.web.IdempotencyService;
import com.java700.achain.domain.ApprovalChain;
import com.java700.achain.domain.ApprovalChainRepository;
import com.java700.achain.domain.ApprovalDecision;
import com.java700.achain.domain.ApprovalDecisionRepository;
import com.java700.achain.domain.ApprovalRequest;
import com.java700.achain.domain.ApprovalRequestRepository;
import com.java700.achain.domain.Policy;
import com.java700.achain.domain.PolicyRepository;
import com.java700.achain.domain.PolicyVersion;
import com.java700.achain.domain.PolicyVersionRepository;
import com.java700.achain.messaging.DomainEvent;
import com.java700.achain.messaging.DomainEventBus;
import com.java700.achain.observability.Metrics;
import com.java700.achain.security.Roles;
import com.java700.achain.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Audit-grade approval engine: multi-step chains with per-step dual control,
 * requests bound to an immutable policy-version snapshot, every decision recorded
 * as evidence, and SLA escalation for stale requests.
 */
@Service
public class ApprovalService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalService.class);

    private final PolicyRepository policyRepository;
    private final PolicyVersionRepository versionRepository;
    private final ApprovalChainRepository chainRepository;
    private final ApprovalRequestRepository requestRepository;
    private final ApprovalDecisionRepository decisionRepository;
    private final ChainParser chainParser;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final Clock clock;

    public ApprovalService(PolicyRepository policyRepository,
                           PolicyVersionRepository versionRepository,
                           ApprovalChainRepository chainRepository,
                           ApprovalRequestRepository requestRepository,
                           ApprovalDecisionRepository decisionRepository,
                           ChainParser chainParser, DomainEventBus bus,
                           IdempotencyService idempotency, AuditLogService audit,
                           Metrics metrics, ObjectMapper mapper, Clock clock) {
        this.policyRepository = policyRepository;
        this.versionRepository = versionRepository;
        this.chainRepository = chainRepository;
        this.requestRepository = requestRepository;
        this.decisionRepository = decisionRepository;
        this.chainParser = chainParser;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- policies

    @Transactional
    public Api.PolicyView createPolicy(Api.CreatePolicyRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "POLICY");
        if (existing != null) {
            return view(loadPolicy(existing));
        }
        try {
            Policy policy = policyRepository.findByPolicyCode(req.policyCode())
                    .orElseGet(() -> policyRepository.save(new Policy(UUID.randomUUID().toString(),
                            req.policyCode(), req.name(), req.description(), Instant.now(clock))));
            int next = versionRepository.findByPolicyIdOrderByVersionNoDesc(policy.getId())
                    .stream().findFirst().map(PolicyVersion::getVersionNo).orElse(0) + 1;
            versionRepository.findByPolicyIdAndStatus(policy.getId(), "ACTIVE").ifPresent(active ->
                    versionRepository.save(new PolicyVersion(active.getId(), active.getPolicyId(),
                            active.getVersionNo(), active.getRulesJson(), "SUPERSEDED",
                            active.getEffectiveFrom(), active.getCreatedBy(), active.getCreatedAt())));
            PolicyVersion version = new PolicyVersion(UUID.randomUUID().toString(), policy.getId(),
                    next, req.rulesJson(), "ACTIVE", Instant.now(clock),
                    SecurityUtil.currentUsername(), Instant.now(clock));
            versionRepository.save(version);
            policy.activateVersion(version.getId());
            policyRepository.save(policy);
            audit.record("POLICY_VERSION_ACTIVATED", "POLICY", policy.getPolicyCode(),
                    "Activated v" + next);
            idempotency.complete(idemKey, policy.getId(), 201);
            return view(policy);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- chains

    @Transactional
    public Api.ChainView createChain(Api.CreateChainRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "APPROVAL_CHAIN");
        if (existing != null) {
            return chainView(loadChain(existing));
        }
        try {
            try {
                chainParser.parse(req.stepsJson());
            } catch (IllegalArgumentException e) {
                throw new Problems.BadRequest(e.getMessage());
            }
            if (chainRepository.findByChainCode(req.chainCode()).isPresent()) {
                throw new Problems.Conflict("Chain code already exists");
            }
            ApprovalChain chain = new ApprovalChain(UUID.randomUUID().toString(), req.chainCode(),
                    req.name(), req.stepsJson(), Instant.now(clock));
            chainRepository.save(chain);
            audit.record("CHAIN_CREATED", "APPROVAL_CHAIN", chain.getId(), req.chainCode());
            idempotency.complete(idemKey, chain.getId(), 201);
            return chainView(chain);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- requests

    @Transactional
    public Api.RequestView createRequest(Api.CreateRequestRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "APPROVAL_REQUEST");
        if (existing != null) {
            return requestView(loadRequest(existing));
        }
        try {
            ApprovalChain chain = chainRepository.findByChainCode(req.chainCode())
                    .orElseThrow(() -> new Problems.NotFound("Chain not found"));
            Policy policy = policyRepository.findByPolicyCode(req.policyCode())
                    .orElseThrow(() -> new Problems.NotFound("Policy not found"));
            PolicyVersion version = versionRepository.findById(policy.getActiveVersionId())
                    .orElseThrow(() -> new Problems.NotFound("Policy has no active version"));
            String payloadJson = toJson(req.payload());
            ApprovalRequest request = new ApprovalRequest(UUID.randomUUID().toString(),
                    chain.getId(), version.getId(), req.subjectType(), req.subjectId(), payloadJson,
                    SecurityUtil.currentUserId(), SecurityUtil.currentUsername(), req.dueAt(),
                    Instant.now(clock));
            requestRepository.save(request);
            metrics.incrementRequestsCreated();
            metrics.setPendingRequests(requestRepository.countByStatus("PENDING"));
            audit.record("APPROVAL_REQUESTED", "APPROVAL_REQUEST", request.getId(),
                    req.subjectType() + "/" + req.subjectId() + " chain=" + req.chainCode()
                            + " policy=" + req.policyCode() + " v" + version.getVersionNo());
            idempotency.complete(idemKey, request.getId(), 201);
            return requestView(request);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    /** Records one approver's decision and advances the chain when the step is satisfied. */
    @Transactional
    public Api.RequestView decide(String requestId, boolean approve, String note, String idemKey) {
        String existing = idempotency.begin(idemKey, "APPROVAL_DECISION");
        ApprovalRequest request;
        try {
            request = loadRequest(requestId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (existing != null) {
            return requestView(request);
        }
        if (request.getStatus() != ApprovalRequest.Status.PENDING) {
            throw new Problems.Conflict("Request is already decided");
        }
        ApprovalChain chain = loadChain(request.getChainId());
        List<ChainParser.Step> steps = chainParser.parse(chain.getStepsJson());
        ChainParser.Step current = steps.get(request.getCurrentStep() - 1);
        String approverId = SecurityUtil.currentUserId();
        String approverName = SecurityUtil.currentUsername();

        if (approverId.equals(request.getRequestedById())) {
            throw new Problems.Conflict("Segregation of duties: the requester may not approve");
        }
        if (!SecurityUtil.hasRole(current.role()) && !SecurityUtil.hasRole(Roles.ADMIN)) {
            throw new Problems.Conflict("Step " + current.step() + " requires role " + current.role());
        }
        List<ApprovalDecision> stepDecisions = decisionRepository
                .findByRequestIdOrderByStepNoAsc(requestId).stream()
                .filter(d -> d.getStepNo() == current.step()).toList();
        boolean already = stepDecisions.stream().anyMatch(d -> d.getApproverId().equals(approverId));
        if (already) {
            throw new Problems.Conflict("Dual control: this approver has already decided this step");
        }

        decisionRepository.save(new ApprovalDecision(UUID.randomUUID().toString(), requestId,
                current.step(), approverId, approverName, approve ? "APPROVE" : "REJECT",
                note, Instant.now(clock)));
        metrics.incrementDecisionsRecorded();
        audit.record("APPROVAL_DECISION", "APPROVAL_REQUEST", requestId,
                current.role() + " step " + current.step() + " -> " + (approve ? "APPROVE" : "REJECT")
                        + " by " + approverName);

        if (!approve) {
            request.finish(ApprovalRequest.Status.REJECTED, Instant.now(clock));
            requestRepository.save(request);
            metrics.setPendingRequests(requestRepository.countByStatus("PENDING"));
            bus.publish(new RequestRejected(UUID.randomUUID().toString(), Instant.now(clock),
                    requestId, request.getSubjectType()));
            idempotency.complete(idemKey, requestId, 200);
            return requestView(request);
        }

        int approvalsThisStep = stepDecisions.size() + 1;
        if (approvalsThisStep >= current.approversRequired()) {
            if (current.step() >= steps.size()) {
                request.finish(ApprovalRequest.Status.APPROVED, Instant.now(clock));
                metrics.incrementRequestsApproved();
                bus.publish(new RequestApproved(UUID.randomUUID().toString(), Instant.now(clock),
                        requestId, request.getSubjectType()));
            } else {
                request.advanceStep();
                audit.record("CHAIN_STEP_ADVANCED", "APPROVAL_REQUEST", requestId,
                        "Advanced to step " + request.getCurrentStep());
            }
            requestRepository.save(request);
            metrics.setPendingRequests(requestRepository.countByStatus("PENDING"));
        }
        idempotency.complete(idemKey, requestId, 200);
        return requestView(request);
    }

    @Transactional
    public Api.RequestView cancel(String requestId) {
        ApprovalRequest request = loadRequest(requestId);
        if (request.getStatus() != ApprovalRequest.Status.PENDING) {
            throw new Problems.Conflict("Only PENDING requests can be cancelled");
        }
        request.finish(ApprovalRequest.Status.CANCELLED, Instant.now(clock));
        requestRepository.save(request);
        metrics.setPendingRequests(requestRepository.countByStatus("PENDING"));
        audit.record("APPROVAL_CANCELLED", "APPROVAL_REQUEST", requestId,
                "Cancelled by " + SecurityUtil.currentUsername());
        return requestView(request);
    }

    /** SLA escalation: extend due dates on stale PENDING requests and emit events. */
    @Transactional
    public Api.EscalationResult escalate() {
        int escalated = 0;
        for (ApprovalRequest request : requestRepository.findByStatusAndDueAtBefore(
                "PENDING", Instant.now(clock))) {
            request.extendDue(Instant.now(clock).plusSeconds(48 * 3600L));
            requestRepository.save(request);
            bus.publish(new RequestEscalated(UUID.randomUUID().toString(), Instant.now(clock),
                    request.getId(), request.getCurrentStep()));
            escalated++;
        }
        if (escalated > 0) {
            log.info("Approval escalation: {} requests extended", escalated);
        }
        return new Api.EscalationResult(escalated);
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public PageResponse<Api.RequestView> requests(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        var result = (status == null || status.isBlank())
                ? requestRepository.findAll(pr)
                : requestRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::requestView));
    }

    @Transactional(readOnly = true)
    public List<Api.DecisionView> decisions(String requestId) {
        return decisionRepository.findByRequestIdOrderByStepNoAsc(requestId).stream()
                .map(d -> new Api.DecisionView(d.getId(), requestId, d.getStepNo(),
                        d.getApproverName(), d.getDecision(), d.getNote(), d.getDecidedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Api.ChainView> chains() {
        return chainRepository.findAll().stream().map(this::chainView).toList();
    }

    @Transactional(readOnly = true)
    public List<Api.PolicyView> policies() {
        return policyRepository.findAll().stream().map(this::view).toList();
    }

    // ---------------------------------------------------------------- helpers

    private Policy loadPolicy(String id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Policy not found"));
    }

    private ApprovalChain loadChain(String id) {
        return chainRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Chain not found"));
    }

    private ApprovalRequest loadRequest(String id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Request not found"));
    }

    private Api.PolicyView view(Policy p) {
        return new Api.PolicyView(p.getId(), p.getPolicyCode(), p.getName(), p.getActiveVersionId());
    }

    private Api.ChainView chainView(ApprovalChain c) {
        return new Api.ChainView(c.getId(), c.getChainCode(), c.getName(), c.getStepsJson());
    }

    private Api.RequestView requestView(ApprovalRequest r) {
        return new Api.RequestView(r.getId(), r.getChainId(), r.getPolicyVersionId(),
                r.getSubjectType(), r.getSubjectId(), r.getStatus().name(), r.getCurrentStep(),
                r.getRequestedByName(), r.getDueAt(), r.getCreatedAt(), r.getDecidedAt());
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new Problems.BadRequest("Invalid payload");
        }
    }

    public record RequestApproved(String eventId, Instant occurredAt, String requestId,
                                  String subjectType) implements DomainEvent {
    }

    public record RequestRejected(String eventId, Instant occurredAt, String requestId,
                                  String subjectType) implements DomainEvent {
    }

    public record RequestEscalated(String eventId, Instant occurredAt, String requestId,
                                   int currentStep) implements DomainEvent {
    }
}
