package com.java700.achain.api;

import com.java700.achain.common.api.PageResponse;
import com.java700.achain.service.Api;
import com.java700.achain.service.ApprovalService;
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
@Tag(name = "approvals", description = "Audit-grade approval chains bound to policy versions")
public class ApprovalController {

    private final ApprovalService service;

    public ApprovalController(ApprovalService service) {
        this.service = service;
    }

    @PostMapping("/policies")
    @Operation(summary = "Create/activate a policy version")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','ADMIN')")
    public Api.PolicyView createPolicy(@Valid @RequestBody Api.CreatePolicyRequest body,
                                       @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createPolicy(body, idemKey);
    }

    @GetMapping("/policies")
    @Operation(summary = "List policies")
    @PreAuthorize("isAuthenticated()")
    public List<Api.PolicyView> policies() {
        return service.policies();
    }

    @PostMapping("/chains")
    @Operation(summary = "Create an approval chain (validated steps)")
    @PreAuthorize("hasRole('ADMIN')")
    public Api.ChainView createChain(@Valid @RequestBody Api.CreateChainRequest body,
                                     @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createChain(body, idemKey);
    }

    @GetMapping("/chains")
    @Operation(summary = "List approval chains")
    @PreAuthorize("isAuthenticated()")
    public List<Api.ChainView> chains() {
        return service.chains();
    }

    @PostMapping("/requests")
    @Operation(summary = "Create an approval request (bound to the active policy version)")
    @PreAuthorize("hasRole('REQUESTER')")
    public Api.RequestView createRequest(@Valid @RequestBody Api.CreateRequestRequest body,
                                         @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createRequest(body, idemKey);
    }

    @GetMapping("/requests")
    @Operation(summary = "List approval requests (optional status filter)")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.RequestView> requests(@RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return service.requests(status, page, size);
    }

    @PostMapping("/requests/{id}/approve")
    @Operation(summary = "Approve at the current step (per-step dual control)")
    @PreAuthorize("hasAnyRole('MANAGER','DIRECTOR','LEGAL_COUNSEL','ADMIN')")
    public Api.RequestView approve(@PathVariable String id,
                                   @RequestHeader(value = "Idempotency-Key") String idemKey,
                                   @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.decide(id, true, body == null ? null : body.note(), idemKey);
    }

    @PostMapping("/requests/{id}/reject")
    @Operation(summary = "Reject at the current step")
    @PreAuthorize("hasAnyRole('MANAGER','DIRECTOR','LEGAL_COUNSEL','ADMIN')")
    public Api.RequestView reject(@PathVariable String id,
                                  @RequestHeader(value = "Idempotency-Key") String idemKey,
                                  @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.decide(id, false, body == null ? null : body.note(), idemKey);
    }

    @PostMapping("/requests/{id}/cancel")
    @Operation(summary = "Cancel a pending request")
    @PreAuthorize("hasAnyRole('REQUESTER','ADMIN')")
    public Api.RequestView cancel(@PathVariable String id) {
        return service.cancel(id);
    }

    @GetMapping("/requests/{id}/decisions")
    @Operation(summary = "Audit-grade decision evidence for a request")
    @PreAuthorize("hasAnyRole('AUDITOR','LEGAL_COUNSEL','ADMIN','MANAGER','DIRECTOR')")
    public List<Api.DecisionView> decisions(@PathVariable String id) {
        return service.decisions(id);
    }

    @PostMapping("/escalations")
    @Operation(summary = "Escalate stale pending requests (SLA)")
    @PreAuthorize("hasRole('ADMIN')")
    public Api.EscalationResult escalate() {
        return service.escalate();
    }
}
