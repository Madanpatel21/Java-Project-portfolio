package com.java700.govault.api;

import com.java700.govault.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dev-only time-travel for retention testing. Never present in local/production profiles. */
@RestController
@RequestMapping("/api/v1/dev")
@Profile("dev")
@Tag(name = "dev", description = "Dev-only simulation facilities")
public class DevTimeController {

    private final DocumentService service;

    public DevTimeController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/documents/{id}/elapse-days/{days}")
    @Operation(summary = "DEV ONLY: shift a document's upload date back N days to exercise retention")
    @PreAuthorize("hasRole('ADMIN')")
    public void elapseDays(@PathVariable String id, @PathVariable int days) {
        service.backdateForTest(id, days);
    }
}
