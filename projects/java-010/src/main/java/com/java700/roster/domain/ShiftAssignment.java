package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** ShiftAssignment. */
@Entity
@Table(name = "shift_assignments")
public class ShiftAssignment {

    public static final String STATUS_UNASSIGNED = "UNASSIGNED";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";

    public void assign(String newEmployeeId, Instant at) {
        this.employeeId = newEmployeeId;
        this.status = STATUS_ASSIGNED;
        this.assignedAt = at;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "roster_id", length = 36, nullable = false)
    private String rosterId;
    @Column(name = "shift_id", length = 36, nullable = false)
    private String shiftId;
    @Column(name = "employee_id", length = 36)
    private String employeeId;
    @Column(length = 20, nullable = false)
    private String status;
    @Column(name = "assigned_at")
    private Instant assignedAt;

    protected ShiftAssignment() {
    }

    public ShiftAssignment(String id, String rosterId, String shiftId, String employeeId, String status, Instant assignedAt) {
        this.id = id;
        this.rosterId = rosterId;
        this.shiftId = shiftId;
        this.employeeId = employeeId;
        this.status = status;
        this.assignedAt = assignedAt;

    }

    public String getId() {
        return id;
    }

    public String getRosterId() {
        return rosterId;
    }
    public String getShiftId() {
        return shiftId;
    }
    public String getEmployeeId() {
        return employeeId;
    }
    public String getStatus() {
        return status;
    }
    public Instant getAssignedAt() {
        return assignedAt;
    }
}
