package com.java700.stewardship.microbiology;

import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.messaging.DomainEvent;
import com.java700.stewardship.messaging.DomainEventBus;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Culture reporting pipeline. Reporting a culture publishes CultureReported; consumers
 * evaluate the new susceptibility evidence against the patient's active therapy
 * (drug-bug mismatch alerts, de-escalation suggestions).
 */
@Service
public class MicrobiologyService {

    private final CultureRepository cultureRepository;
    private final IsolateRepository isolateRepository;
    private final SusceptibilityRepository susceptibilityRepository;
    private final DrugRepository drugRepository;
    private final PatientRepository patientRepository;
    private final DomainEventBus bus;
    private final AuditLogService audit;
    private final Clock clock;

    public MicrobiologyService(CultureRepository cultureRepository, IsolateRepository isolateRepository,
                               SusceptibilityRepository susceptibilityRepository, DrugRepository drugRepository,
                               PatientRepository patientRepository, DomainEventBus bus,
                               AuditLogService audit, Clock clock) {
        this.cultureRepository = cultureRepository;
        this.isolateRepository = isolateRepository;
        this.susceptibilityRepository = susceptibilityRepository;
        this.drugRepository = drugRepository;
        this.patientRepository = patientRepository;
        this.bus = bus;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public String createCulture(MicrobiologyApi.CreateCultureRequest req) {
        patientRepository.findById(req.patientId())
                .orElseThrow(() -> new Problems.NotFound("Patient not found"));
        Culture culture = new Culture(UUID.randomUUID().toString(), req.patientId(),
                req.specimenType(), req.collectedAt());
        cultureRepository.save(culture);
        audit.record("CULTURE_CREATED", "CULTURE", culture.getId(),
                req.specimenType() + " collected for patient " + req.patientId());
        return culture.getId();
    }

    @Transactional
    public void addIsolate(String cultureId, MicrobiologyApi.AddIsolateRequest req) {
        Culture culture = cultureRepository.findById(cultureId)
                .orElseThrow(() -> new Problems.NotFound("Culture not found"));
        if (culture.getReportedAt() != null) {
            throw new Problems.Conflict("Culture already reported — cannot add isolates");
        }
        Isolate isolate = new Isolate(UUID.randomUUID().toString(), cultureId, req.organism(),
                culture.getCollectedAt());
        isolateRepository.save(isolate);
        for (MicrobiologyApi.SusceptibilityEntry entry : req.susceptibility()) {
            String drugId = drugRepository.findByCode(entry.drugCode())
                    .orElseThrow(() -> new Problems.NotFound("Drug not found: " + entry.drugCode()))
                    .getId();
            if (!"S".equals(entry.result()) && !"I".equals(entry.result()) && !"R".equals(entry.result())) {
                throw new Problems.BadRequest("Susceptibility result must be S, I or R");
            }
            susceptibilityRepository.save(new SusceptibilityResult(UUID.randomUUID().toString(),
                    isolate.getId(), drugId, entry.result(), entry.micValue()));
        }
        audit.record("ISOLATE_ADDED", "CULTURE", cultureId,
                req.organism() + " with " + req.susceptibility().size() + " susceptibility rows");
    }

    /** Finalizes a culture and fires the evaluation event. */
    @Transactional
    public void report(String cultureId) {
        Culture culture = cultureRepository.findById(cultureId)
                .orElseThrow(() -> new Problems.NotFound("Culture not found"));
        if (culture.getReportedAt() != null) {
            throw new Problems.Conflict("Culture already reported");
        }
        culture.report(Instant.now(clock));
        cultureRepository.save(culture);
        audit.record("CULTURE_REPORTED", "CULTURE", cultureId,
                "Reported by " + SecurityUtil.currentUsername());
        bus.publish(new CultureReported(UUID.randomUUID().toString(), Instant.now(clock),
                cultureId, culture.getPatientId()));
    }

    @Transactional(readOnly = true)
    public List<MicrobiologyApi.CultureView> culturesFor(String patientId) {
        return cultureRepository.findByPatientId(patientId).stream()
                .map(this::toView).toList();
    }

    @Transactional(readOnly = true)
    public List<SusceptibilityResult> susceptibilityForPatient(String patientId) {
        return susceptibilityRepository.findByPatient(patientId);
    }

    private MicrobiologyApi.CultureView toView(Culture c) {
        List<MicrobiologyApi.IsolateView> isolates = new ArrayList<>();
        for (Isolate i : isolateRepository.findByCultureId(c.getId())) {
            List<MicrobiologyApi.SuscView> susc = susceptibilityRepository.findByIsolateId(i.getId())
                    .stream()
                    .map(s -> new MicrobiologyApi.SuscView(drugCode(s.getDrugId()), s.getResult(),
                            s.getMicValue()))
                    .toList();
            isolates.add(new MicrobiologyApi.IsolateView(i.getId(), i.getOrganism(),
                    i.getCollectedAt(), susc));
        }
        return new MicrobiologyApi.CultureView(c.getId(), c.getPatientId(), c.getSpecimenType(),
                c.getCollectedAt(), c.getReportedAt(), isolates);
    }

    private String drugCode(String drugId) {
        return drugRepository.findById(drugId).map(d -> d.getCode()).orElse("?");
    }

    public record CultureReported(String eventId, Instant occurredAt, String cultureId,
                                  String patientId) implements DomainEvent {
    }
}
