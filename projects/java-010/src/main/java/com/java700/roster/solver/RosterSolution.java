package com.java700.roster.solver;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.java700.roster.domain.Availability;
import com.java700.roster.domain.Employee;
import java.util.List;

/** The rostering planning problem: staff every demand slot with a qualified employee. */
@PlanningSolution
public class RosterSolution {

    private Long rosterId;
    private List<Employee> employees;
    private List<Availability> availabilities;
    private List<PlannedShift> plannedShifts;
    private HardSoftScore score;

    public RosterSolution() {
    }

    public RosterSolution(Long rosterId, List<Employee> employees,
                          List<Availability> availabilities, List<PlannedShift> plannedShifts) {
        this.rosterId = rosterId;
        this.employees = employees;
        this.availabilities = availabilities;
        this.plannedShifts = plannedShifts;
    }

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "employeeRange")
    public List<Employee> getEmployees() {
        return employees;
    }

    @ProblemFactCollectionProperty
    public List<Availability> getAvailabilities() {
        return availabilities;
    }

    @PlanningEntityCollectionProperty
    public List<PlannedShift> getPlannedShifts() {
        return plannedShifts;
    }

    @PlanningScore
    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore newScore) {
        this.score = newScore;
    }

    @PlanningId
    public Long getRosterId() {
        return rosterId;
    }

    public int assignedCount() {
        return (int) plannedShifts.stream()
                .filter(shift -> shift.getEmployee() != null)
                .count();
    }

    @Override
    public String toString() {
        return "RosterSolution{roster=" + rosterId + ", shifts=" + plannedShifts.size()
                + ", score=" + score + "}";
    }
}
