package com.java700.roster.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** API request/response records (immutable, defensive copies on collections). */
public final class Api {

    private Api() {
    }

    public record EmployeeRequest(String empNo, String name, String department, String skills,
                                  String employmentType, int maxWeeklyHours) {
    }

    public record EmployeeView(String id, String empNo, String name, String department,
                               List<String> skills, String employmentType, int maxWeeklyHours,
                               boolean active) {
        public EmployeeView {
            skills = List.copyOf(skills);
        }

        @Override
        public List<String> skills() {
            return List.copyOf(skills);
        }
    }

    public record DemandSlot(String shiftType, int startHour, int durationHours,
                             String requiredSkill, int headcount) {
    }

    public record RosterRequest(String name, String department, LocalDate startDate, int days,
                                List<DemandSlot> demand) {
        public RosterRequest {
            demand = List.copyOf(demand);
        }

        @Override
        public List<DemandSlot> demand() {
            return List.copyOf(demand);
        }
    }

    public record RosterView(String id, String name, String department, LocalDate startDate,
                             LocalDate endDate, String status, int shiftCount, int assignedCount,
                             String score, Instant publishedAt) {
    }

    public record ShiftView(String id, String assignmentId, String shiftDate, String shiftType,
                            int startHour, int durationHours, String requiredSkill,
                            String employeeEmpNo, String employeeName) {
    }

    public record AssignmentView(String id, String shiftId, String shiftDate, String shiftType,
                                 String requiredSkill, String employeeId, String employeeName,
                                 String status) {
    }

    public record OptimizeResult(String rosterId, boolean feasible, String score, int totalShifts,
                                 int assigned, String breakdownJson) {
    }

    public record ExplainResult(String score, boolean feasible, List<ConstraintMatchView> matches) {
        public ExplainResult {
            matches = List.copyOf(matches);
        }

        @Override
        public List<ConstraintMatchView> matches() {
            return List.copyOf(matches);
        }
    }

    public record ConstraintMatchView(String constraint, long count, int hard, int soft) {
    }

    public record AvailabilityRequest(LocalDate day, String reason) {
    }

    public record SwapRequestView(String id, String swapNo, String assignmentId, String shiftDate,
                                  String shiftType, String requestedBy, String targetEmployeeId,
                                  String targetName, String reason, String status,
                                  String reviewedBy, Instant reviewedAt, Instant createdAt) {
    }

    public record SwapCreateRequest(String assignmentId, String targetEmployeeId, String reason) {
    }

    public record SwapDecisionRequest(String decision, String note) {
    }

    public record StatsView(int employees, int totalShifts, int assigned, int unassigned,
                            double coveragePct, double fairnessStdDevHours,
                            List<EmployeeHours> perEmployee, int pendingSwaps) {
        public StatsView {
            perEmployee = List.copyOf(perEmployee);
        }

        @Override
        public List<EmployeeHours> perEmployee() {
            return List.copyOf(perEmployee);
        }
    }

    public record EmployeeHours(String empNo, String name, int hours) {
    }

    public record RuleView(String code, String name, String level, int threshold, int weight,
                           boolean active) {
    }
}
