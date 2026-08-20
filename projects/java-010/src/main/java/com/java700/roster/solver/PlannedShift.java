package com.java700.roster.solver;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.java700.roster.domain.Employee;
import java.time.LocalDate;

/**
 * A single demand slot that the solver must staff. One PlannedShift exists per
 * required headcount unit per shift; the solver picks the employee.
 */
@PlanningEntity
public class PlannedShift {

    private String id;
    private String shiftId;
    private LocalDate shiftDate;
    private String shiftType;
    private int startHour;
    private int durationHours;
    private String requiredSkill;
    private Employee employee;

    public PlannedShift() {
    }

    public PlannedShift(String id, String shiftId, LocalDate shiftDate, String shiftType,
                        int startHour, int durationHours, String requiredSkill) {
        this.id = id;
        this.shiftId = shiftId;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.startHour = startHour;
        this.durationHours = durationHours;
        this.requiredSkill = requiredSkill;
    }

    @PlanningVariable(valueRangeProviderRefs = "employeeRange")
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee newEmployee) {
        this.employee = newEmployee;
    }

    @PlanningId
    public String getId() {
        return id;
    }

    public String getShiftId() {
        return shiftId;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public String getShiftType() {
        return shiftType;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    @Override
    public String toString() {
        return shiftId + "(" + shiftType + " " + shiftDate + ")"
                + (employee == null ? " unassigned" : " -> " + employee.getEmpNo());
    }
}
