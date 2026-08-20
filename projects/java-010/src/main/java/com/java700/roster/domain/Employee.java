package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Employee. */
@Entity
@Table(name = "employees")
public class Employee {


    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "emp_no", length = 20, nullable = false, unique = true)
    private String empNo;
    @Column(length = 120, nullable = false)
    private String name;
    @Column(length = 64, nullable = false)
    private String department;
    @Column(length = 500, nullable = false)
    private String skills;
    @Column(name = "employment_type", length = 16, nullable = false)
    private String employmentType;
    @Column(name = "max_weekly_hours", nullable = false)
    private int maxWeeklyHours;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Employee() {
    }

    public Employee(String id, String userId, String empNo, String name, String department,
                   String skills, String employmentType, int maxWeeklyHours, boolean active,
                   Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.empNo = empNo;
        this.name = name;
        this.department = department;
        this.skills = skills;
        this.employmentType = employmentType;
        this.maxWeeklyHours = maxWeeklyHours;
        this.active = active;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    /** Parsed skill tags (e.g. "NURSE,CARE" -> {NURSE, CARE}). */
    public Set<String> skillSet() {
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    public String getEmpNo() {
        return empNo;
    }
    public String getName() {
        return name;
    }
    public String getDepartment() {
        return department;
    }
    public String getSkills() {
        return skills;
    }
    public String getEmploymentType() {
        return employmentType;
    }
    public int getMaxWeeklyHours() {
        return maxWeeklyHours;
    }
    public boolean isActive() {
        return active;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
