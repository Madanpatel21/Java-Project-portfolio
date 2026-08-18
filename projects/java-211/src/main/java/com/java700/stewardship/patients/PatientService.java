package com.java700.stewardship.patients;

import com.java700.stewardship.common.api.PageResponse;
import com.java700.stewardship.common.api.Problems;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;
    private final LabValueRepository labRepository;
    private final Clock clock;

    public PatientService(PatientRepository patientRepository, AdmissionRepository admissionRepository,
                          LabValueRepository labRepository, Clock clock) {
        this.patientRepository = patientRepository;
        this.admissionRepository = admissionRepository;
        this.labRepository = labRepository;
        this.clock = clock;
    }

    @Transactional
    public PatientApi.PatientView create(PatientApi.CreatePatientRequest req) {
        patientRepository.findByMrn(req.mrn()).ifPresent(p -> {
            throw new Problems.Conflict("Patient with this MRN already exists");
        });
        Patient p = new Patient(UUID.randomUUID().toString(), req.mrn(), req.name(), req.dob(),
                req.sex(), req.weightKg());
        return PatientApi.PatientView.from(patientRepository.save(p));
    }

    @Transactional(readOnly = true)
    public Patient get(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Patient not found"));
    }

    @Transactional(readOnly = true)
    public PatientApi.PatientView view(String id) {
        return PatientApi.PatientView.from(get(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<PatientApi.PatientView> search(String query, int page, int size) {
        String q = query == null ? "" : query;
        return PageResponse.from(patientRepository
                .findByMrnContainingIgnoreCaseOrNameContainingIgnoreCase(
                        q, q, PageRequest.of(page, Math.min(size, 100), Sort.by("mrn")))
                .map(PatientApi.PatientView::from));
    }

    @Transactional
    public PatientApi.AdmissionView admit(PatientApi.CreateAdmissionRequest req) {
        get(req.patientId());
        Admission a = new Admission(UUID.randomUUID().toString(), req.patientId(), req.ward(),
                req.admittedAt(), req.dischargedAt());
        return PatientApi.AdmissionView.from(admissionRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<PatientApi.AdmissionView> admissions(String patientId) {
        return admissionRepository.findByPatientIdOrderByAdmittedAtDesc(patientId).stream()
                .map(PatientApi.AdmissionView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<Admission> activeAdmissions() {
        Instant now = Instant.now(clock);
        return admissionRepository.findByAdmittedAtBeforeAndDischargedAtAfter(now, now);
    }

    @Transactional
    public void recordLab(PatientApi.CreateLabValueRequest req) {
        get(req.patientId());
        labRepository.save(new LabValue(UUID.randomUUID().toString(), req.patientId(), req.type(),
                req.value(), req.unit(), req.measuredAt()));
    }

    /** Latest value of a lab type (e.g. most recent serum creatinine). */
    @Transactional(readOnly = true)
    public LabValue latestLab(String patientId, String type) {
        return labRepository.findByPatientIdAndTypeOrderByMeasuredAtDesc(patientId, type).stream()
                .findFirst()
                .orElseThrow(() -> new Problems.NotFound("No " + type + " lab value for patient"));
    }
}
