package com.java700.stewardship.interventions;

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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interventions")
@Tag(name = "interventions", description = "Pharmacist stewardship interventions")
public class InterventionController {

    private final InterventionService service;

    public InterventionController(InterventionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Propose a stewardship intervention")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public InterventionApi.InterventionView propose(@Valid @RequestBody InterventionApi.ProposeRequest body,
                                                    @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.propose(body, idempotencyKey);
    }

    @GetMapping("/open")
    @Operation(summary = "List open (undecided) interventions")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','STEWARDSHIP_ADMIN','PRESCRIBER')")
    public List<InterventionApi.InterventionView> open() {
        return service.open();
    }

    @GetMapping("/prescriptions/{prescriptionId}")
    @Operation(summary = "List interventions for a prescription")
    @PreAuthorize("isAuthenticated()")
    public List<InterventionApi.InterventionView> forPrescription(@PathVariable String prescriptionId) {
        return service.forPrescription(prescriptionId);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Prescriber accepts an intervention (therapy modified transactionally)")
    @PreAuthorize("hasAnyRole('PRESCRIBER','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public InterventionApi.InterventionView accept(@PathVariable String id,
                                                   @Valid @RequestBody(required = false) InterventionApi.DecideRequest body) {
        return service.decide(id, true, body == null ? null : body.response());
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Prescriber rejects an intervention (reason required)")
    @PreAuthorize("hasAnyRole('PRESCRIBER','ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public InterventionApi.InterventionView reject(@PathVariable String id,
                                                   @Valid @RequestBody(required = false) InterventionApi.DecideRequest body) {
        return service.decide(id, false, body == null ? null : body.response());
    }
}
