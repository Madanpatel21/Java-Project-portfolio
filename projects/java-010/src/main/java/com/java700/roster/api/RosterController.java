package com.java700.roster.api;

import com.java700.roster.domain.Roster;
import com.java700.roster.security.SecurityUtil;
import com.java700.roster.service.Api;
import com.java700.roster.service.RosterService;
import com.java700.roster.service.SolverService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Roster lifecycle: creation from demand curves, optimization, publishing, views. */
@RestController
@RequestMapping("/api/v1/rosters")
public class RosterController {

    private final RosterService rosters;
    private final SolverService solver;

    public RosterController(RosterService rosters, SolverService solver) {
        this.rosters = rosters;
        this.solver = solver;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.RosterView create(@Valid @RequestBody Api.RosterRequest request) {
        Roster roster = rosters.create(request, SecurityUtil.currentUsername());
        return rosters.view(roster);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public List<Api.RosterView> list() {
        return rosters.all().stream().map(rosters::view).toList();
    }

    @GetMapping("/{rosterId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR','EMPLOYEE')")
    public Api.RosterView get(@PathVariable String rosterId) {
        return rosters.view(rosters.load(rosterId));
    }

    @GetMapping("/{rosterId}/shifts")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR','EMPLOYEE')")
    public List<Api.ShiftView> shifts(@PathVariable String rosterId) {
        return rosters.shiftViews(rosterId);
    }

    @PostMapping("/{rosterId}/optimize")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.OptimizeResult optimize(@PathVariable String rosterId) {
        return solver.optimize(rosterId, SecurityUtil.currentUsername());
    }

    @GetMapping("/{rosterId}/explain")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public Api.ExplainResult explain(@PathVariable String rosterId) {
        return solver.explain(rosterId);
    }

    @PostMapping("/{rosterId}/publish")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.RosterView publish(@PathVariable String rosterId) {
        return rosters.view(rosters.publish(rosterId, SecurityUtil.currentUsername()));
    }
}
