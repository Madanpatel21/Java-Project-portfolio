package com.java700.stewardship.interventions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.common.web.IdempotencyService;
import com.java700.stewardship.messaging.DomainEvent;
import com.java700.stewardship.messaging.DomainEventBus;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.observability.Metrics;
import com.java700.stewardship.prescriptions.PrescriptionService;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Intervention lifecycle: pharmacist proposes a stewardship change; the prescriber accepts
 * (therapy is modified transactionally) or rejects with a mandatory reason.
 */
@Service
public class InterventionService {

    private static final Set<String> TYPES = Set.of(
            "STOP", "IV_TO_PO", "DOSE_CHANGE", "DE_ESCALATE", "DURATION_CHANGE", "RENAL_ADJUST",
            "ADD_THERAPY");

    private final InterventionRepository repository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionService prescriptionService;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final Clock clock;

    public InterventionService(InterventionRepository repository,
                               PrescriptionRepository prescriptionRepository,
                               PrescriptionService prescriptionService, DomainEventBus bus,
                               IdempotencyService idempotency, AuditLogService audit,
                               Metrics metrics, ObjectMapper mapper, Clock clock) {
        this.repository = repository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionService = prescriptionService;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Transactional
    public InterventionApi.InterventionView propose(InterventionApi.ProposeRequest req,
                                                    String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "INTERVENTION");
        if (existing != null) {
            return view(load(existing));
        }
        try {
            if (!TYPES.contains(req.type())) {
                throw new Problems.BadRequest("Unknown intervention type: " + req.type());
            }
            Prescription rx = prescriptionRepository.findById(req.prescriptionId())
                    .orElseThrow(() -> new Problems.NotFound("Prescription not found"));
            if (rx.getStatus() != Prescription.Status.ACTIVE) {
                throw new Problems.Conflict("Interventions require an ACTIVE prescription");
            }
            String detailJson = toJson(req.detail() == null ? Map.of() : req.detail());
            Intervention i = new Intervention(UUID.randomUUID().toString(), req.prescriptionId(),
                    req.reviewTaskId(), req.type(), detailJson, req.reason(),
                    SecurityUtil.currentUsername(), Instant.now(clock));
            repository.save(i);
            metrics.incrementInterventionsProposed();
            audit.record("INTERVENTION_PROPOSED", "INTERVENTION", i.getId(),
                    req.type() + " proposed for prescription " + req.prescriptionId() + ": " + req.reason());
            bus.publish(new InterventionProposed(UUID.randomUUID().toString(), Instant.now(clock),
                    i.getId(), req.prescriptionId(), req.type()));
            idempotency.complete(idempotencyKey, i.getId(), 201);
            return view(i);
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public InterventionApi.InterventionView decide(String id, boolean accept, String response) {
        Intervention i = load(id);
        if (i.getStatus() != Intervention.Status.PROPOSED) {
            throw new Problems.Conflict("Intervention is already decided");
        }
        if (!accept) {
            if (response == null || response.isBlank()) {
                throw new Problems.BadRequest("A rejection requires a clinical reason");
            }
            i.decide(Intervention.Status.REJECTED, SecurityUtil.currentUsername(),
                    Instant.now(clock), response);
            repository.save(i);
            audit.record("INTERVENTION_REJECTED", "INTERVENTION", id, response);
            return view(i);
        }
        i.decide(Intervention.Status.ACCEPTED, SecurityUtil.currentUsername(),
                Instant.now(clock), response);
        repository.save(i);
        prescriptionService.applyIntervention(i.getPrescriptionId(), i.getType(),
                parseDetail(i.getDetailJson()));
        metrics.incrementInterventionsAccepted();
        audit.record("INTERVENTION_ACCEPTED", "INTERVENTION", id,
                "Accepted and applied by " + SecurityUtil.currentUsername());
        bus.publish(new InterventionAccepted(UUID.randomUUID().toString(), Instant.now(clock),
                id, i.getPrescriptionId(), i.getType()));
        return view(i);
    }

    /** Expiry sweep: PROPOSED interventions older than the clinical window. */
    @Transactional
    public int expireStale() {
        List<Intervention> stale = repository.findByStatusAndProposedAtBefore(
                "PROPOSED", Instant.now(clock).minus(Intervention.EXPIRY_DAYS, ChronoUnit.DAYS));
        for (Intervention i : stale) {
            i.expire(Instant.now(clock));
            repository.save(i);
        }
        return stale.size();
    }

    @Transactional(readOnly = true)
    public List<InterventionApi.InterventionView> forPrescription(String prescriptionId) {
        return repository.findByPrescriptionIdOrderByProposedAtDesc(prescriptionId).stream()
                .map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InterventionApi.InterventionView> open() {
        return repository.findByStatus("PROPOSED").stream().map(this::view).toList();
    }

    private Intervention load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Intervention not found"));
    }

    private InterventionApi.InterventionView view(Intervention i) {
        return InterventionApi.InterventionView.from(i, parseDetail(i.getDetailJson()));
    }

    private Map<String, Object> parseDetail(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String toJson(Map<String, Object> detail) {
        try {
            return mapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            throw new Problems.BadRequest("Invalid intervention detail");
        }
    }

    public record InterventionProposed(String eventId, Instant occurredAt, String interventionId,
                                       String prescriptionId, String type) implements DomainEvent {
    }

    public record InterventionAccepted(String eventId, Instant occurredAt, String interventionId,
                                       String prescriptionId, String type) implements DomainEvent {
    }
}
