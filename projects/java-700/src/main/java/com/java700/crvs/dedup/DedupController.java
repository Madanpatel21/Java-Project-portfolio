package com.java700.crvs.dedup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dedup")
@Tag(name = "dedup", description = "Duplicate-identity candidate adjudication")
public class DedupController {

    private final DedupService service;

    public DedupController(DedupService service) {
        this.service = service;
    }

    @GetMapping("/open")
    @Operation(summary = "List OPEN duplicate candidates")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DedupApi.CandidateView> open() {
        return service.open();
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a duplicate candidate")
    @PreAuthorize("hasRole('ADMIN')")
    public DedupApi.CandidateView confirm(@PathVariable String id) {
        return service.confirm(id);
    }

    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss a duplicate candidate")
    @PreAuthorize("hasRole('ADMIN')")
    public DedupApi.CandidateView dismiss(@PathVariable String id) {
        return service.dismiss(id);
    }
}
