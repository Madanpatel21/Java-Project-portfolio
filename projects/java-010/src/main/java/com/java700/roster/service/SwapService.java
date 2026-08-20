package com.java700.roster.service;

import com.java700.roster.common.api.Problems;
import com.java700.roster.common.audit.AuditLogService;
import com.java700.roster.domain.AvailabilityRepository;
import com.java700.roster.domain.Employee;
import com.java700.roster.domain.EmployeeRepository;
import com.java700.roster.domain.Shift;
import com.java700.roster.domain.ShiftAssignment;
import com.java700.roster.domain.ShiftAssignmentRepository;
import com.java700.roster.domain.ShiftRepository;
import com.java700.roster.domain.SwapRequest;
import com.java700.roster.domain.SwapRequestRepository;
import com.java700.roster.messaging.DomainEventBus;
import com.java700.roster.messaging.RosterEvents;
import com.java700.roster.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shift swap (cover/exchange) workflow: employees request, managers approve.
 * Approvals re-validate skill, availability and double-booking, then move the
 * assignments — a two-sided exchange when the target also works that day.
 */
@Service
public class SwapService {

    private final SwapRequestRepository swaps;
    private final ShiftAssignmentRepository assignments;
    private final ShiftRepository shifts;
    private final EmployeeRepository employees;
    private final AvailabilityRepository availabilities;
    private final DomainEventBus events;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;

    public SwapService(SwapRequestRepository swaps, ShiftAssignmentRepository assignments,
                       ShiftRepository shifts, EmployeeRepository employees,
                       AvailabilityRepository availabilities, DomainEventBus events,
                       AuditLogService audit, Metrics metrics, Clock clock) {
        this.swaps = swaps;
        this.assignments = assignments;
        this.shifts = shifts;
        this.employees = employees;
        this.availabilities = availabilities;
        this.events = events;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
    }

    @Transactional
    public SwapRequest request(Api.SwapCreateRequest request, String requesterEmployeeId,
                               String username) {
        ShiftAssignment assignment = assignments.findById(request.assignmentId())
                .orElseThrow(() -> new Problems.NotFound(
                        "assignment " + request.assignmentId()));
        if (!ShiftAssignment.STATUS_ASSIGNED.equals(assignment.getStatus())
                && !ShiftAssignment.STATUS_CONFIRMED.equals(assignment.getStatus())) {
            throw new Problems.Conflict("assignment is " + assignment.getStatus());
        }
        Employee requester = employees.findByUserId(requesterEmployeeId)
                .orElseThrow(() -> new Problems.NotFound("employee profile of current user"));
        if (!requester.getId().equals(assignment.getEmployeeId())) {
            throw new Problems.Conflict("only the assigned employee can request a swap");
        }
        Employee target = employees.findById(request.targetEmployeeId())
                .orElseThrow(() -> new Problems.NotFound(
                        "employee " + request.targetEmployeeId()));
        validateTarget(assignment, target);
        SwapRequest swap = new SwapRequest(UUID.randomUUID().toString(), nextSwapNo(),
                assignment.getId(), requesterEmployeeId, target.getId(),
                trimToNull(request.reason()), SwapRequest.STATUS_PENDING, null, null,
                Instant.now(clock));
        SwapRequest saved = swaps.save(swap);
        audit.record("SWAP_REQUESTED", "swap_request", saved.getSwapNo(),
                "assignment=" + assignment.getId() + " target=" + target.getEmpNo()
                        + " by=" + username);
        return saved;
    }

    /** Manager decision; PENDING -> APPROVED/REJECTED with re-validation on approval. */
    @Transactional
    public SwapRequest decide(String swapId, String decision, String username) {
        if (!SwapRequest.STATUS_APPROVED.equals(decision)
                && !SwapRequest.STATUS_REJECTED.equals(decision)) {
            throw new Problems.BadRequest("decision must be APPROVED or REJECTED");
        }
        SwapRequest swap = swaps.findById(swapId)
                .orElseThrow(() -> new Problems.NotFound("swap request " + swapId));
        if (!SwapRequest.STATUS_PENDING.equals(swap.getStatus())) {
            throw new Problems.Conflict("swap " + swap.getSwapNo() + " is " + swap.getStatus());
        }
        if (SwapRequest.STATUS_REJECTED.equals(decision)) {
            swap.decide(SwapRequest.STATUS_REJECTED, username, Instant.now(clock));
            metrics.swapRejected();
            audit.record("SWAP_REJECTED", "swap_request", swap.getSwapNo(), "by=" + username);
            return swaps.save(swap);
        }
        ShiftAssignment assignment = assignments.findById(swap.getAssignmentId())
                .orElseThrow(() -> new Problems.NotFound("assignment " + swap.getAssignmentId()));
        Employee target = employees.findById(swap.getTargetEmployeeId())
                .orElseThrow(() -> new Problems.NotFound("employee " + swap.getTargetEmployeeId()));
        validateTarget(assignment, target);
        executeSwap(assignment, target);
        swap.decide(SwapRequest.STATUS_APPROVED, username, Instant.now(clock));
        SwapRequest saved = swaps.save(swap);
        metrics.swapApproved();
        audit.record("SWAP_APPROVED", "swap_request", saved.getSwapNo(), "by=" + username);
        events.publish(new RosterEvents.SwapApproved(UUID.randomUUID().toString(),
                Instant.now(clock), saved.getSwapNo(), assignment.getShiftId(),
                target.getEmpNo()));
        return saved;
    }

    /** Moves the assignment to the target; exchanges when the target works the same day. */
    private void executeSwap(ShiftAssignment assignment, Employee target) {
        Shift shift = shifts.findById(assignment.getShiftId())
                .orElseThrow(() -> new Problems.NotFound("shift " + assignment.getShiftId()));
        ShiftAssignment counterpart = assignments.findByRosterId(assignment.getRosterId()).stream()
                .filter(other -> !other.getId().equals(assignment.getId()))
                .filter(other -> target.getId().equals(other.getEmployeeId()))
                .filter(other -> {
                    Shift otherShift = shifts.findById(other.getShiftId()).orElse(null);
                    return otherShift != null
                            && otherShift.getShiftDate().isEqual(shift.getShiftDate());
                })
                .findFirst().orElse(null);

        Employee original = employees.findById(assignment.getEmployeeId()).orElse(null);
        if (counterpart != null && original != null) {
            Shift counterpartShift = shifts.findById(counterpart.getShiftId()).orElse(null);
            if (counterpartShift != null
                    && !original.skillSet().contains(counterpartShift.getRequiredSkill())) {
                throw new Problems.Conflict("exchange impossible: " + original.getEmpNo()
                        + " lacks skill " + counterpartShift.getRequiredSkill());
            }
            ShiftAssignment updatedCounterpart = new ShiftAssignment(counterpart.getId(),
                    counterpart.getRosterId(), counterpart.getShiftId(), original.getId(),
                    counterpart.getStatus(), counterpart.getAssignedAt());
            assignments.save(updatedCounterpart);
        }
        ShiftAssignment updated = new ShiftAssignment(assignment.getId(),
                assignment.getRosterId(), assignment.getShiftId(), target.getId(),
                ShiftAssignment.STATUS_CONFIRMED, Instant.now(clock));
        assignments.save(updated);
    }

    private void validateTarget(ShiftAssignment assignment, Employee target) {
        Shift shift = shifts.findById(assignment.getShiftId())
                .orElseThrow(() -> new Problems.NotFound("shift " + assignment.getShiftId()));
        if (!target.skillSet().contains(shift.getRequiredSkill())) {
            throw new Problems.Conflict(target.getEmpNo() + " lacks skill "
                    + shift.getRequiredSkill());
        }
        boolean unavailable = availabilities.findByEmployeeId(target.getId()).stream()
                .anyMatch(entry -> entry.getAvailDate().isEqual(shift.getShiftDate()));
        if (unavailable) {
            throw new Problems.Conflict(target.getEmpNo() + " is unavailable on "
                    + shift.getShiftDate());
        }
        // Same-day assignment on the target is handled as a two-sided exchange at approval.
    }

    @Transactional(readOnly = true)
    public List<SwapRequest> byRequester(String employeeId) {
        return swaps.findByRequestedByOrderByCreatedAtDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<SwapRequest> pending() {
        return swaps.findByStatusOrderByCreatedAtAsc(SwapRequest.STATUS_PENDING);
    }

    @Transactional(readOnly = true)
    public SwapRequest load(String swapId) {
        return swaps.findById(swapId)
                .orElseThrow(() -> new Problems.NotFound("swap request " + swapId));
    }

    @Transactional(readOnly = true)
    public Api.SwapRequestView view(SwapRequest swap) {
        ShiftAssignment assignment = assignments.findById(swap.getAssignmentId()).orElse(null);
        Shift shift = assignment == null ? null : shifts.findById(assignment.getShiftId())
                .orElse(null);
        Employee target = employees.findById(swap.getTargetEmployeeId()).orElse(null);
        Employee requester = employees.findById(swap.getRequestedBy()).orElse(null);
        return new Api.SwapRequestView(swap.getId(), swap.getSwapNo(), swap.getAssignmentId(),
                shift == null ? "?" : shift.getShiftDate().toString(),
                shift == null ? "?" : shift.getShiftType(),
                requester == null ? "?" : requester.getEmpNo(), swap.getTargetEmployeeId(),
                target == null ? "?" : target.getEmpNo(), swap.getReason(), swap.getStatus(),
                swap.getReviewedBy(), swap.getReviewedAt(), swap.getCreatedAt());
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private synchronized String nextSwapNo() {
        return "SWAP-2026-" + String.format("%05d", swaps.count() + 1);
    }
}
