package com.java700.stewardship.restricted;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restricted-authorizations")
@Tag(name = "restricted", description = "Time-boxed pre-authorization for restricted antimicrobials")
public class RestrictedAuthController {

    private final RestrictedAuthorizationService service;
    private final int ttlHours;

    public RestrictedAuthController(RestrictedAuthorizationService service,
                                    @Value("${app.stewardship.restricted-auth-hours:72}") int ttlHours) {
        this.service = service;
        this.ttlHours = ttlHours;
    }

    @GetMapping("/pending")
    @Operation(summary = "List pending pre-authorization requests")
    @PreAuthorize("hasAnyRole('ID_PHYSICIAN','STEWARDSHIP_ADMIN','PHARMACIST')")
    public List<RestrictedAuthApi.AuthView> pending() {
        return service.pending();
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a restricted-drug authorization (time-boxed)")
    @PreAuthorize("hasAnyRole('ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public RestrictedAuthApi.AuthView approve(@PathVariable String id) {
        return service.approve(id, ttlHours);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject a restricted-drug authorization")
    @PreAuthorize("hasAnyRole('ID_PHYSICIAN','STEWARDSHIP_ADMIN')")
    public RestrictedAuthApi.AuthView reject(@PathVariable String id,
                                             @Valid @RequestBody(required = false) RestrictedAuthApi.DecideRequest body) {
        return service.reject(id, body == null ? null : body.note());
    }
}
