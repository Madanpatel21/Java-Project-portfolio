package com.java700.fleetmaint.api;

import com.java700.fleetmaint.security.SecurityUtil;
import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.InspectionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Compliance inspection ledger and the fleet compliance report. */
@RestController
@RequestMapping("/api/v1/inspections")
public class InspectionController {

    private final InspectionService inspections;

    public InspectionController(InspectionService inspections) {
        this.inspections = inspections;
    }

    @PostMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public Api.InspectionView record(@PathVariable String vehicleId,
                                     @Valid @RequestBody Api.InspectionRequest request) {
        return inspections.view(inspections.record(vehicleId, request,
                SecurityUtil.currentUsername()));
    }

    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','FLEET_MANAGER','AUDITOR','ADMIN')")
    public List<Api.InspectionView> history(@PathVariable String vehicleId) {
        return inspections.history(vehicleId).stream().map(inspections::view).toList();
    }

    @GetMapping("/compliance-report")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','FLEET_MANAGER','AUDITOR','ADMIN')")
    public List<Api.ComplianceRow> complianceReport() {
        return inspections.complianceReport();
    }

}
