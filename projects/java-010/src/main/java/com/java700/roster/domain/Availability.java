package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** An employee's unavailability entry (leave, training, personal day). */
@Entity
@Table(name = "availabilities")
public class Availability {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    @Column(name = "avail_date", nullable = false)
    private LocalDate availDate;

    @Column(length = 120)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Availability() {
    }

    public Availability(String id, String employeeId, LocalDate availDate, String reason,
                        Instant createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.availDate = availDate;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public LocalDate getAvailDate() {
        return availDate;
    }

    public String getReason() {
        return reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
