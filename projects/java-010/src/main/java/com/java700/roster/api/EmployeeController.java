package com.java700.roster.api;

import com.java700.roster.domain.Availability;
import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.domain.AvailabilityRepository;
import com.java700.roster.common.api.Problems;
import com.java700.roster.security.SecurityUtil;
import com.java700.roster.service.Api;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Employee registry and self-service availability. */
@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeRepository employees;
    private final AvailabilityRepository availabilities;
    private final Clock clock;

    public EmployeeController(EmployeeRepository employees, AvailabilityRepository availabilities,
                              Clock clock) {
        this.employees = employees;
        this.availabilities = availabilities;
        this.clock = clock;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public Api.EmployeeView create(@Valid @RequestBody Api.EmployeeRequest request) {
        if (request.empNo() == null || request.empNo().isBlank()
                || request.name() == null || request.name().isBlank()
                || request.skills() == null || request.skills().isBlank()) {
            throw new Problems.BadRequest("empNo, name and skills are required");
        }
        Employee employee = new Employee(UUID.randomUUID().toString(), null,
                request.empNo().trim(), request.name().trim(), request.department().trim(),
                request.skills().trim(),
                request.employmentType() == null ? "FULL_TIME" : request.employmentType(),
                request.maxWeeklyHours() <= 0 ? 40 : request.maxWeeklyHours(), true,
                Instant.now(clock));
        return view(employees.save(employee));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR')")
    public List<Api.EmployeeView> list() {
        return employees.findAll().stream().map(this::view).toList();
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','AUDITOR','EMPLOYEE')")
    public Api.EmployeeView get(@PathVariable String employeeId) {
        return view(employees.findById(employeeId)
                .orElseThrow(() -> new Problems.NotFound("employee " + employeeId)));
    }

    @PostMapping("/me/availability")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public AvailabilityView submitAvailability(@Valid @RequestBody Api.AvailabilityRequest request) {
        Availability availability = new Availability(UUID.randomUUID().toString(),
                SecurityUtil.currentUserId(), request.day(), request.reason(),
                Instant.now(clock));
        Availability saved = availabilities.save(availability);
        return new AvailabilityView(saved.getId(), saved.getAvailDate(), saved.getReason());
    }

    @GetMapping("/me/availability")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN')")
    public List<AvailabilityView> myAvailability() {
        return availabilities.findByEmployeeId(SecurityUtil.currentUserId()).stream()
                .map(entry -> new AvailabilityView(entry.getId(), entry.getAvailDate(),
                        entry.getReason()))
                .toList();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER','ADMIN','AUDITOR')")
    public Api.EmployeeView me() {
        return view(employees.findByUserId(SecurityUtil.currentUserId())
                .orElseThrow(() -> new Problems.NotFound("current employee")));
    }

    private Api.EmployeeView view(Employee employee) {
        return new Api.EmployeeView(employee.getId(), employee.getEmpNo(), employee.getName(),
                employee.getDepartment(),
                Arrays.asList(employee.getSkills().split(",")), employee.getEmploymentType(),
                employee.getMaxWeeklyHours(), employee.isActive());
    }

    /** Availability entry view. */
    public record AvailabilityView(String id, java.time.LocalDate day, String reason) {
    }
}
