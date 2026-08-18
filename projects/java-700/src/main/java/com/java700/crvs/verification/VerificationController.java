package com.java700.crvs.verification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/verify")
@Tag(name = "verification", description = "Third-party identity verification (deceased-aware)")
public class VerificationController {

    private final VerificationService service;

    public VerificationController(VerificationService service) {
        this.service = service;
    }

    @GetMapping("/person/{nationalId}")
    @Operation(summary = "Verify a national id (rate-limited; deceased status propagates immediately)")
    @PreAuthorize("hasAnyRole('VERIFIER_CLIENT','SUPERVISOR','ADMIN')")
    public VerificationApi.PersonVerification verify(@PathVariable String nationalId) {
        return service.verify(nationalId);
    }
}
