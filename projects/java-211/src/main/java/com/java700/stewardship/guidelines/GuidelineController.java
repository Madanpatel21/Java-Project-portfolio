package com.java700.stewardship.guidelines;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guidelines")
@Tag(name = "guidelines", description = "Versioned stewardship guideline sets")
public class GuidelineController {

    private final GuidelineService service;

    public GuidelineController(GuidelineService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List guideline versions (newest first)")
    @PreAuthorize("isAuthenticated()")
    public List<GuidelineApi.GuidelineView> versions() {
        return service.versions();
    }

    @PostMapping
    @Operation(summary = "Create and activate a new immutable guideline version")
    @PreAuthorize("hasRole('STEWARDSHIP_ADMIN')")
    public GuidelineApi.GuidelineView createVersion(@Valid @RequestBody GuidelineApi.CreateVersionRequest body) {
        return service.createVersion(body.rulesJson());
    }
}
