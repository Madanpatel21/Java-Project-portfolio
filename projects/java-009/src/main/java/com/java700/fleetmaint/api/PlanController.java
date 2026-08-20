package com.java700.fleetmaint.api;

import com.java700.fleetmaint.service.Api;
import com.java700.fleetmaint.service.PlanService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Maintenance plan catalogue with parts kits. */
@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {

    private final PlanService plans;

    public PlanController(PlanService plans) {
        this.plans = plans;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','ADMIN')")
    public Api.PlanView create(@RequestBody Api.PlanRequest request) {
        return plans.view(plans.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('FLEET_MANAGER','MECHANIC','PARTS_CLERK','AUDITOR','ADMIN')")
    public List<Api.PlanView> list() {
        return plans.all().stream().map(plans::view).toList();
    }

    @PostMapping("/{planId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public Api.PlanView setActive(@PathVariable String planId, @RequestParam boolean active) {
        return plans.view(plans.setActive(planId, active));
    }
}
