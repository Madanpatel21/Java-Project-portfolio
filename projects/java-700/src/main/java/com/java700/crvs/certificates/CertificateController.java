package com.java700.crvs.certificates;

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
@RequestMapping("/api/v1/certificates")
@Tag(name = "certificates", description = "Civil-status certificate issuance, verification and revocation")
public class CertificateController {

    private final CertificateService service;

    public CertificateController(CertificateService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Issue a certificate (BIRTH/MARRIAGE/DEATH)")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN')")
    public CertificateApi.CertificateView issue(@Valid @RequestBody CertificateApi.IssueRequest body,
                                                @RequestHeader(value = "Idempotency-Key") String idempotencyKey) {
        return service.issue(body.personId(), body.type(), idempotencyKey);
    }

    @GetMapping("/verify/{token}")
    @Operation(summary = "Verify a certificate token (identity disclosed only when valid)")
    @PreAuthorize("hasAnyRole('VERIFIER_CLIENT','SUPERVISOR','ADMIN')")
    public CertificateApi.VerificationView verify(@PathVariable String token) {
        return service.verify(token);
    }

    @PostMapping("/{id}/revoke")
    @Operation(summary = "Revoke a certificate")
    @PreAuthorize("hasRole('ADMIN')")
    public CertificateApi.CertificateView revoke(@PathVariable String id,
                                                 @Valid @RequestBody(required = false) CertificateApi.RevokeRequest body) {
        return service.revoke(id, body == null ? null : body.reason());
    }

    @GetMapping("/person/{personId}")
    @Operation(summary = "List certificates for a person")
    @PreAuthorize("hasAnyRole('SUPERVISOR','ADMIN','REGISTRAR')")
    public List<CertificateApi.CertificateView> forPerson(@PathVariable String personId) {
        return service.forPerson(personId);
    }
}
