package com.java700.stewardship.patients;

import com.java700.stewardship.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "patients", description = "Patient registry, admissions and labs (PHI masked)")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a patient")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public PatientApi.PatientView create(@Valid @RequestBody PatientApi.CreatePatientRequest body) {
        return service.create(body);
    }

    @GetMapping
    @Operation(summary = "Search patients by MRN or name")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<PatientApi.PatientView> search(@RequestParam(required = false) String query,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "20") int size) {
        return service.search(query, page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient (masked view)")
    @PreAuthorize("isAuthenticated()")
    public PatientApi.PatientView get(@PathVariable String id) {
        return service.view(id);
    }

    @PostMapping("/{id}/admissions")
    @Operation(summary = "Record an admission")
    @PreAuthorize("hasAnyRole('PRESCRIBER','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public PatientApi.AdmissionView admit(@PathVariable String id,
                                          @Valid @RequestBody PatientApi.CreateAdmissionRequest body) {
        if (!id.equals(body.patientId())) {
            throw new com.java700.stewardship.common.api.Problems.BadRequest(
                    "Path patientId must match request body");
        }
        return service.admit(body);
    }

    @GetMapping("/{id}/admissions")
    @Operation(summary = "List admissions for a patient")
    @PreAuthorize("isAuthenticated()")
    public List<PatientApi.AdmissionView> admissions(@PathVariable String id) {
        return service.admissions(id);
    }

    @PostMapping("/labs")
    @Operation(summary = "Record a laboratory value")
    @PreAuthorize("hasAnyRole('MICROBIOLOGIST','PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public void recordLab(@Valid @RequestBody PatientApi.CreateLabValueRequest body) {
        service.recordLab(body);
    }
}
