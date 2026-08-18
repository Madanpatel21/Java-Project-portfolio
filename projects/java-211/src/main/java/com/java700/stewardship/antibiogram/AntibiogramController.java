package com.java700.stewardship.antibiogram;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/antibiogram")
@Tag(name = "antibiogram", description = "Hospital-wide susceptibility aggregation (first-isolate rules)")
public class AntibiogramController {

    private final AntibiogramService service;

    public AntibiogramController(AntibiogramService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Compute the antibiogram (rows under the minimum isolate count are not reportable)")
    @PreAuthorize("hasAnyRole('MICROBIOLOGIST','ID_PHYSICIAN','INFECTION_CONTROL','STEWARDSHIP_ADMIN')")
    public AntibiogramService.AntibiogramReport report() {
        return service.report();
    }
}
