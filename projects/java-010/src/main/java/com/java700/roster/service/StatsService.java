package com.java700.roster.service;

import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.domain.Shift;
import com.java700.roster.domain.ShiftAssignment;
import com.java700.roster.domain.ShiftAssignmentRepository;
import com.java700.roster.domain.ShiftRepository;
import com.java700.roster.domain.SwapRequest;
import com.java700.roster.domain.SwapRequestRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Coverage and fairness analytics across the latest roster. */
@Service
public class StatsService {

    private final EmployeeRepository employees;
    private final ShiftAssignmentRepository assignments;
    private final ShiftRepository shifts;
    private final SwapRequestRepository swaps;

    public StatsService(EmployeeRepository employees, ShiftAssignmentRepository assignments,
                        ShiftRepository shifts, SwapRequestRepository swaps) {
        this.employees = employees;
        this.assignments = assignments;
        this.shifts = shifts;
        this.swaps = swaps;
    }

    @Transactional(readOnly = true)
    public Api.StatsView stats(String rosterId) {
        List<ShiftAssignment> rosterAssignments = assignments.findByRosterId(rosterId);
        int assigned = (int) rosterAssignments.stream()
                .filter(a -> a.getEmployeeId() != null
                        && !ShiftAssignment.STATUS_UNASSIGNED.equals(a.getStatus()))
                .count();
        int total = rosterAssignments.size();
        double coveragePct = total == 0 ? 0.0 : Math.round(assigned * 1000.0 / total) / 10.0;

        Map<String, Integer> hoursByEmployee = new HashMap<>();
        for (ShiftAssignment assignment : rosterAssignments) {
            if (assignment.getEmployeeId() == null) {
                continue;
            }
            Shift shift = shifts.findById(assignment.getShiftId()).orElse(null);
            if (shift != null) {
                hoursByEmployee.merge(assignment.getEmployeeId(), shift.getDurationHours(),
                        Integer::sum);
            }
        }
        List<Integer> hourValues = new ArrayList<>(hoursByEmployee.values());
        double mean = hourValues.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = hourValues.stream()
                .mapToDouble(hours -> Math.pow(hours - mean, 2))
                .average().orElse(0.0);
        double stdDev = Math.round(Math.sqrt(variance) * 100.0) / 100.0;

        List<Api.EmployeeHours> perEmployee = new ArrayList<>();
        for (Employee employee : employees.findByActiveTrue()) {
            perEmployee.add(new Api.EmployeeHours(employee.getEmpNo(), employee.getName(),
                    hoursByEmployee.getOrDefault(employee.getId(), 0)));
        }
        perEmployee.sort(Comparator.comparing(Api.EmployeeHours::hours).reversed());
        int pendingSwaps = swaps.findByStatusOrderByCreatedAtAsc(SwapRequest.STATUS_PENDING).size();
        return new Api.StatsView(employees.findByActiveTrue().size(), total, assigned,
                total - assigned, coveragePct, stdDev, perEmployee, pendingSwaps);
    }
}
