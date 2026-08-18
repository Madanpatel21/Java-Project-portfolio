package com.java700.stewardship.prescriptions;

import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.common.web.IdempotencyService;
import com.java700.stewardship.guidelines.GuidelineRepository;
import com.java700.stewardship.messaging.DomainEvent;
import com.java700.stewardship.messaging.DomainEventBus;
import com.java700.stewardship.observability.Metrics;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.prescriptions.Prescription.Status;
import com.java700.stewardship.restricted.RestrictedAuthorizationService;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prescription lifecycle. Restricted antimicrobials enter PENDING_AUTHORIZATION and require
 * an ID-physician approval (time-boxed) before becoming ACTIVE.
 */
@Service
public class PrescriptionService {

    private final PrescriptionRepository repository;
    private final DrugRepository drugRepository;
    private final PatientRepository patientRepository;
    private final GuidelineRepository guidelineRepository;
    private final RestrictedAuthorizationService restrictedAuthService;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final int restrictedAuthHours;

    public PrescriptionService(PrescriptionRepository repository, DrugRepository drugRepository,
                               PatientRepository patientRepository, GuidelineRepository guidelineRepository,
                               RestrictedAuthorizationService restrictedAuthService, DomainEventBus bus,
                               IdempotencyService idempotency, AuditLogService audit, Metrics metrics, Clock clock,
                               @Value("${app.stewardship.restricted-auth-hours:72}") int restrictedAuthHours) {
        this.repository = repository;
        this.drugRepository = drugRepository;
        this.patientRepository = patientRepository;
        this.guidelineRepository = guidelineRepository;
        this.restrictedAuthService = restrictedAuthService;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.restrictedAuthHours = restrictedAuthHours;
    }

    @Transactional
    public PrescriptionApi.CreateResponse create(PrescriptionApi.CreateRequest req, String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "PRESCRIPTION");
        if (existing != null) {
            Prescription p = get(existing);
            return new PrescriptionApi.CreateResponse(p.getId(), p.getStatus().name(),
                    p.getRestrictedAuthId() != null && p.getStatus() == Status.PENDING_AUTHORIZATION);
        }
        try {
            patientRepository.findById(req.patientId())
                    .orElseThrow(() -> new Problems.NotFound("Patient not found"));
            AntimicrobialDrug drug = drugRepository.findByCode(req.drugCode())
                    .orElseThrow(() -> new Problems.NotFound("Drug not found: " + req.drugCode()));
            if ("IV".equals(req.route()) && !drug.isIvAvailable()) {
                throw new Problems.BadRequest(drug.getName() + " is not available IV");
            }
            if ("PO".equals(req.route()) && !drug.isPoAvailable()) {
                throw new Problems.BadRequest(drug.getName() + " is not available orally");
            }
            String guidelineVersionId = guidelineRepository.findByStatus("ACTIVE")
                    .map(g -> g.getId()).orElse(null);
            Prescription rx = new Prescription(UUID.randomUUID().toString(), req.patientId(),
                    req.admissionId(), drug.getId(), req.indication(), req.route(), req.doseMg(),
                    Math.max(1, req.frequencyHours()), req.startAt(), req.empiric(),
                    SecurityUtil.currentUsername(), guidelineVersionId, Instant.now(clock));
            if (drug.isRestricted()) {
                String authId = restrictedAuthService.request(rx.getId(), req.indication());
                rx.linkAuthorization(authId);
                // stays PENDING_AUTHORIZATION until the ID physician decides
            } else {
                rx.activate();
            }
            repository.save(rx);
            metrics.incrementPrescriptions();
            audit.record("PRESCRIPTION_CREATED", "PRESCRIPTION", rx.getId(),
                    drug.getName() + " " + req.route() + " for " + req.indication()
                            + (drug.isRestricted() ? " (restricted — pre-authorization required)" : ""));
            bus.publish(new PrescriptionCreated(UUID.randomUUID().toString(), Instant.now(clock),
                    rx.getId(), rx.getPatientId(), drug.getId(), rx.isEmpiric(), drug.isRestricted()));
            idempotency.complete(idempotencyKey, rx.getId(), 201);
            return new PrescriptionApi.CreateResponse(rx.getId(), rx.getStatus().name(),
                    drug.isRestricted());
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Prescription get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Prescription not found"));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionApi.RxView> activeForPatient(String patientId) {
        return repository.findByPatientIdAndStatus(patientId, "ACTIVE").stream()
                .map(rx -> PrescriptionApi.RxView.from(rx, drugCode(rx.getDrugId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionApi.RxView> allForPatient(String patientId) {
        return repository.findAll().stream()
                .filter(rx -> rx.getPatientId().equals(patientId))
                .map(rx -> PrescriptionApi.RxView.from(rx, drugCode(rx.getDrugId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Prescription> activePrescriptions() {
        return repository.findByStatus("ACTIVE");
    }

    @Transactional
    public PrescriptionApi.RxView stop(String id) {
        Prescription rx = get(id);
        if (rx.getStatus() != Status.ACTIVE) {
            throw new Problems.Conflict("Only ACTIVE prescriptions can be stopped");
        }
        rx.stop(Instant.now(clock));
        repository.save(rx);
        audit.record("PRESCRIPTION_STOPPED", "PRESCRIPTION", id, "Stopped by "
                + SecurityUtil.currentUsername());
        bus.publish(new PrescriptionStopped(UUID.randomUUID().toString(), Instant.now(clock), id));
        return PrescriptionApi.RxView.from(rx, drugCode(rx.getDrugId()));
    }

    /** Applies an accepted intervention's therapy change. */
    @Transactional
    public void applyIntervention(String id, String type, Map<String, Object> detail) {
        Prescription rx = get(id);
        if (rx.getStatus() != Status.ACTIVE) {
            throw new Problems.Conflict("Only ACTIVE prescriptions can be modified by interventions");
        }
        switch (type) {
            case "IV_TO_PO" -> rx.applyRouteChange("PO",
                    new java.math.BigDecimal(detail.getOrDefault("doseMg", rx.getDoseMg()).toString()),
                    Integer.parseInt(detail.getOrDefault("frequencyHours", rx.getFrequencyHours()).toString()));
            case "DOSE_CHANGE", "RENAL_ADJUST" -> rx.applyDoseChange(
                    new java.math.BigDecimal(detail.getOrDefault("doseMg", rx.getDoseMg()).toString()),
                    Integer.parseInt(detail.getOrDefault("frequencyHours", rx.getFrequencyHours()).toString()));
            case "STOP" -> rx.stop(Instant.now(clock));
            default -> throw new Problems.BadRequest("Unsupported intervention type for application: " + type);
        }
        repository.save(rx);
        audit.record("PRESCRIPTION_MODIFIED", "PRESCRIPTION", id,
                "Intervention applied: " + type);
    }

    /** Expiry sweep for authorized-but-unrenewed restricted therapies. */
    @Transactional
    public int expireAuthorizations() {
        return restrictedAuthService.expireDue(restrictedAuthHours);
    }

    @Transactional(readOnly = true)
    public String drugCode(String drugId) {
        return drugRepository.findById(drugId).map(AntimicrobialDrug::getCode).orElse("?");
    }

    public record PrescriptionCreated(String eventId, Instant occurredAt, String prescriptionId,
                                      String patientId, String drugId, boolean empiric,
                                      boolean restricted) implements DomainEvent {
    }

    public record PrescriptionStopped(String eventId, Instant occurredAt, String prescriptionId)
            implements DomainEvent {
    }
}
