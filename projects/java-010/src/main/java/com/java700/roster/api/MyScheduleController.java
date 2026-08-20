package com.java700.roster.api;

import com.java700.roster.common.api.Problems;
import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.domain.ShiftAssignment;
import com.java700.roster.security.SecurityUtil;
import com.java700.roster.service.Api;
import com.java700.roster.service.RosterService;
import com.java700.roster.service.StatsService;
import com.java700.roster.service.SwapService;
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

/** Employee self-service: my shifts, swap requests, roster stats. */
@RestController
@RequestMapping("/api/v1/my")
public class MyScheduleController {

    private final RosterService rosters;
    private final SwapService swaps;
    private final StatsService stats;
    private final EmployeeRepository employees;

    public MyScheduleController(RosterService rosters, SwapService swaps, StatsService stats,
                                EmployeeRepository employees) {
        this.rosters = rosters;
        this.swaps = swaps;
        this.stats = stats;
        this.employees = employees;
    }

    private Employee currentEmployee() {
        return employees.findByUserId(SecurityUtil.currentUserId())
                .orElseThrow(() -> new Problems.NotFound("current employee profile"));
    }

    @GetMapping("/shifts/{rosterId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public List<Api.ShiftView> myShifts(@PathVariable String rosterId) {
        rosters.load(rosterId);
        List<ShiftAssignment> mine = rosters.myAssignments(rosterId,
                currentEmployee().getId());
        List<String> myShiftIds = mine.stream().map(ShiftAssignment::getShiftId).toList();
        return rosters.shiftsOf(rosterId).stream()
                .filter(shift -> myShiftIds.contains(shift.getId()))
                .map(rosters::shiftView)
                .toList();
    }

    @PostMapping("/swaps")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public Api.SwapRequestView requestSwap(@Valid @RequestBody Api.SwapCreateRequest request) {
        return swaps.view(swaps.request(request, SecurityUtil.currentUserId(),
                SecurityUtil.currentUsername()));
    }

    @GetMapping("/swaps")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public List<Api.SwapRequestView> mySwaps() {
        return swaps.byRequester(SecurityUtil.currentUserId()).stream()
                .map(swaps::view)
                .toList();
    }

    @GetMapping("/stats/{rosterId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public Api.StatsView stats(@PathVariable String rosterId) {
        return stats.stats(rosterId);
    }
}
