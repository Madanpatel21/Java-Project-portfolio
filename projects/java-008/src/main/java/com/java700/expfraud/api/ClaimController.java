package com.java700.expfraud.api;

import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.security.SecurityUtil;
import com.java700.expfraud.service.Api;
import com.java700.expfraud.service.ClaimService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Expense claim intake, review queue and manager decisions. */
@RestController
@RequestMapping("/api/v1/claims")
public class ClaimController {

    private final ClaimService claims;

    public ClaimController(ClaimService claims) {
        this.claims = claims;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public Api.ClaimView submit(@Valid @RequestBody Api.SubmitClaimRequest request,
                                @RequestHeader(value = "Idempotency-Key", required = false)
                                String idemKey) {
        ExpenseClaim claim = claims.submit(request, idemKey, SecurityUtil.currentUsername());
        return claims.view(claim);
    }

    @GetMapping("/{claimId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public Api.ClaimView get(@PathVariable String claimId) {
        return claims.view(claims.load(claimId));
    }

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.ClaimView> queue(@RequestParam(defaultValue = "35") int minScore) {
        return claims.queue(minScore).stream().map(claims::view).toList();
    }

    @PostMapping("/{claimId}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.ClaimView approve(@PathVariable String claimId, @RequestBody(required = false)
                                 Api.ApproveRequest request) {
        String note = request == null ? null : request.note();
        return claims.view(claims.decide(claimId, true, note, SecurityUtil.currentUsername()));
    }

    @PostMapping("/{claimId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.ClaimView reject(@PathVariable String claimId, @RequestBody(required = false)
                                Api.ApproveRequest request) {
        String note = request == null ? null : request.note();
        return claims.view(claims.decide(claimId, false, note, SecurityUtil.currentUsername()));
    }

    @GetMapping("/{claimId}/violations")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.ViolationView> violations(@PathVariable String claimId) {
        return claims.violationsOf(claimId);
    }

    @GetMapping("/duplicate-groups")
    @PreAuthorize("hasAnyRole('FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public List<Api.DuplicateGroupView> duplicateGroups() {
        return claims.duplicateGroups();
    }

    /** Documented RBAC surface for the OpenAPI spec and tests. */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','FRAUD_INVESTIGATOR','AUDITOR','ADMIN')")
    public String roles() {
        return "EMPLOYEE=submit+view | MANAGER=approve/reject low+medium risk | "
                + "FRAUD_INVESTIGATOR=queue+cases+four-eyes | AUDITOR=full evidence | "
                + "ADMIN=policy rules+baselines";
    }
}
