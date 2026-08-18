package com.java700.crvs.registration;

import com.java700.crvs.common.api.PageResponse;
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
@RequestMapping("/api/v1/registrations")
@Tag(name = "registrations", description = "Four-eyes life-event registration (birth/marriage/death/correction)")
public class RegistrationController {

    private final RegistrationService service;

    public RegistrationController(RegistrationService service) {
        this.service = service;
    }

    @PostMapping("/birth")
    @Operation(summary = "Capture a birth registration (PENDING until a supervisor approves)")
    @PreAuthorize("hasRole('REGISTRAR')")
    public RegistrationApi.CreateResponse birth(@Valid @RequestBody RegistrationApi.BirthRequest body,
                                                @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.createBirth(body, idempotencyKey);
    }

    @PostMapping("/marriage")
    @Operation(summary = "Capture a marriage registration between two ACTIVE persons")
    @PreAuthorize("hasRole('REGISTRAR')")
    public RegistrationApi.CreateResponse marriage(@Valid @RequestBody RegistrationApi.MarriageRequest body,
                                                   @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.createMarriage(body, idempotencyKey);
    }

    @PostMapping("/death")
    @Operation(summary = "Capture a death registration (person marked DECEASED on approval)")
    @PreAuthorize("hasRole('REGISTRAR')")
    public RegistrationApi.CreateResponse death(@Valid @RequestBody RegistrationApi.DeathRequest body,
                                                @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.createDeath(body, idempotencyKey);
    }

    @PostMapping("/correction")
    @Operation(summary = "Capture a correction to an existing record (amendment preserves originals)")
    @PreAuthorize("hasRole('REGISTRAR')")
    public RegistrationApi.CreateResponse correction(@Valid @RequestBody RegistrationApi.CorrectionRequest body,
                                                     @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.createCorrection(body, idempotencyKey);
    }

    @GetMapping
    @Operation(summary = "List PENDING registrations (optionally scoped to an office)")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public PageResponse<RegistrationApi.RegistrationView> pending(
            @RequestParam(required = false) String officeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.pending(officeId, page, size);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Supervisor approves (four-eyes: not the capturing registrar)")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public RegistrationApi.RegistrationView approve(@PathVariable String id,
                                                    @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
                                                    @Valid @RequestBody(required = false) RegistrationApi.DecideRequest body) {
        return service.decide(id, true, body == null ? null : body.note(), idempotencyKey);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Supervisor rejects a registration")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public RegistrationApi.RegistrationView reject(@PathVariable String id,
                                                   @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
                                                   @Valid @RequestBody(required = false) RegistrationApi.DecideRequest body) {
        return service.decide(id, false, body == null ? null : body.note(), idempotencyKey);
    }

    @GetMapping("/person/{personId}")
    @Operation(summary = "Registration history for a person")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','REGISTRAR','STATISTICIAN')")
    public List<RegistrationApi.RegistrationView> history(@PathVariable String personId) {
        return service.historyFor(personId);
    }
}
