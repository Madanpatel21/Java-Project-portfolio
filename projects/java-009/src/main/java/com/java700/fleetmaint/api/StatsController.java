package com.java700.fleetmaint.api;

import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.StatsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Fleet-wide SLA and cost analytics. */
@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService stats;

    public StatsController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','AUDITOR','ADMIN')")
    public Api.StatsView stats() {
        return stats.stats();
    }
}
