package com.java700.workforce.compliance;

import com.java700.workforce.common.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "compliance", description = "Violation lifecycle and correlation engine")
public class ComplianceController {

    private final ViolationService violationService;
    private final CorrelationJob correlationJob;

    public ComplianceController(ViolationService violationService, CorrelationJob correlationJob) {
        this.violationService = violationService;
        this.correlationJob = correlationJob;
    }

    @GetMapping("/violations")
    @Operation(summary = "List violations (optionally filtered by status)")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public PageResponse<ComplianceApi.ViolationView> list(@RequestParam(required = false) String status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return violationService.list(status, page, size);
    }

    @PostMapping("/violations/{id}/acknowledge")
    @Operation(summary = "Acknowledge an open violation")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','COMPLIANCE_ADMIN')")
    public ComplianceApi.ViolationView acknowledge(@PathVariable String id) {
        return violationService.acknowledge(id);
    }

    @PostMapping("/violations/{id}/remediate")
    @Operation(summary = "Record remediation of a violation")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','COMPLIANCE_ADMIN')")
    public ComplianceApi.ViolationView remediate(@PathVariable String id,
                                                 @Valid @RequestBody(required = false) ComplianceApi.RemediateRequest body) {
        return violationService.remediate(id, body == null ? null : body.note());
    }

    @PostMapping("/violations/{id}/close")
    @Operation(summary = "Close a remediated violation")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','COMPLIANCE_ADMIN')")
    public ComplianceApi.ViolationView close(@PathVariable String id) {
        return violationService.close(id);
    }

    @PostMapping("/compliance/run")
    @Operation(summary = "Trigger a correlation run on demand")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','COMPLIANCE_ADMIN')")
    public ComplianceApi.RunResult run() {
        return correlationJob.run();
    }
}
