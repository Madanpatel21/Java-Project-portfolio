package com.java700.p2p.api;

import com.java700.p2p.common.api.PageResponse;
import com.java700.p2p.service.ReconciliationService;
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
@Tag(name = "p2p", description = "Procure-to-pay reconciliation (three-way match, exceptions, posting)")
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    @PostMapping("/po")
    @Operation(summary = "Register a purchase order with lines")
    @PreAuthorize("hasAnyRole('PROCUREMENT','INTEGRATION','ADMIN')")
    public String createPo(@Valid @RequestBody Api.CreatePoRequest body,
                           @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createPo(body, idemKey);
    }

    @PostMapping("/gr")
    @Operation(summary = "Post a goods receipt against a PO")
    @PreAuthorize("hasAnyRole('PROCUREMENT','INTEGRATION','ADMIN')")
    public String createGr(@Valid @RequestBody Api.CreateGrRequest body,
                           @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.createGr(body, idemKey);
    }

    @PostMapping("/invoices")
    @Operation(summary = "Ingest a supplier invoice (idempotent) and run the three-way match")
    @PreAuthorize("hasAnyRole('AP_CLERK','INTEGRATION','ADMIN')")
    public Api.InvoiceView ingest(@Valid @RequestBody Api.CreateInvoiceRequest body,
                                  @RequestHeader(value = "Idempotency-Key") String idemKey) {
        return service.ingestInvoice(body, idemKey);
    }

    @GetMapping("/po/{id}")
    @Operation(summary = "Get a purchase order with its lines and received/invoiced quantities")
    @PreAuthorize("hasAnyRole('PROCUREMENT','AP_CLERK','AP_MANAGER','AUDITOR','ADMIN')")
    public Api.PoView poView(@PathVariable String id) {
        return service.poView(id);
    }

    @GetMapping("/invoices")
    @Operation(summary = "List invoices (optional status filter)")
    @PreAuthorize("hasAnyRole('AP_CLERK','AP_MANAGER','AUDITOR','ADMIN')")
    public PageResponse<Api.InvoiceView> invoices(@RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return service.invoices(status, page, size);
    }

    @PostMapping("/invoices/{id}/match")
    @Operation(summary = "Re-run the three-way match for an invoice")
    @PreAuthorize("hasAnyRole('AP_CLERK','AP_MANAGER','ADMIN')")
    public Api.InvoiceView match(@PathVariable String id) {
        return service.match(id);
    }

    @GetMapping("/exceptions")
    @Operation(summary = "List match exceptions (optional status filter)")
    @PreAuthorize("hasAnyRole('AP_CLERK','AP_MANAGER','AUDITOR','ADMIN')")
    public PageResponse<Api.ExceptionView> exceptions(@RequestParam(required = false) String status,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return service.exceptions(status, page, size);
    }

    @PostMapping("/exceptions/{id}/assign")
    @Operation(summary = "Assign an exception to an AP clerk")
    @PreAuthorize("hasAnyRole('AP_CLERK','AP_MANAGER','ADMIN')")
    public Api.ExceptionView assign(@PathVariable String id,
                                    @Valid @RequestBody Api.AssignRequest body) {
        return service.assign(id, body.assignee());
    }

    @PostMapping("/exceptions/{id}/waive")
    @Operation(summary = "Waive an exception (four-eyes: AP_MANAGER only)")
    @PreAuthorize("hasAnyRole('AP_MANAGER','ADMIN')")
    public Api.ExceptionView waive(@PathVariable String id,
                                   @RequestHeader(value = "Idempotency-Key") String idemKey,
                                   @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.waive(id, body == null ? null : body.note(), idemKey);
    }

    @PostMapping("/exceptions/{id}/reject")
    @Operation(summary = "Reject an exception (invoice is rejected)")
    @PreAuthorize("hasAnyRole('AP_MANAGER','ADMIN')")
    public Api.ExceptionView reject(@PathVariable String id,
                                    @Valid @RequestBody(required = false) Api.DecideRequest body) {
        return service.reject(id, body == null ? null : body.note());
    }

    @GetMapping("/tolerance-rules")
    @Operation(summary = "List active tolerance rules")
    @PreAuthorize("hasAnyRole('AP_CLERK','AP_MANAGER','AUDITOR','ADMIN')")
    public List<Api.RuleView> rules() {
        return service.toleranceRules();
    }

    @PostMapping("/tolerance-rules/{id}")
    @Operation(summary = "Update a tolerance rule")
    @PreAuthorize("hasRole('ADMIN')")
    public Api.RuleView updateRule(@PathVariable String id,
                                   @Valid @RequestBody Api.UpdateRuleRequest body) {
        return service.updateRule(id, body);
    }

    @PostMapping("/batch")
    @Operation(summary = "Run the reconciliation + posting batch manually")
    @PreAuthorize("hasAnyRole('AP_MANAGER','ADMIN')")
    public Api.BatchView runBatch() {
        return service.runBatch();
    }
}
