package com.java700.stewardship.metrics;

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
@RequestMapping("/api/v1/metrics")
@Tag(name = "metrics", description = "DOT / patient-days / DDD utilization analytics")
public class MetricsController {

    private final UtilizationMetrics service;

    public MetricsController(UtilizationMetrics service) {
        this.service = service;
    }

    @GetMapping("/utilization")
    @Operation(summary = "Compute utilization metrics for a window (optional ward filter)")
    @PreAuthorize("hasAnyRole('PHARMACIST','ID_PHYSICIAN','INFECTION_CONTROL','STEWARDSHIP_ADMIN')")
    public UtilizationMetrics.MetricsReport utilization(@RequestParam @NotNull Instant from,
                                                        @RequestParam @NotNull Instant to,
                                                        @RequestParam(required = false) String ward) {
        return service.compute(from, to, ward);
    }
}
