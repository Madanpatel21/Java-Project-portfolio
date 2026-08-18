package com.java700.workforce.access;

import com.java700.workforce.common.api.PageResponse;
import com.java700.workforce.security.Roles;
import com.java700.workforce.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "access", description = "Access requests, dual-control approvals and grants")
public class AccessController {

    private final AccessService service;

    public AccessController(AccessService service) {
        this.service = service;
    }

    @PostMapping("/access-requests")
    @Operation(summary = "Request access (employees may only request for themselves)")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ACCESS_MANAGER','COMPLIANCE_ADMIN')")
    public AccessApi.RequestView create(@Valid @RequestBody AccessApi.CreateRequest req) {
        String requesterId = SecurityUtil.currentUserId();
        if (SecurityUtil.hasRole(Roles.EMPLOYEE) && !requesterId.equals(req.subjectUserId())) {
            throw new com.java700.workforce.common.api.Problems.Conflict(
                    "Employees may only request access for themselves");
        }
        return service.createRequest(requesterId, req);
    }

    @GetMapping("/access-requests")
    @Operation(summary = "List access requests (pending queue or my requests)")
    @PreAuthorize("hasAnyRole('ACCESS_MANAGER','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','AUDITOR','EMPLOYEE')")
    public PageResponse<AccessApi.RequestView> list(@RequestParam(required = false) String view,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        if ("pending".equals(view)) {
            return service.pendingRequests(page, Math.min(size, 100));
        }
        if ("mine".equals(view) && SecurityUtil.hasRole(Roles.EMPLOYEE)) {
            return service.requestsFor(SecurityUtil.currentUserId(), page, Math.min(size, 100));
        }
        return service.requestsFor(SecurityUtil.currentUserId(), page, Math.min(size, 100));
    }

    @PostMapping("/access-requests/{id}/approve")
    @Operation(summary = "Approve an access request (dual control + segregation of duties enforced)")
    @PreAuthorize("hasAnyRole('ACCESS_MANAGER','COMPLIANCE_ADMIN')")
    public AccessApi.RequestView approve(@PathVariable String id,
                                         @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
                                         @Valid @RequestBody(required = false) AccessApi.DecideRequest body) {
        return service.decide(id, true, body == null ? null : body.comment(), idempotencyKey);
    }

    @PostMapping("/access-requests/{id}/reject")
    @Operation(summary = "Reject an access request")
    @PreAuthorize("hasAnyRole('ACCESS_MANAGER','COMPLIANCE_ADMIN')")
    public AccessApi.RequestView reject(@PathVariable String id,
                                        @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
                                        @Valid @RequestBody(required = false) AccessApi.DecideRequest body) {
        return service.decide(id, false, body == null ? null : body.comment(), idempotencyKey);
    }

    @GetMapping("/grants")
    @Operation(summary = "List my grants")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<AccessApi.GrantView> myGrants(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return service.grantsFor(SecurityUtil.currentUserId(), page, Math.min(size, 100));
    }

    @GetMapping("/grants/{userId}")
    @Operation(summary = "List a user's grants")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public PageResponse<AccessApi.GrantView> grantsOf(@PathVariable String userId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return service.grantsFor(userId, page, Math.min(size, 100));
    }

    @PostMapping("/grants/{id}/revoke")
    @Operation(summary = "Revoke an active grant")
    @PreAuthorize("hasAnyRole('ACCESS_MANAGER','COMPLIANCE_ADMIN')")
    public AccessApi.GrantView revoke(@PathVariable String id,
                                      @Valid @RequestBody(required = false) AccessApi.RevokeRequest body) {
        return service.revoke(id, body == null ? null : body.reason());
    }
}
