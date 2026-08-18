package com.java700.crvs.statistics;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@Tag(name = "statistics", description = "Vital statistics from the life-event ledger")
public class StatisticsController {

    private final VitalStatistics service;

    public StatisticsController(VitalStatistics service) {
        this.service = service;
    }

    @GetMapping("/vital")
    @Operation(summary = "Births/deaths/marriages/natural-increase per region for a window")
    @PreAuthorize("hasAnyRole('STATISTICIAN','ADMIN')")
    public VitalStatistics.VitalReport vital(@RequestParam @NotNull Instant from,
                                             @RequestParam @NotNull Instant to) {
        return service.report(from, to);
    }
}
