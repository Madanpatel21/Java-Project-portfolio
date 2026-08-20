package com.java700.contracts.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.java700.contracts.common.api.PageResponse;
import com.java700.contracts.matching.ContractDiff;
import com.java700.contracts.service.Api;
import com.java700.contracts.service.ContractService;
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
@Tag(name = "contracts", description = "Contract lifecycle, versioned clauses and obligation SLA engine")
public class ContractController {

    private final ContractService service;

    public ContractController(ContractService service) {
        this.service = service;
    }

    // ---- contracts ----

    @PostMapping("/contracts")
    @Operation(summary = "Create a contract (DRAFT)")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.ContractView create(@Valid @RequestBody Api.CreateContractRequest body,
                                   @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.create(body, idemKey);
    }

    @GetMapping("/contracts")
    @Operation(summary = "List contracts (optional status filter)")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.ContractView> contracts(@RequestParam(required = false) String status,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return service.contracts(status, page, size);
    }

    @PostMapping("/contracts/{id}/versions")
    @Operation(summary = "Add an immutable version to a contract")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.VersionView addVersion(@PathVariable String id,
                                      @Valid @RequestBody Api.CreateVersionRequest body,
                                      @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.addVersion(id, body, idemKey);
    }

    @GetMapping("/contracts/{id}/versions")
    @Operation(summary = "List contract versions (newest first)")
    @PreAuthorize("isAuthenticated()")
    public List<Api.VersionView> versions(@PathVariable String id) {
        return service.versions(id);
    }

    @GetMapping("/contracts/{id}/clauses")
    @Operation(summary = "Read clauses with role-based sensitivity redaction; ?version=N selects a version")
    @PreAuthorize("isAuthenticated()")
    public JsonNode clauses(@PathVariable String id, @RequestParam(defaultValue = "0") int version) {
        return service.clauses(id, version);
    }

    @GetMapping("/contracts/{id}/diff")
    @Operation(summary = "Clause-level diff between two versions")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN','AUDITOR')")
    public List<ContractDiff.Change> diff(@PathVariable String id,
                                          @RequestParam int v1, @RequestParam int v2) {
        return service.diff(id, v1, v2);
    }

    @PostMapping("/contracts/{id}/activate")
    @Operation(summary = "Four-eyes activation: LEGAL + CONTRACT_MANAGER must both approve")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','CONTRACT_MANAGER','ADMIN')")
    public Api.ContractView activate(@PathVariable String id,
                                     @RequestParam(defaultValue = "true") boolean approve,
                                     @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.activate(id, approve, body == null ? null : body.note());
    }

    @PostMapping("/contracts/{id}/terminate")
    @Operation(summary = "Terminate an active contract")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.ContractView terminate(@PathVariable String id) {
        return service.terminate(id);
    }

    // ---- obligations ----

    @PostMapping("/obligations")
    @Operation(summary = "Attach an obligation to a contract")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.ObligationView createObligation(@Valid @RequestBody Api.CreateObligationRequest body,
                                               @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createObligation(body, idemKey);
    }

    @GetMapping("/obligations")
    @Operation(summary = "List obligations (optional status filter)")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<Api.ObligationView> obligations(@RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return service.obligations(status, page, size);
    }

    @GetMapping("/contracts/{id}/obligations")
    @Operation(summary = "List a contract's obligations (due first)")
    @PreAuthorize("isAuthenticated()")
    public List<Api.ObligationView> obligationsFor(@PathVariable String id) {
        return service.obligationsFor(id);
    }

    @PostMapping("/obligations/{id}/acknowledge")
    @Operation(summary = "Acknowledge an obligation")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','BUSINESS_OWNER','ADMIN')")
    public Api.ObligationView acknowledge(@PathVariable String id) {
        return service.acknowledge(id);
    }

    @PostMapping("/obligations/{id}/complete")
    @Operation(summary = "Complete an obligation (recurring obligations spawn the next instance)")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','BUSINESS_OWNER','ADMIN')")
    public Api.ObligationView complete(@PathVariable String id) {
        return service.complete(id);
    }

    @PostMapping("/obligations/{id}/waive")
    @Operation(summary = "Waive an obligation (four-eyes: LEGAL_COUNSEL only)")
    @PreAuthorize("hasAnyRole('LEGAL_COUNSEL','ADMIN')")
    public Api.ObligationView waive(@PathVariable String id,
                                    @RequestHeader(value = "Idempotency-Key") String idemKey,
                                    @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.waive(id, body == null ? null : body.note(), idemKey);
    }

    @PostMapping("/obligations/scan")
    @Operation(summary = "Run the SLA scan manually (notifications + overdue detection)")
    @PreAuthorize("hasAnyRole('CONTRACT_MANAGER','LEGAL_COUNSEL','ADMIN')")
    public Api.ScanResult scan() {
        return service.scan();
    }
}
