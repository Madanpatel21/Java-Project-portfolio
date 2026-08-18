package com.java700.stewardship.reviews;

import com.java700.stewardship.catalog.AntimicrobialDrug;
import com.java700.stewardship.catalog.DrugRepository;
import com.java700.stewardship.common.api.Problems;
import com.java700.stewardship.common.audit.AuditLogService;
import com.java700.stewardship.guidelines.GuidelineRepository;
import com.java700.stewardship.guidelines.StewardshipFinding;
import com.java700.stewardship.guidelines.StewardshipFinding.FindingType;
import com.java700.stewardship.guidelines.StewardshipRuleEngine;
import com.java700.stewardship.messaging.DomainEventHandler;
import com.java700.stewardship.microbiology.SusceptibilityRepository;
import com.java700.stewardship.observability.Metrics;
import com.java700.stewardship.microbiology.MicrobiologyService.CultureReported;
import com.java700.stewardship.patients.LabValue;
import com.java700.stewardship.patients.LabValueRepository;
import com.java700.stewardship.patients.PatientRepository;
import com.java700.stewardship.prescriptions.Prescription;
import com.java700.stewardship.prescriptions.PrescriptionRepository;
import com.java700.stewardship.prescriptions.PrescriptionService.PrescriptionCreated;
import com.java700.stewardship.prescriptions.PrescriptionService.PrescriptionStopped;
import com.java700.stewardship.restricted.RestrictedAuthorizationService.RestrictedAuthApproved;
import com.java700.stewardship.restricted.RestrictedAuthorizationService.RestrictedAuthExpired;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Stewardship evaluation: reacts to prescription, culture and authorization events,
 * runs the guideline rule engine, and materializes review tasks. Idempotent by design
 * (task creation dedupes on prescription + trigger).
 */
@Component
public class StewardshipEvaluation {

    private static final Logger log = LoggerFactory.getLogger(StewardshipEvaluation.class);

    private final PrescriptionRepository prescriptionRepository;
    private final DrugRepository drugRepository;
    private final PatientRepository patientRepository;
    private final GuidelineRepository guidelineRepository;
    private final LabValueRepository labRepository;
    private final SusceptibilityRepository susceptibilityRepository;
    private final StewardshipRuleEngine ruleEngine;
    private final ReviewTaskService reviewTasks;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public StewardshipEvaluation(PrescriptionRepository prescriptionRepository,
                                 DrugRepository drugRepository, PatientRepository patientRepository,
                                 GuidelineRepository guidelineRepository, LabValueRepository labRepository,
                                 SusceptibilityRepository susceptibilityRepository,
                                 StewardshipRuleEngine ruleEngine, ReviewTaskService reviewTasks,
                                 AuditLogService audit, Metrics metrics, Clock clock) {
        this.prescriptionRepository = prescriptionRepository;
        this.drugRepository = drugRepository;
        this.patientRepository = patientRepository;
        this.guidelineRepository = guidelineRepository;
        this.labRepository = labRepository;
        this.susceptibilityRepository = susceptibilityRepository;
        this.ruleEngine = ruleEngine;
        this.reviewTasks = reviewTasks;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    /** Evaluates one prescription against the active guideline set + cultures + labs. */
    @Transactional(readOnly = true)
    public List<StewardshipFinding> evaluate(String prescriptionId) {
        Prescription rx = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new Problems.NotFound("Prescription not found"));
        var patient = patientRepository.findById(rx.getPatientId())
                .orElseThrow(() -> new Problems.NotFound("Patient not found"));
        var guideline = guidelineRepository.findByStatus("ACTIVE")
                .orElseThrow(() -> new Problems.NotFound("No active guideline set"));
        AntimicrobialDrug drug = drugRepository.findById(rx.getDrugId())
                .orElseThrow(() -> new Problems.NotFound("Drug not found"));
        List<Prescription> siblings = prescriptionRepository
                .findByPatientIdAndStatus(rx.getPatientId(), "ACTIVE").stream()
                .filter(s -> !s.getId().equals(rx.getId())).toList();
        List<AntimicrobialDrug> siblingDrugs = siblings.stream()
                .map(s -> drugRepository.findById(s.getDrugId()).orElse(null))
                .filter(d -> d != null).toList();
        BigDecimal creatinine = latestCreatinine(rx.getPatientId());
        return ruleEngine.evaluate(guideline.getRulesJson(), rx, drug, patient, creatinine,
                siblings, siblingDrugs, susceptibilityRepository.findByPatient(rx.getPatientId()),
                Instant.now(clock));
    }

    private BigDecimal latestCreatinine(String patientId) {
        List<LabValue> labs = labRepository.findByPatientIdAndTypeOrderByMeasuredAtDesc(
                patientId, "CREATININE");
        return labs.isEmpty() ? null : labs.get(0).getValue();
    }

    // ------------------------------------------------------------ handlers

    @Component
    public static class OnPrescriptionCreated
            implements DomainEventHandler<PrescriptionCreated> {

        private final StewardshipEvaluation evaluation;
        private final ReviewTaskService reviewTasks;
        private final Clock clock;

        public OnPrescriptionCreated(StewardshipEvaluation evaluation, ReviewTaskService reviewTasks,
                                     Clock clock) {
            this.evaluation = evaluation;
            this.reviewTasks = reviewTasks;
            this.clock = clock;
        }

        @Override
        public Class<PrescriptionCreated> supportedType() {
            return PrescriptionCreated.class;
        }

        @Override
        @Transactional
        public void handle(PrescriptionCreated event) {
            for (StewardshipFinding f : evaluation.evaluate(event.prescriptionId())) {
                if (f.type() == FindingType.REDUNDANT_COVERAGE
                        || f.type() == FindingType.DRUG_BUG_MISMATCH) {
                    reviewTasks.createIfAbsent(event.prescriptionId(), f.type().name(),
                            Instant.now(clock));
                }
            }
        }
    }

    @Component
    public static class OnCultureReported implements DomainEventHandler<CultureReported> {

        private final PrescriptionRepository prescriptionRepository;
        private final StewardshipEvaluation evaluation;
        private final ReviewTaskService reviewTasks;
        private final AuditLogService audit;
        private final Metrics metrics;
        private final Clock clock;

        public OnCultureReported(PrescriptionRepository prescriptionRepository,
                                 StewardshipEvaluation evaluation, ReviewTaskService reviewTasks,
                                 AuditLogService audit, Metrics metrics, Clock clock) {
            this.prescriptionRepository = prescriptionRepository;
            this.evaluation = evaluation;
            this.reviewTasks = reviewTasks;
            this.audit = audit;
            this.metrics = metrics;
            this.clock = clock;
        }

        @Override
        public Class<CultureReported> supportedType() {
            return CultureReported.class;
        }

        @Override
        @Transactional
        public void handle(CultureReported event) {
            List<Prescription> active = prescriptionRepository.findByPatientIdAndStatus(
                    event.patientId(), "ACTIVE");
            for (Prescription rx : active) {
                for (StewardshipFinding f : evaluation.evaluate(rx.getId())) {
                    if (f.type() == FindingType.DRUG_BUG_MISMATCH) {
                        reviewTasks.createIfAbsent(rx.getId(), f.type().name(), Instant.now(clock));
                        metrics.incrementDrugBugMismatchAlerts();
                        audit.record("DRUG_BUG_MISMATCH_ALERT", "PRESCRIPTION", rx.getId(), f.detail());
                    }
                    if (f.type() == FindingType.DE_ESCALATION_CANDIDATE) {
                        reviewTasks.createIfAbsent(rx.getId(), f.type().name(), Instant.now(clock));
                    }
                }
            }
        }
    }

    @Component
    public static class OnRestrictedAuthApproved implements DomainEventHandler<RestrictedAuthApproved> {

        private final PrescriptionRepository prescriptionRepository;
        private final AuditLogService audit;
        private final Clock clock;

        public OnRestrictedAuthApproved(PrescriptionRepository prescriptionRepository,
                                        AuditLogService audit, Clock clock) {
            this.prescriptionRepository = prescriptionRepository;
            this.audit = audit;
            this.clock = clock;
        }

        @Override
        public Class<RestrictedAuthApproved> supportedType() {
            return RestrictedAuthApproved.class;
        }

        @Override
        @Transactional
        public void handle(RestrictedAuthApproved event) {
            prescriptionRepository.findById(event.prescriptionId()).ifPresent(rx -> {
                if (rx.getStatus() == Prescription.Status.PENDING_AUTHORIZATION) {
                    rx.activate();
                    prescriptionRepository.save(rx);
                    audit.record("PRESCRIPTION_ACTIVATED", "PRESCRIPTION", rx.getId(),
                            "Restricted therapy authorized until " + event.expiresAt());
                }
            });
        }
    }

    @Component
    public static class OnRestrictedAuthExpired implements DomainEventHandler<RestrictedAuthExpired> {

        private final PrescriptionRepository prescriptionRepository;
        private final AuditLogService audit;
        private final Clock clock;

        public OnRestrictedAuthExpired(PrescriptionRepository prescriptionRepository,
                                       AuditLogService audit, Clock clock) {
            this.prescriptionRepository = prescriptionRepository;
            this.audit = audit;
            this.clock = clock;
        }

        @Override
        public Class<RestrictedAuthExpired> supportedType() {
            return RestrictedAuthExpired.class;
        }

        @Override
        @Transactional
        public void handle(RestrictedAuthExpired event) {
            prescriptionRepository.findById(event.prescriptionId()).ifPresent(rx -> {
                if (rx.getStatus() == Prescription.Status.ACTIVE) {
                    rx.expire(Instant.now(clock));
                    prescriptionRepository.save(rx);
                    audit.record("PRESCRIPTION_EXPIRED", "PRESCRIPTION", rx.getId(),
                            "Restricted authorization expired — therapy stopped");
                }
            });
        }
    }

    @Component
    public static class OnPrescriptionStopped implements DomainEventHandler<PrescriptionStopped> {

        private final ReviewTaskService reviewTasks;

        public OnPrescriptionStopped(ReviewTaskService reviewTasks) {
            this.reviewTasks = reviewTasks;
        }

        @Override
        public Class<PrescriptionStopped> supportedType() {
            return PrescriptionStopped.class;
        }

        @Override
        @Transactional
        public void handle(PrescriptionStopped event) {
            reviewTasks.cancelForPrescription(event.prescriptionId());
        }
    }
}
