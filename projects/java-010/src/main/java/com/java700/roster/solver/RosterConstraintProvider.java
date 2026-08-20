package com.java700.roster.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.java700.roster.domain.Availability;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Labor-law (hard) and fairness (soft) constraints. The rule set mirrors the
 * data-driven roster_rules table (V3) so scores are explainable to auditors.
 */
public class RosterConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                skillMatch(constraintFactory),
                availability(constraintFactory),
                oneShiftPerEmployeePerDay(constraintFactory),
                weeklyHoursCap(constraintFactory),
                minRestAfterNight(constraintFactory),
                maxWorkingDaysPerWeek(constraintFactory),
                nightShiftCap(constraintFactory),
                coverage(constraintFactory),
                fairness(constraintFactory)
        };
    }

    /** HARD: employee must hold the shift's required skill. */
    Constraint skillMatch(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null
                        && !shift.getEmployee().skillSet().contains(shift.getRequiredSkill()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Skill match");
    }

    /** HARD: employee must be available on the shift day (no leave/absence). */
    Constraint availability(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null)
                .join(Availability.class,
                        Joiners.equal(shift -> shift.getEmployee().getId(),
                                Availability::getEmployeeId),
                        Joiners.equal(PlannedShift::getShiftDate, Availability::getAvailDate))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Availability");
    }

    /** HARD: at most one shift per employee per day. */
    Constraint oneShiftPerEmployeePerDay(ConstraintFactory factory) {
        return factory.forEachUniquePair(PlannedShift.class,
                        Joiners.equal(shift -> shift.getEmployee() == null
                                ? "" : shift.getEmployee().getId()),
                        Joiners.equal(PlannedShift::getShiftDate))
                .filter((first, second) -> first.getEmployee() != null)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("One shift per employee per day");
    }

    /** HARD: weekly hours cap (per employee contract, default 40h). */
    Constraint weeklyHoursCap(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null)
                .groupBy(shift -> weekKey(shift.getShiftDate()), PlannedShift::getEmployee,
                        ConstraintCollectors.sum(PlannedShift::getDurationHours))
                .filter((week, employee, hours) -> hours > employee.getMaxWeeklyHours())
                .penalize(HardSoftScore.ONE_HARD,
                        (week, employee, hours) -> hours - employee.getMaxWeeklyHours())
                .asConstraint("Weekly hours cap");
    }

    /** HARD: at least 11h rest between a NIGHT and the following MORNING. */
    Constraint minRestAfterNight(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null
                        && "NIGHT".equals(shift.getShiftType()))
                .join(PlannedShift.class,
                        Joiners.equal(PlannedShift::getEmployee),
                        Joiners.equal(night -> night.getShiftDate().plusDays(1),
                                PlannedShift::getShiftDate),
                        Joiners.filtering((night, next) -> "MORNING".equals(next.getShiftType())))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Min rest after night");
    }

    /** HARD: at most 6 distinct working days per rolling week. */
    Constraint maxWorkingDaysPerWeek(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null)
                .groupBy(PlannedShift::getEmployee,
                        shift -> weekKey(shift.getShiftDate()),
                        ConstraintCollectors.countDistinct(PlannedShift::getShiftDate))
                .filter((employee, week, days) -> days > 6)
                .penalize(HardSoftScore.ONE_HARD,
                        (employee, week, days) -> days - 6)
                .asConstraint("Max working days per week");
    }

    /** HARD: at most 4 NIGHT shifts per employee per week. */
    Constraint nightShiftCap(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null
                        && "NIGHT".equals(shift.getShiftType()))
                .groupBy(shift -> weekKey(shift.getShiftDate()), PlannedShift::getEmployee,
                        ConstraintCollectors.count())
                .filter((week, employee, count) -> count > 4)
                .penalize(HardSoftScore.ONE_HARD, (week, employee, count) -> count - 4)
                .asConstraint("Night shift cap");
    }

    /** SOFT: every demand slot should be staffed (10 points each). */
    Constraint coverage(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() == null)
                .penalize(HardSoftScore.ONE_SOFT, shift -> 10)
                .asConstraint("Coverage");
    }

    /** SOFT: balance total assigned hours across employees (minimize sum of squares). */
    Constraint fairness(ConstraintFactory factory) {
        return factory.forEach(PlannedShift.class)
                .filter(shift -> shift.getEmployee() != null)
                .groupBy(PlannedShift::getEmployee,
                        ConstraintCollectors.sum(PlannedShift::getDurationHours))
                .penalize(HardSoftScore.ONE_SOFT,
                        (employee, hours) -> hours * hours)
                .asConstraint("Fairness (balanced hours)");
    }

    static LocalDate weekKey(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
