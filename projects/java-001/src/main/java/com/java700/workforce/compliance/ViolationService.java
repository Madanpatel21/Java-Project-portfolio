package com.java700.workforce.compliance;


import com.java700.workforce.common.api.PageResponse;
import com.java700.workforce.common.api.Problems;
import com.java700.workforce.common.audit.AuditLogService;
import com.java700.workforce.evidence.EvidenceService;
import com.java700.workforce.messaging.DomainEvent;
import com.java700.workforce.messaging.DomainEventBus;
import com.java700.workforce.messaging.DomainEventHandler;
import com.java700.workforce.observability.Metrics;
import com.java700.workforce.policy.ViolationCandidate;
import com.java700.workforce.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Violation lifecycle with evidence linkage and notification fan-out. */
@Service
public class ViolationService {

    private static final Logger log = LoggerFactory.getLogger(ViolationService.class);

    private final ViolationRepository repository;
    private final EvidenceService evidence;
    private final AuditLogService audit;
    private final DomainEventBus bus;
    private final Metrics metrics;
    private final Clock clock;

    public ViolationService(ViolationRepository repository, EvidenceService evidence,
                            AuditLogService audit, DomainEventBus bus, Metrics metrics, Clock clock) {
        this.repository = repository;
        this.evidence = evidence;
        this.audit = audit;
        this.bus = bus;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Creates a violation if none is open for the same user/policy/rule/resource (dedup). */
    @Transactional
    public Violation detect(ViolationCandidate candidate) {
        boolean exists = repository.findByUserIdAndPolicyCodeAndRuleTypeAndStatusIn(
                candidate.userId(), candidate.policyCode(), candidate.ruleType().name(),
                List.of("OPEN", "ACKNOWLEDGED")).isPresent();
        if (exists) {
            return null;
        }
        Violation v = new Violation(UUID.randomUUID().toString(), candidate.userId(),
                candidate.policyCode(), candidate.ruleType().name(), candidate.severity().name(),
                candidate.description(), Instant.now(clock));
        repository.save(v);
        var entry = evidence.append("VIOLATION", v.getId(), "VIOLATION_DETECTED", "correlation-engine",
                Map.of("userId", v.getUserId(), "policyCode", v.getPolicyCode(),
                        "ruleType", v.getRuleType(), "severity", v.getSeverity(),
                        "description", v.getDescription()));
        v.linkEvidence(entry.getSeq());
        repository.save(v);
        audit.record("VIOLATION_DETECTED", "VIOLATION", v.getId(), v.getDescription());
        metrics.incrementViolationsDetected();
        metrics.setOpenViolations(repository.countByStatus("OPEN"));
        bus.publish(new ViolationDetected(UUID.randomUUID().toString(), Instant.now(clock),
                v.getId(), v.getUserId(), v.getSeverity(), v.getDescription()));
        return v;
    }

    @Transactional
    public ComplianceApi.ViolationView acknowledge(String id) {
        Violation v = load(id);
        if (v.getStatus() != Violation.Status.OPEN) {
            throw new Problems.Conflict("Only OPEN violations can be acknowledged");
        }
        v.acknowledge(Instant.now(clock));
        repository.save(v);
        evidence.append("VIOLATION", id, "VIOLATION_ACKNOWLEDGED", SecurityUtil.currentUsername(), Map.of());
        audit.record("VIOLATION_ACKNOWLEDGED", "VIOLATION", id, "Acknowledged");
        return ComplianceApi.ViolationView.from(v);
    }

    @Transactional
    public ComplianceApi.ViolationView remediate(String id, String note) {
        Violation v = load(id);
        if (v.getStatus() == Violation.Status.CLOSED) {
            throw new Problems.Conflict("CLOSED violations cannot be remediated");
        }
        v.remediate(Instant.now(clock), note);
        repository.save(v);
        evidence.append("VIOLATION", id, "VIOLATION_REMEDIATED", SecurityUtil.currentUsername(),
                Map.of("note", note == null ? "" : note));
        audit.record("VIOLATION_REMEDIATED", "VIOLATION", id, "Remediated: " + note);
        return ComplianceApi.ViolationView.from(v);
    }

    @Transactional
    public ComplianceApi.ViolationView close(String id) {
        Violation v = load(id);
        if (v.getStatus() != Violation.Status.REMEDIATED) {
            throw new Problems.Conflict("Only REMEDIATED violations can be closed");
        }
        v.close(Instant.now(clock));
        repository.save(v);
        evidence.append("VIOLATION", id, "VIOLATION_CLOSED", SecurityUtil.currentUsername(), Map.of());
        audit.record("VIOLATION_CLOSED", "VIOLATION", id, "Closed");
        metrics.setOpenViolations(repository.countByStatus("OPEN"));
        return ComplianceApi.ViolationView.from(v);
    }

    @Transactional(readOnly = true)
    public PageResponse<ComplianceApi.ViolationView> list(String status, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("detectedAt").descending());
        var result = (status == null || status.isBlank())
                ? repository.findAll(pr)
                : repository.findByStatus(status.toUpperCase(), pr);
        return PageResponse.from(result.map(ComplianceApi.ViolationView::from));
    }

    @Transactional(readOnly = true)
    public long openCount() {
        return repository.countByStatus("OPEN");
    }

    private Violation load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Violation not found"));
    }

    /** Marks violations as notified when the broker (or direct bus) delivers the event. */
    @Component
    public static class ViolationNotifier implements DomainEventHandler<ViolationDetected> {

        private final ViolationRepository repository;
        private final Clock clock;

        public ViolationNotifier(ViolationRepository repository, Clock clock) {
            this.repository = repository;
            this.clock = clock;
        }

        @Override
        public Class<ViolationDetected> supportedType() {
            return ViolationDetected.class;
        }

        @Override
        @Transactional
        public void handle(ViolationDetected event) {
            repository.findById(event.violationId()).ifPresent(v -> {
                if (v.getNotifiedAt() == null) {
                    v.markNotified(Instant.now(clock));
                    repository.save(v);
                    log.info("Violation {} notified (severity {})", event.violationId(), event.severity());
                }
            });
        }
    }

    public record ViolationDetected(String eventId, Instant occurredAt, String violationId,
                                    String userId, String severity, String description)
            implements DomainEvent {
    }
}
