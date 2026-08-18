package com.java700.workforce.policy;

import com.java700.workforce.common.api.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
@Tag(name = "policies", description = "Versioned compliance policies and rule sets")
public class PolicyController {

    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List policies")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<PolicyApi.PolicyView> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.list(page, Math.min(size, 100));
    }

    @GetMapping("/{code}/versions")
    @Operation(summary = "List versions of a policy (newest first)")
    @PreAuthorize("isAuthenticated()")
    public List<PolicyApi.VersionView> versions(@PathVariable String code) {
        return service.versions(code);
    }

    @PostMapping("/{code}/versions")
    @Operation(summary = "Create and activate a new immutable policy version")
    @PreAuthorize("hasRole('COMPLIANCE_ADMIN')")
    public PolicyApi.VersionView createVersion(@PathVariable String code,
                                               @Valid @RequestBody PolicyApi.CreateVersionRequest body) {
        return service.createVersion(code, body.rulesJson());
    }
}
