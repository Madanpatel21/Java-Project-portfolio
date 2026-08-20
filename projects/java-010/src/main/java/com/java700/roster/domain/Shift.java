package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Shift. */
@Entity
@Table(name = "shifts")
public class Shift {


    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "roster_id", length = 36, nullable = false)
    private String rosterId;
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    @Column(name = "shift_type", length = 16, nullable = false)
    private String shiftType;
    @Column(name = "start_hour", nullable = false)
    private int startHour;
    @Column(name = "duration_hours", nullable = false)
    private int durationHours;
    @Column(name = "required_skill", length = 40, nullable = false)
    private String requiredSkill;
    @Column(name = "required_headcount", nullable = false)
    private int requiredHeadcount;

    protected Shift() {
    }

    public Shift(String id, String rosterId, LocalDate shiftDate, String shiftType, int startHour, int durationHours, String requiredSkill, int requiredHeadcount) {
        this.id = id;
        this.rosterId = rosterId;
        this.shiftDate = shiftDate;
        this.shiftType = shiftType;
        this.startHour = startHour;
        this.durationHours = durationHours;
        this.requiredSkill = requiredSkill;
        this.requiredHeadcount = requiredHeadcount;

    }

    public String getId() {
        return id;
    }

    public String getRosterId() {
        return rosterId;
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
    public int getRequiredHeadcount() {
        return requiredHeadcount;
    }
}
