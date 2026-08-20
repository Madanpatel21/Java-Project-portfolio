package com.java700.roster.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolutionManager;
import ai.timefold.solver.core.api.solver.SolverConfigOverride;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.config.solver.termination.TerminationConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.roster.common.api.Problems;
import com.java700.roster.common.audit.AuditLogService;
import com.java700.roster.domain.Availability;
import com.java700.roster.domain.AvailabilityRepository;
import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.domain.Roster;
import com.java700.roster.domain.RosterRepository;
import com.java700.roster.domain.Shift;
import com.java700.roster.domain.ShiftAssignment;
import com.java700.roster.domain.ShiftAssignmentRepository;
import com.java700.roster.domain.ShiftRepository;
import com.java700.roster.messaging.DomainEventBus;
import com.java700.roster.messaging.RosterEvents;
import com.java700.roster.observability.Metrics;
import com.java700.roster.solver.PlannedShift;
import com.java700.roster.solver.RosterSolution;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Timefold integration: builds the planning problem from persisted data, solves it,
 * persists the assignment set and produces an auditor-friendly score explanation.
 */
@Service
public class SolverService {

    private static final Logger log = LoggerFactory.getLogger(SolverService.class);
    private static final AtomicLong PROBLEM_IDS = new AtomicLong(1);

    private final SolverManager<RosterSolution, Long> solverManager;
    private final SolutionManager<RosterSolution, HardSoftScore> solutionManager;
    private final RosterRepository rosters;
    private final ShiftRepository shifts;
    private final ShiftAssignmentRepository assignments;
    private final EmployeeRepository employees;
    private final AvailabilityRepository availabilities;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final DomainEventBus events;
    private final Duration solverTimeLimit;

    public SolverService(SolverManager<RosterSolution, Long> solverManager,
                         SolutionManager<RosterSolution, HardSoftScore> solutionManager,
                         RosterRepository rosters, ShiftRepository shifts,
                         ShiftAssignmentRepository assignments, EmployeeRepository employees,
                         AvailabilityRepository availabilities, AuditLogService audit,
                         Metrics metrics, ObjectMapper mapper, DomainEventBus events,
                         @Value("${app.roster.solver-time-limit:5s}") Duration solverTimeLimit) {
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
        this.rosters = rosters;
        this.shifts = shifts;
        this.assignments = assignments;
        this.employees = employees;
        this.availabilities = availabilities;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.events = events;
        this.solverTimeLimit = solverTimeLimit;
    }

    /** Solves the roster and persists the resulting assignment set. */
    @Transactional
    public Api.OptimizeResult optimize(String rosterId, String username) {
        Roster roster = rosters.findById(rosterId)
                .orElseThrow(() -> new Problems.NotFound("roster " + rosterId));
        List<Employee> team = employees.findByActiveTrue().stream()
                .filter(employee -> roster.getDepartment().equals(employee.getDepartment()))
                .toList();
        if (team.isEmpty()) {
            throw new Problems.Conflict("no active employees in department "
                    + roster.getDepartment());
        }
        List<Availability> availabilityList = availabilities.findAll();
        List<Shift> rosterShifts = shifts.findByRosterIdOrderByShiftDateAsc(rosterId);
        List<PlannedShift> planned = new ArrayList<>();
        for (Shift shift : rosterShifts) {
            planned.add(new PlannedShift(UUID.randomUUID().toString(), shift.getId(),
                    shift.getShiftDate(), shift.getShiftType(), shift.getStartHour(),
                    shift.getDurationHours(), shift.getRequiredSkill()));
        }
        long problemId = PROBLEM_IDS.getAndIncrement();
        RosterSolution problem = new RosterSolution(problemId, team, availabilityList, planned);

        Instant start = Instant.now();
        SolverConfigOverride<RosterSolution> override = new SolverConfigOverride<RosterSolution>()
                .withTerminationConfig(new TerminationConfig().withSpentLimit(solverTimeLimit));
        RosterSolution solved;
        try {
            solved = solverManager.solveBuilder()
                    .withProblemId(problemId)
                    .withProblem(problem)
                    .withConfigOverride(override)
                    .run()
                    .getFinalBestSolution();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("solver interrupted", ex);
        } catch (java.util.concurrent.ExecutionException ex) {
            throw new IllegalStateException("solver failed", ex);
        }
        HardSoftScore score = solved.getScore();
        metrics.optimizationDuration().record(Duration.between(start, Instant.now()));
        metrics.rosterOptimized();

        persistAssignments(rosterId, solved);

        Map<String, Object> breakdown = constraintBreakdown(solved);
        String breakdownJson;
        try {
            breakdownJson = mapper.writeValueAsString(breakdown);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("cannot serialize constraint breakdown", ex);
        }
        Roster updated = new Roster(roster.getId(), roster.getName(), roster.getDepartment(),
                roster.getStartDate(), roster.getEndDate(), roster.getStatus(), breakdownJson,
                roster.getPublishedAt(), roster.getCreatedAt());
        rosters.save(updated);
        audit.record("ROSTER_OPTIMIZED", "roster", rosterId,
                "score=" + score + " assigned=" + solved.assignedCount() + "/"
                        + solved.getPlannedShifts().size() + " by=" + username);
        events.publish(new RosterEvents.RosterOptimized(UUID.randomUUID().toString(),
                Instant.now(), rosterId, score.toString()));
        log.info("Roster {} optimized: {} ({}ms)", rosterId, score,
                Duration.between(start, Instant.now()).toMillis());
        return new Api.OptimizeResult(rosterId, score.isFeasible(), score.toString(),
                solved.getPlannedShifts().size(), solved.assignedCount(), breakdownJson);
    }

    /** Explains the current assignment set against the constraint rules (auditor view). */
    @Transactional(readOnly = true)
    public Api.ExplainResult explain(String rosterId) {
        RosterSolution solution = buildFromPersisted(rosterId);
        ai.timefold.solver.core.api.score.ScoreExplanation<RosterSolution, HardSoftScore>
                explanation = solutionManager.explain(solution);
        HardSoftScore score = explanation.getScore();
        List<Api.ConstraintMatchView> matches = explanation.getConstraintMatchTotalMap()
                .values().stream()
                .map(total -> new Api.ConstraintMatchView(total.getConstraintName(),
                        total.getConstraintMatchCount(), total.getScore().hardScore(),
                        total.getScore().softScore()))
                .sorted(Comparator.comparingInt(Api.ConstraintMatchView::hard)
                        .thenComparingInt(Api.ConstraintMatchView::soft))
                .toList();
        return new Api.ExplainResult(score.toString(), score.isFeasible(), matches);
    }

    private void persistAssignments(String rosterId, RosterSolution solved) {
        Map<String, String> employeeByShift = new HashMap<>();
        for (PlannedShift plannedShift : solved.getPlannedShifts()) {
            if (plannedShift.getEmployee() != null) {
                employeeByShift.put(plannedShift.getShiftId(),
                        plannedShift.getEmployee().getId());
            }
        }
        List<ShiftAssignment> existing = assignments.findByRosterId(rosterId);
        for (ShiftAssignment assignment : existing) {
            String employeeId = employeeByShift.get(assignment.getShiftId());
            if (employeeId != null) {
                ShiftAssignment refreshed = new ShiftAssignment(assignment.getId(),
                        assignment.getRosterId(), assignment.getShiftId(), employeeId,
                        ShiftAssignment.STATUS_ASSIGNED, Instant.now());
                assignments.save(refreshed);
            } else {
                ShiftAssignment refreshed = new ShiftAssignment(assignment.getId(),
                        assignment.getRosterId(), assignment.getShiftId(), null,
                        ShiftAssignment.STATUS_UNASSIGNED, null);
                assignments.save(refreshed);
            }
        }
    }

    private RosterSolution buildFromPersisted(String rosterId) {
        Roster roster = rosters.findById(rosterId).orElseThrow();
        List<Employee> team = employees.findByActiveTrue().stream()
                .filter(employee -> roster.getDepartment().equals(employee.getDepartment()))
                .toList();
        Map<String, String> employeeByShift = new HashMap<>();
        for (ShiftAssignment assignment : assignments.findByRosterId(rosterId)) {
            if (assignment.getEmployeeId() != null) {
                employeeByShift.put(assignment.getShiftId(), assignment.getEmployeeId());
            }
        }
        Map<String, Employee> employeeById = new HashMap<>();
        for (Employee employee : team) {
            employeeById.put(employee.getId(), employee);
        }
        List<PlannedShift> planned = new ArrayList<>();
        for (Shift shift : shifts.findByRosterIdOrderByShiftDateAsc(rosterId)) {
            PlannedShift plannedShift = new PlannedShift(UUID.randomUUID().toString(),
                    shift.getId(), shift.getShiftDate(), shift.getShiftType(),
                    shift.getStartHour(), shift.getDurationHours(), shift.getRequiredSkill());
            plannedShift.setEmployee(employeeById.get(employeeByShift.get(shift.getId())));
            planned.add(plannedShift);
        }
        RosterSolution solution = new RosterSolution(PROBLEM_IDS.getAndIncrement(), team,
                availabilities.findAll(), planned);
        return solution;
    }

    private Map<String, Object> constraintBreakdown(RosterSolution solved) {
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("score", solved.getScore().toString());
        breakdown.put("feasible", solved.getScore().isFeasible());
        breakdown.put("totalShifts", solved.getPlannedShifts().size());
        breakdown.put("assigned", solved.assignedCount());
        Map<String, Integer> perConstraint = new HashMap<>();
        solutionManager.explain(solved).getConstraintMatchTotalMap().values()
                .forEach(total -> {
                    int severity = total.getScore().hardScore() != 0
                            ? total.getScore().hardScore()
                            : total.getScore().softScore();
                    perConstraint.merge(total.getConstraintName(), Math.abs(severity),
                            Integer::sum);
                });
        breakdown.put("constraints", perConstraint);
        return breakdown;
    }
}
