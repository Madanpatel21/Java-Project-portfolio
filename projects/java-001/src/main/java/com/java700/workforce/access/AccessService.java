package com.java700.workforce.access;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.workforce.common.api.PageResponse;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.common.web.IdempotencyService;
import com.java700.workforce.evidence.EvidenceService;
import com.java700.workforce.identity.UserProfileRepository;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Access lifecycle: request → dual-control approval (segregation of duties enforced) →
 * grant creation → revocation/expiry. Every state change writes an evidence entry
 * in the same transaction.
 */
@Service
public class AccessService {

    public static final int REQUIRED_APPROVALS = 2;

    private final AccessRequestRepository requestRepository;
    private final ApprovalRepository approvalRepository;
    private final GrantRepository grantRepository;
    private final UserProfileRepository userRepository;
    private final EvidenceService evidence;
    private final AuditLogService audit;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final int recertIntervalDays;

    public AccessService(AccessRequestRepository requestRepository, ApprovalRepository approvalRepository,
                         GrantRepository grantRepository, UserProfileRepository userRepository,
                         EvidenceService evidence, AuditLogService audit, DomainEventBus bus,
                         IdempotencyService idempotency, ObjectMapper mapper, Clock clock,
                         @Value("${app.correlation.recert-interval-days:90}") int recertIntervalDays) {
        this.requestRepository = requestRepository;
        this.approvalRepository = approvalRepository;
        this.grantRepository = grantRepository;
        this.userRepository = userRepository;
        this.evidence = evidence;
        this.audit = audit;
        this.bus = bus;
        this.idempotency = idempotency;
        this.mapper = mapper;
        this.clock = clock;
        this.recertIntervalDays = recertIntervalDays;
    }

    // ---------------------------------------------------------------- requests

    @Transactional
    public AccessApi.RequestView createRequest(String requesterId, AccessApi.CreateRequest req) {
        userRepository.findById(req.subjectUserId())
                .orElseThrow(() -> new Problems.NotFound("Subject user not found"));
        String rolesJson = toJson(req.roles());
        AccessRequest request = new AccessRequest(UUID.randomUUID().toString(), requesterId,
                req.subjectUserId(), req.resourceType(), req.resourceName(), rolesJson,
                req.justification(), Instant.now(clock));
        requestRepository.save(request);
        evidence.append("ACCESS_REQUEST", request.getId(), "ACCESS_REQUESTED", SecurityUtil.currentUsername(),
                Map.of("requesterId", requesterId, "subjectUserId", req.subjectUserId(),
                        "resourceName", req.resourceName(), "roles", req.roles(),
                        "justification", req.justification() == null ? "" : req.justification()));
        audit.record("ACCESS_REQUESTED", "ACCESS_REQUEST", request.getId(),
                "Requested " + req.roles() + " on " + req.resourceName());
        return AccessApi.RequestView.from(request);
    }

    @Transactional
    public AccessApi.RequestView decide(String requestId, boolean approve, String comment, String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "ACCESS_DECISION");
        AccessRequest request;
        try {
            request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new Problems.NotFound("Access request not found"));
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
        if (existing != null) {
            // replay of a completed decision → return the current state
            return AccessApi.RequestView.from(request);
        }
        if (request.getStatus() != AccessRequest.Status.PENDING) {
            throw new Problems.Conflict("Request is already decided");
        }
        String approverId = SecurityUtil.currentUserId();
        String approverName = SecurityUtil.currentUsername();
        if (approverId.equals(request.getRequesterId())) {
            throw new Problems.Conflict(
                    "Segregation of duties: the requester may not approve their own request");
        }
        List<Approval> approvals = approvalRepository.findByAccessRequestId(requestId);
        boolean alreadyDecided = approvals.stream().anyMatch(a -> a.getApproverId().equals(approverId));
        if (alreadyDecided) {
            throw new Problems.Conflict("Dual control: this approver has already decided");
        }
        Approval approval = new Approval(UUID.randomUUID().toString(), requestId, approverId,
                approverName, approve ? "APPROVE" : "REJECT", comment, Instant.now(clock));
        approvalRepository.save(approval);
        evidence.append("ACCESS_REQUEST", requestId, approve ? "ACCESS_APPROVED" : "ACCESS_REJECTED",
                approverName, Map.of("approverId", approverId, "comment", comment == null ? "" : comment));

        if (!approve) {
            request.markDecided(AccessRequest.Status.REJECTED, Instant.now(clock), approverName, comment);
            requestRepository.save(request);
            audit.record("ACCESS_REJECTED", "ACCESS_REQUEST", requestId, "Rejected by " + approverName);
            idempotency.complete(idempotencyKey, requestId, 200);
            return AccessApi.RequestView.from(request);
        }
        long distinctApprovers = approvals.stream().map(Approval::getApproverId).distinct().count() + 1;
        if (distinctApprovers >= REQUIRED_APPROVALS) {
            Instant now = Instant.now(clock);
            request.markDecided(AccessRequest.Status.APPROVED, now, approverName, comment);
            requestRepository.save(request);
            Instant recertDue = now.plus(recertIntervalDays, ChronoUnit.DAYS);
            Grant grant = new Grant(UUID.randomUUID().toString(), request.getSubjectUserId(),
                    request.getResourceType(), request.getResourceName(), request.getRolesJson(),
                    now, null, recertDue);
            grantRepository.save(grant);
            evidence.append("ACCESS_GRANT", grant.getId(), "GRANT_CREATED", approverName,
                    Map.of("userId", grant.getUserId(), "resourceName", grant.getResourceName(),
                            "roles", grant.roles(), "recertDueAt", recertDue.toString(),
                            "requestId", requestId));
            audit.record("GRANT_CREATED", "ACCESS_GRANT", grant.getId(),
                    "Grant for " + grant.getResourceName() + " via request " + requestId);
            bus.publish(new GrantCreatedEvent(UUID.randomUUID().toString(), Instant.now(clock),
                    grant.getId(), grant.getUserId(), grant.getResourceName()));
        }
        idempotency.complete(idempotencyKey, requestId, 200);
        return AccessApi.RequestView.from(request);
    }

    // ---------------------------------------------------------------- grants

    @Transactional
    public AccessApi.GrantView revoke(String grantId, String reason) {
        Grant grant = grantRepository.findByIdAndStatus(grantId, "ACTIVE")
                .orElseThrow(() -> new Problems.NotFound("Active grant not found"));
        grant.revoke(Instant.now(clock), SecurityUtil.currentUsername(), reason);
        grantRepository.save(grant);
        evidence.append("ACCESS_GRANT", grantId, "GRANT_REVOKED", SecurityUtil.currentUsername(),
                Map.of("reason", reason == null ? "" : reason));
        audit.record("GRANT_REVOKED", "ACCESS_GRANT", grantId, "Revoked: " + reason);
        return AccessApi.GrantView.from(grant);
    }

    /** Scheduled sweep: expire grants past their expiry window. */
    @Transactional
    public int expireDueGrants() {
        List<Grant> due = grantRepository.findByStatusAndExpiresAtBefore("ACTIVE", Instant.now(clock));
        for (Grant g : due) {
            g.expire(Instant.now(clock));
            grantRepository.save(g);
            evidence.append("ACCESS_GRANT", g.getId(), "GRANT_EXPIRED", "system", Map.of());
        }
        return due.size();
    }

    @Transactional(readOnly = true)
    public PageResponse<AccessApi.GrantView> grantsFor(String userId, int page, int size) {
        return PageResponse.from(grantRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by("grantedAt").descending()))
                .map(AccessApi.GrantView::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AccessApi.RequestView> pendingRequests(int page, int size) {
        return PageResponse.from(requestRepository.findByStatus("PENDING",
                PageRequest.of(page, size, Sort.by("createdAt"))).map(AccessApi.RequestView::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<AccessApi.RequestView> requestsFor(String userId, int page, int size) {
        return PageResponse.from(requestRepository.findBySubjectUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(AccessApi.RequestView::from));
    }

    // ---------------------------------------------------------------- helpers

    static List<String> parseRoles(String rolesJson) {
        try {
            return new ObjectMapper().readValue(rolesJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    private String toJson(List<String> roles) {
        try {
            return mapper.writeValueAsString(roles);
        } catch (JsonProcessingException e) {
            throw new Problems.BadRequest("Invalid roles payload");
        }
    }

    public record GrantCreatedEvent(String eventId, Instant occurredAt, String grantId,
                                    String userId, String resourceName)
            implements com.java700.workforce.messaging.DomainEvent {
    }
}
