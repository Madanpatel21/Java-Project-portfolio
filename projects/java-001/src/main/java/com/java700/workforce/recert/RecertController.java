package com.java700.workforce.recert;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recertification")
@Tag(name = "recertification", description = "Periodic access review campaigns")
public class RecertController {

    private final RecertService service;

    public RecertController(RecertService service) {
        this.service = service;
    }

    @PostMapping("/campaigns")
    @Operation(summary = "Generate a recertification campaign")
    @PreAuthorize("hasRole('COMPLIANCE_ADMIN')")
    public RecertApi.CampaignView generate(@Valid @RequestBody RecertApi.GenerateCampaignRequest body) {
        return service.generateCampaign(body.name(), body.windowDays());
    }

    @GetMapping("/campaigns")
    @Operation(summary = "List recertification campaigns")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public List<RecertApi.CampaignView> campaigns() {
        return service.campaigns();
    }

    @PostMapping("/campaigns/{campaignId}/decisions")
    @Operation(summary = "Record a KEEP/REVOKE decision on a grant")
    @PreAuthorize("hasAnyRole('ACCESS_MANAGER','COMPLIANCE_ADMIN')")
    public RecertApi.DecisionView decide(@PathVariable String campaignId,
                                         @Valid @RequestBody RecertApi.DecideRequest body) {
        return service.decide(campaignId, body.grantId(), body.decision());
    }

    @GetMapping("/campaigns/{campaignId}/decisions")
    @Operation(summary = "List decisions in a campaign")
    @PreAuthorize("hasAnyRole('AUDITOR','COMPLIANCE_ADMIN','COMPLIANCE_OFFICER','ACCESS_MANAGER')")
    public List<RecertApi.DecisionView> decisions(@PathVariable String campaignId) {
        return service.decisions(campaignId);
    }
}
