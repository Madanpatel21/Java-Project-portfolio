package com.java700.stewardship.prescriptions;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "prescriptions", description = "Antimicrobial prescription lifecycle")
public class PrescriptionController {

    private final PrescriptionService service;

    public PrescriptionController(PrescriptionService service) {
        this.service = service;
    }

    @PostMapping("/prescriptions")
    @Operation(summary = "Order an antimicrobial (restricted drugs require ID pre-authorization)")
    @PreAuthorize("hasAnyRole('PRESCRIBER','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public PrescriptionApi.CreateResponse create(@Valid @RequestBody PrescriptionApi.CreateRequest body,
                                                 @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.create(body, idempotencyKey);
    }

    @GetMapping("/patients/{patientId}/prescriptions")
    @Operation(summary = "List prescriptions for a patient")
    @PreAuthorize("isAuthenticated()")
    public List<PrescriptionApi.RxView> forPatient(@PathVariable String patientId,
                                                   @RequestParam(required = false) String status) {
        return "ACTIVE".equalsIgnoreCase(status)
                ? service.activeForPatient(patientId)
                : service.allForPatient(patientId);
    }

    @PostMapping("/prescriptions/{id}/stop")
    @Operation(summary = "Stop an active prescription")
    @PreAuthorize("hasAnyRole('PRESCRIBER','PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public PrescriptionApi.RxView stop(@PathVariable String id) {
        return service.stop(id);
    }
}
