package com.java700.contracts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.contracts.common.api.PageResponse;
import com.java700.contracts.common.api.Problems;
import com.java700.contracts.common.audit.AuditLogService;
import com.java700.contracts.common.web.IdempotencyService;
import com.java700.contracts.domain.Approval;
import com.java700.contracts.domain.ApprovalRepository;
import com.java700.contracts.domain.Contract;
import com.java700.contracts.domain.ContractRepository;
import com.java700.contracts.domain.ContractVersion;
import com.java700.contracts.domain.ContractVersionRepository;
import com.java700.contracts.domain.Obligation;
import com.java700.contracts.domain.ObligationEvent;
import com.java700.contracts.domain.ObligationEventRepository;
import com.java700.contracts.domain.ObligationRepository;
import com.java700.contracts.matching.ContractDiff;
import com.java700.contracts.messaging.DomainEvent;
import com.java700.contracts.messaging.DomainEventBus;
import com.java700.contracts.observability.Metrics;
import com.java700.contracts.security.Roles;
import com.java700.contracts.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Contract lifecycle + obligation engine:
 * <ul>
 *   <li>versioned clauses with clause-level clearance (role-based redaction)</li>
 *   <li>activation through a four-eyes approval chain (LEGAL + CONTRACT_MANAGER)</li>
 *   <li>obligation SLA state machine with notifications, acknowledgement, completion,
 *       four-eyes waiver, overdue detection and recurrence</li>
 * </ul>
 */
@Service
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final ContractVersionRepository versionRepository;
    private final ObligationRepository obligationRepository;
    private final ObligationEventRepository eventRepository;
    private final ApprovalRepository approvalRepository;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final Clock clock;

    public ContractService(ContractRepository contractRepository,
                           ContractVersionRepository versionRepository,
                           ObligationRepository obligationRepository,
                           ObligationEventRepository eventRepository,
                           ApprovalRepository approvalRepository,
                           DomainEventBus bus, IdempotencyService idempotency,
                           AuditLogService audit, Metrics metrics,
                           ObjectMapper mapper, Clock clock) {
        this.contractRepository = contractRepository;
        this.versionRepository = versionRepository;
        this.obligationRepository = obligationRepository;
        this.eventRepository = eventRepository;
        this.approvalRepository = approvalRepository;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- contracts

    @Transactional
    public Api.ContractView create(Api.CreateContractRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "CONTRACT");
        if (existing != null) {
            return view(load(existing));
        }
        try {
            if (contractRepository.findByContractNo(req.contractNo()).isPresent()) {
                throw new Problems.Conflict("Contract number already exists");
            }
            Contract contract = new Contract(UUID.randomUUID().toString(), req.contractNo(),
                    req.title(), req.counterparty(), SecurityUtil.currentUserId(),
                    SecurityUtil.currentUsername(), req.effectiveFrom(), req.effectiveTo(),
                    Instant.now(clock));
            contractRepository.save(contract);
            metrics.incrementContractsCreated();
            audit.record("CONTRACT_CREATED", "CONTRACT", contract.getId(), req.contractNo());
            idempotency.complete(idemKey, contract.getId(), 201);
            return view(contract);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.VersionView addVersion(String contractId, Api.CreateVersionRequest req,
                                      String idemKey) {
        String existing = idempotency.begin(idemKey, "CONTRACT_VERSION");
        if (existing != null) {
            return versionView(versionRepository.findById(existing)
                    .orElseThrow(() -> new Problems.NotFound("Version not found")));
        }
        try {
            Contract contract = load(contractId);
            if (contract.getStatus() == Contract.Status.TERMINATED) {
                throw new Problems.Conflict("Terminated contracts are immutable");
            }
            int next = versionRepository.findByContractIdOrderByVersionNoDesc(contractId)
                    .stream().findFirst().map(ContractVersion::getVersionNo).orElse(0) + 1;
            ContractVersion version = new ContractVersion(UUID.randomUUID().toString(),
                    contractId, next, req.contentJson(), SecurityUtil.currentUsername(),
                    Instant.now(clock));
            versionRepository.save(version);
            audit.record("CONTRACT_VERSION_ADDED", "CONTRACT", contractId,
                    "Version " + next + " by " + SecurityUtil.currentUsername());
            idempotency.complete(idemKey, version.getId(), 201);
            return versionView(version);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    /** Four-eyes activation: LEGAL + CONTRACT_MANAGER must both approve. */
    @Transactional
    public Api.ContractView activate(String contractId, boolean approve, String note) {
        Contract contract = load(contractId);
        if (contract.getStatus() != Contract.Status.DRAFT) {
            throw new Problems.Conflict("Only DRAFT contracts can be activated");
        }
        String role = currentRole();
        if (!Roles.LEGAL_COUNSEL.equals(role) && !Roles.CONTRACT_MANAGER.equals(role)
                && !Roles.ADMIN.equals(role)) {
            throw new Problems.Conflict("Activation requires LEGAL_COUNSEL or CONTRACT_MANAGER");
        }
        List<Approval> approvals = approvalRepository.findByTargetTypeAndTargetId(
                "CONTRACT_ACTIVATION", contractId);
        boolean already = approvals.stream().anyMatch(a ->
                a.getApproverRole().equals(role) || a.getApproverId().equals(SecurityUtil.currentUserId()));
        if (already) {
            throw new Problems.Conflict("This role has already decided");
        }
        if (!approve) {
            approvalRepository.save(new Approval(UUID.randomUUID().toString(),
                    "CONTRACT_ACTIVATION", contractId, role, SecurityUtil.currentUserId(),
                    SecurityUtil.currentUsername(), "REJECT", note, Instant.now(clock)));
            audit.record("CONTRACT_ACTIVATION_REJECTED", "CONTRACT", contractId, note);
            return view(contract);
        }
        approvalRepository.save(new Approval(UUID.randomUUID().toString(),
                "CONTRACT_ACTIVATION", contractId, role, SecurityUtil.currentUserId(),
                SecurityUtil.currentUsername(), "APPROVE", note, Instant.now(clock)));
        long distinctRoles = approvals.stream().map(Approval::getApproverRole).distinct().count() + 1;
        boolean hasLegal = approvals.stream().anyMatch(a ->
                Roles.LEGAL_COUNSEL.equals(a.getApproverRole()) || Roles.LEGAL_COUNSEL.equals(role));
        boolean hasManager = approvals.stream().anyMatch(a ->
                Roles.CONTRACT_MANAGER.equals(a.getApproverRole())
                        || Roles.CONTRACT_MANAGER.equals(role));
        if (distinctRoles >= 2 && hasLegal && hasManager) {
            contract.activate();
            contractRepository.save(contract);
            bus.publish(new ContractActivated(UUID.randomUUID().toString(), Instant.now(clock),
                    contractId, contract.getContractNo()));
            audit.record("CONTRACT_ACTIVATED", "CONTRACT", contractId,
                    "Four-eyes activation complete");
        }
        return view(contract);
    }

    @Transactional
    public Api.ContractView terminate(String contractId) {
        Contract contract = load(contractId);
        if (contract.getStatus() != Contract.Status.ACTIVE) {
            throw new Problems.Conflict("Only ACTIVE contracts can be terminated");
        }
        contract.terminate();
        contractRepository.save(contract);
        audit.record("CONTRACT_TERMINATED", "CONTRACT", contractId,
                "Terminated by " + SecurityUtil.currentUsername());
        return view(contract);
    }

    /** Clause view with role-based redaction by sensitivity. */
    @Transactional(readOnly = true)
    public JsonNode clauses(String contractId, int versionNo) {
        ContractVersion version = versionNo > 0
                ? versionRepository.findByContractIdAndVersionNo(contractId, versionNo)
                        .orElseThrow(() -> new Problems.NotFound("Version not found"))
                : versionRepository.findByContractIdOrderByVersionNoDesc(contractId).stream()
                        .findFirst().orElseThrow(() -> new Problems.NotFound("No versions yet"));
        try {
            JsonNode root = mapper.readTree(version.getContentJson());
            int clearance = Roles.clearance(currentRole());
            List<JsonNode> redacted = new ArrayList<>();
            for (JsonNode clause : root.path("clauses")) {
                int sensitivity = clause.path("sensitivity").asInt(0);
                if (sensitivity > clearance) {
                    redacted.add(mapper.createObjectNode()
                            .put("number", clause.path("number").asText())
                            .put("title", clause.path("title").asText())
                            .put("text", "[REDACTED — clearance level " + clearance + "]")
                            .put("sensitivity", sensitivity)
                            .put("redacted", true));
                } else {
                    redacted.add(clause);
                }
            }
            var out = mapper.createObjectNode();
            var arr = out.putArray("clauses");
            redacted.forEach(arr::add);
            out.put("contractId", contractId);
            out.put("versionNo", version.getVersionNo());
            out.put("viewerClearance", clearance);
            return out;
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new Problems.BadRequest("Malformed contract content JSON");
        }
    }

    @Transactional(readOnly = true)
    public List<ContractDiff.Change> diff(String contractId, int v1, int v2) {
        ContractVersion a = versionRepository.findByContractIdAndVersionNo(contractId, v1)
                .orElseThrow(() -> new Problems.NotFound("Version " + v1 + " not found"));
        ContractVersion b = versionRepository.findByContractIdAndVersionNo(contractId, v2)
                .orElseThrow(() -> new Problems.NotFound("Version " + v2 + " not found"));
        return ContractDiff.diff(a.getContentJson(), b.getContentJson());
    }

    // ---------------------------------------------------------------- obligations

    @Transactional
    public Api.ObligationView createObligation(Api.CreateObligationRequest req, String idemKey) {
        String existing = idempotency.begin(idemKey, "OBLIGATION");
        if (existing != null) {
            return obligationView(loadObligation(existing));
        }
        try {
            // validate first: reject malformed input before any data access
            Obligation.Type type = parseType(req.type());
            Obligation.Criticality criticality = parseCriticality(req.criticality());
            Contract contract = load(req.contractId());
            Obligation obligation = new Obligation(UUID.randomUUID().toString(), contract.getId(),
                    req.sourceClause(), type, req.title(), req.description(),
                    req.dueAt(), req.windowBeforeDays(), req.repeatIntervalDays(),
                    criticality, req.assignedTo(), Instant.now(clock));
            obligationRepository.save(obligation);
            event(obligation, "CREATED", "Obligation created by " + SecurityUtil.currentUsername());
            metrics.incrementObligationsCreated();
            metrics.setOpenObligations(obligationRepository.countByStatus("OPEN"));
            audit.record("OBLIGATION_CREATED", "OBLIGATION", obligation.getId(),
                    req.title() + " due " + req.dueAt());
            idempotency.complete(idemKey, obligation.getId(), 201);
            return obligationView(obligation);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.ObligationView acknowledge(String obligationId) {
        Obligation o = loadObligation(obligationId);
        if (o.getStatus() != Obligation.Status.NOTIFIED
                && o.getStatus() != Obligation.Status.OPEN) {
            throw new Problems.Conflict("Only OPEN/NOTIFIED obligations can be acknowledged");
        }
        o.acknowledge(Instant.now(clock));
        obligationRepository.save(o);
        event(o, "ACKNOWLEDGED", "Acknowledged by " + SecurityUtil.currentUsername());
        audit.record("OBLIGATION_ACKNOWLEDGED", "OBLIGATION", obligationId,
                "By " + SecurityUtil.currentUsername());
        return obligationView(o);
    }

    @Transactional
    public Api.ObligationView complete(String obligationId) {
        Obligation o = loadObligation(obligationId);
        if (o.getStatus() == Obligation.Status.COMPLETED) {
            throw new Problems.Conflict("Already completed");
        }
        o.complete(Instant.now(clock));
        obligationRepository.save(o);
        event(o, "COMPLETED", "Completed by " + SecurityUtil.currentUsername());
        metrics.incrementObligationsCompleted();
        audit.record("OBLIGATION_COMPLETED", "OBLIGATION", obligationId,
                "By " + SecurityUtil.currentUsername());
        if (o.getRepeatIntervalDays() != null) {
            Obligation next = new Obligation(UUID.randomUUID().toString(), o.getContractId(),
                    o.getSourceClause(), o.getType(), o.getTitle(), o.getDescription(),
                    o.getDueAt().plus(o.getRepeatIntervalDays(), ChronoUnit.DAYS),
                    o.getWindowBeforeDays(), o.getRepeatIntervalDays(), o.getCriticality(),
                    o.getAssignedTo(), Instant.now(clock));
            obligationRepository.save(next);
            event(next, "RECURRED", "Recurring obligation from " + o.getId());
            audit.record("OBLIGATION_RECURRED", "OBLIGATION", next.getId(),
                    "Recurrence of " + obligationId);
        }
        return obligationView(o);
    }

    /** Four-eyes waiver: LEGAL or ADMIN only, audited. */
    @Transactional
    public Api.ObligationView waive(String obligationId, String reason, String idemKey) {
        String existing = idempotency.begin(idemKey, "OBLIGATION_WAIVER");
        Obligation o;
        try {
            o = loadObligation(obligationId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
        if (existing != null) {
            return obligationView(o);
        }
        if (o.getStatus() == Obligation.Status.COMPLETED
                || o.getStatus() == Obligation.Status.WAIVED) {
            throw new Problems.Conflict("Cannot waive a completed/waived obligation");
        }
        String role = currentRole();
        if (!Roles.LEGAL_COUNSEL.equals(role) && !Roles.ADMIN.equals(role)) {
            throw new Problems.Conflict("Waivers require LEGAL_COUNSEL (four-eyes)");
        }
        o.waive(SecurityUtil.currentUsername(), Instant.now(clock), reason);
        obligationRepository.save(o);
        event(o, "WAIVED", "Waived by " + SecurityUtil.currentUsername() + ": " + reason);
        metrics.incrementObligationsWaived();
        audit.record("OBLIGATION_WAIVED", "OBLIGATION", obligationId, reason);
        idempotency.complete(idemKey, obligationId, 200);
        return obligationView(o);
    }

    /** SLA scan: notify obligations entering their window; mark overdue past due date. */
    @Transactional
    public Api.ScanResult scan() {
        Instant now = Instant.now(clock);
        int notified = 0;
        int overdue = 0;
        for (Obligation o : obligationRepository.findByStatusInAndDueAtBefore(
                List.of("OPEN", "NOTIFIED", "ACKNOWLEDGED"), now)) {
            if (o.getStatus() != Obligation.Status.OVERDUE) {
                o.markOverdue(now);
                obligationRepository.save(o);
                event(o, "OVERDUE", "Obligation overdue as of " + now);
                bus.publish(new ObligationOverdue(UUID.randomUUID().toString(), now,
                        o.getId(), o.getContractId(), o.getTitle()));
                overdue++;
                continue;
            }
        }
        for (Obligation o : obligationRepository.findByStatus("OPEN")) {
            Instant notifyFrom = o.getDueAt().minus(o.getWindowBeforeDays(), ChronoUnit.DAYS);
            if (!now.isBefore(notifyFrom) && now.isBefore(o.getDueAt())) {
                o.notify(now);
                obligationRepository.save(o);
                event(o, "NOTIFIED", "Reminder within " + o.getWindowBeforeDays() + "d window");
                metrics.incrementRemindersSent();
                notified++;
            }
        }
        metrics.setOpenObligations(obligationRepository.countByStatus("OPEN"));
        if (notified + overdue > 0) {
            log.info("Obligation scan: {} notified, {} overdue", notified, overdue);
        }
        return new Api.ScanResult(notified, overdue);
    }

    @Transactional(readOnly = true)
    public PageResponse<Api.ObligationView> obligations(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("dueAt"));
        var result = (status == null || status.isBlank())
                ? obligationRepository.findAll(pr)
                : obligationRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::obligationView));
    }

    @Transactional(readOnly = true)
    public List<Api.ObligationView> obligationsFor(String contractId) {
        return obligationRepository.findByContractIdOrderByDueAtAsc(contractId).stream()
                .map(this::obligationView).toList();
    }

    // ---------------------------------------------------------------- queries / helpers

    @Transactional(readOnly = true)
    public PageResponse<Api.ContractView> contracts(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        var result = (status == null || status.isBlank())
                ? contractRepository.findAll(pr)
                : contractRepository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(this::view));
    }

    @Transactional(readOnly = true)
    public List<Api.VersionView> versions(String contractId) {
        return versionRepository.findByContractIdOrderByVersionNoDesc(contractId).stream()
                .map(this::versionView).toList();
    }

    private Contract load(String id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Contract not found"));
    }

    private Obligation loadObligation(String id) {
        return obligationRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Obligation not found"));
    }

    private void event(Obligation o, String type, String detail) {
        eventRepository.save(new ObligationEvent(UUID.randomUUID().toString(), o.getId(),
                type, detail, Instant.now(clock)));
    }

    private String currentRole() {
        return SecurityUtil.hasRole(Roles.LEGAL_COUNSEL) ? Roles.LEGAL_COUNSEL
                : SecurityUtil.hasRole(Roles.CONTRACT_MANAGER) ? Roles.CONTRACT_MANAGER
                : SecurityUtil.hasRole(Roles.ADMIN) ? Roles.ADMIN
                : SecurityUtil.hasRole(Roles.BUSINESS_OWNER) ? Roles.BUSINESS_OWNER
                : SecurityUtil.hasRole(Roles.FINANCE) ? Roles.FINANCE
                : SecurityUtil.hasRole(Roles.AUDITOR) ? Roles.AUDITOR : "UNKNOWN";
    }

    private static Obligation.Type parseType(String t) {
        try {
            return Obligation.Type.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Problems.BadRequest("Unknown obligation type: " + t);
        }
    }

    private static Obligation.Criticality parseCriticality(String c) {
        try {
            return Obligation.Criticality.valueOf(c.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Problems.BadRequest("Unknown criticality: " + c);
        }
    }

    private Api.ContractView view(Contract c) {
        return new Api.ContractView(c.getId(), c.getContractNo(), c.getTitle(),
                c.getCounterparty(), c.getOwnerName(), c.getStatus().name(),
                c.getEffectiveFrom(), c.getEffectiveTo(), c.getCreatedAt());
    }

    private Api.VersionView versionView(ContractVersion v) {
        return new Api.VersionView(v.getId(), v.getContractId(), v.getVersionNo(),
                v.getContentJson(), v.getCreatedBy(), v.getCreatedAt());
    }

    private Api.ObligationView obligationView(Obligation o) {
        return new Api.ObligationView(o.getId(), o.getContractId(), o.getSourceClause(),
                o.getType().name(), o.getTitle(), o.getDescription(), o.getDueAt(),
                o.getWindowBeforeDays(), o.getRepeatIntervalDays(), o.getCriticality().name(),
                o.getStatus().name(), o.getAssignedTo(), o.getAcknowledgedAt(),
                o.getCompletedAt(), o.getWaivedAt(), o.getWaivedBy(), o.getWaiverReason(),
                o.getNotifiedAt(), o.getOverdueAt(), o.getCreatedAt());
    }

    public record ContractActivated(String eventId, Instant occurredAt, String contractId,
                                    String contractNo) implements DomainEvent {
    }

    public record ObligationOverdue(String eventId, Instant occurredAt, String obligationId,
                                    String contractId, String title) implements DomainEvent {
    }
}
