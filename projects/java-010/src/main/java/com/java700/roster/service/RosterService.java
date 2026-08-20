package com.java700.roster.service;

import com.java700.roster.common.api.Problems;
import com.java700.roster.common.audit.AuditLogService;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Roster and shift generation from demand curves, plus the publish lifecycle.
 * Publishing requires a feasible optimized assignment set.
 */
@Service
public class RosterService {

    private static final Set<String> SHIFT_TYPES = Set.of("MORNING", "AFTERNOON", "NIGHT");

    private final RosterRepository rosters;
    private final ShiftRepository shifts;
    private final ShiftAssignmentRepository assignments;
    private final EmployeeRepository employees;
    private final AuditLogService audit;
    private final Clock clock;
    private final DomainEventBus events;

    public RosterService(RosterRepository rosters, ShiftRepository shifts,
                         ShiftAssignmentRepository assignments, EmployeeRepository employees,
                         AuditLogService audit, Clock clock, DomainEventBus events) {
        this.rosters = rosters;
        this.shifts = shifts;
        this.assignments = assignments;
        this.employees = employees;
        this.audit = audit;
        this.clock = clock;
        this.events = events;
    }

    /** Creates a DRAFT roster and materialises its demand curve into shift rows. */
    @Transactional
    public Roster create(Api.RosterRequest request, String username) {
        validate(request);
        LocalDate endDate = request.startDate().plusDays(request.days() - 1);
        Roster roster = new Roster(UUID.randomUUID().toString(), request.name().trim(),
                request.department().trim(), request.startDate(), endDate, Roster.STATUS_DRAFT,
                null, null, Instant.now(clock));
        Roster saved = rosters.save(roster);
        for (int day = 0; day < request.days(); day++) {
            LocalDate date = request.startDate().plusDays(day);
            for (Api.DemandSlot slot : request.demand()) {
                for (int i = 0; i < slot.headcount(); i++) {
                    Shift shift = new Shift(UUID.randomUUID().toString(), saved.getId(), date,
                            slot.shiftType(), slot.startHour(), slot.durationHours(),
                            slot.requiredSkill(), 1);
                    shifts.save(shift);
                    assignments.save(new ShiftAssignment(UUID.randomUUID().toString(),
                            saved.getId(), shift.getId(), null,
                            ShiftAssignment.STATUS_UNASSIGNED, null));
                }
            }
        }
        audit.record("ROSTER_CREATED", "roster", saved.getId(),
                "name=" + saved.getName() + " " + saved.getStartDate() + ".." + saved.getEndDate()
                        + " by=" + username);
        return saved;
    }

    /** Publishes a roster whose optimized assignment set is complete and feasible. */
    @Transactional
    public Roster publish(String rosterId, String username) {
        Roster roster = load(rosterId);
        if (!Roster.STATUS_DRAFT.equals(roster.getStatus())) {
            throw new Problems.Conflict("roster " + roster.getName() + " is " + roster.getStatus());
        }
        long unassigned = assignments.findByRosterIdAndStatus(rosterId,
                ShiftAssignment.STATUS_UNASSIGNED).size();
        if (roster.getScoreJson() == null || unassigned > 0) {
            throw new Problems.Conflict("roster must be optimized with full coverage before "
                    + "publishing (" + unassigned + " unassigned slots)");
        }
        roster.markPublished(Instant.now(clock));
        Roster saved = rosters.save(roster);
        audit.record("ROSTER_PUBLISHED", "roster", saved.getId(), "by=" + username);
        events.publish(new RosterEvents.RosterPublished(UUID.randomUUID().toString(),
                Instant.now(clock), saved.getId(), saved.getName()));
        return saved;
    }

    @Transactional
    public Roster archive(String rosterId, String username) {
        Roster roster = load(rosterId);
        roster.transition(Roster.STATUS_ARCHIVED);
        Roster saved = rosters.save(roster);
        audit.record("ROSTER_ARCHIVED", "roster", saved.getId(), "by=" + username);
        return saved;
    }

    @Transactional(readOnly = true)
    public Roster load(String rosterId) {
        return rosters.findById(rosterId)
                .orElseThrow(() -> new Problems.NotFound("roster " + rosterId));
    }

    @Transactional(readOnly = true)
    public List<Roster> all() {
        return rosters.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Shift> shiftsOf(String rosterId) {
        return shifts.findByRosterIdOrderByShiftDateAsc(rosterId);
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignment> assignmentsOf(String rosterId) {
        return assignments.findByRosterId(rosterId);
    }

    @Transactional(readOnly = true)
    public List<ShiftAssignment> myAssignments(String rosterId, String employeeId) {
        return assignments.findByRosterIdAndEmployeeId(rosterId, employeeId);
    }

    @Transactional(readOnly = true)
    public Api.RosterView view(Roster roster) {
        List<ShiftAssignment> rosterAssignments = assignments.findByRosterId(roster.getId());
        int assigned = (int) rosterAssignments.stream()
                .filter(a -> ShiftAssignment.STATUS_ASSIGNED.equals(a.getStatus())
                        || ShiftAssignment.STATUS_CONFIRMED.equals(a.getStatus()))
                .count();
        return new Api.RosterView(roster.getId(), roster.getName(), roster.getDepartment(),
                roster.getStartDate(), roster.getEndDate(), roster.getStatus(),
                rosterAssignments.size(), assigned, roster.getScoreJson(),
                roster.getPublishedAt());
    }

    @Transactional(readOnly = true)
    public Api.ShiftView shiftView(Shift shift) {
        ShiftAssignment assignment = assignments.findByRosterId(shift.getRosterId()).stream()
                .filter(a -> shift.getId().equals(a.getShiftId()))
                .findFirst().orElse(null);
        Employee employee = assignment != null && assignment.getEmployeeId() != null
                ? employees.findById(assignment.getEmployeeId()).orElse(null)
                : null;
        return new Api.ShiftView(shift.getId(),
                assignment == null ? null : assignment.getId(),
                shift.getShiftDate().toString(), shift.getShiftType(), shift.getStartHour(),
                shift.getDurationHours(), shift.getRequiredSkill(),
                employee == null ? null : employee.getEmpNo(),
                employee == null ? null : employee.getName());
    }

    @Transactional(readOnly = true)
    public List<Api.ShiftView> shiftViews(String rosterId) {
        List<Api.ShiftView> views = new ArrayList<>();
        for (Shift shift : shifts.findByRosterIdOrderByShiftDateAsc(rosterId)) {
            views.add(shiftView(shift));
        }
        views.sort(Comparator.comparing(Api.ShiftView::shiftDate)
                .thenComparingInt(Api.ShiftView::startHour));
        return views;
    }

    private void validate(Api.RosterRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new Problems.BadRequest("name is required");
        }
        if (request.department() == null || request.department().isBlank()) {
            throw new Problems.BadRequest("department is required");
        }
        if (request.startDate() == null || request.startDate().isBefore(LocalDate.now(clock))) {
            throw new Problems.BadRequest("startDate must be today or later");
        }
        if (request.days() < 1 || request.days() > 31) {
            throw new Problems.BadRequest("days must be between 1 and 31");
        }
        if (request.demand() == null || request.demand().isEmpty()) {
            throw new Problems.BadRequest("demand template is required");
        }
        for (Api.DemandSlot slot : request.demand()) {
            if (!SHIFT_TYPES.contains(slot.shiftType())) {
                throw new Problems.BadRequest("shiftType must be one of " + SHIFT_TYPES);
            }
            if (slot.headcount() < 1 || slot.durationHours() < 1) {
                throw new Problems.BadRequest("headcount and durationHours must be positive");
            }
        }
    }
}
