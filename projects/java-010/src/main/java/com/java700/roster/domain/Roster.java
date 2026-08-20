package com.java700.roster.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/** Roster. */
@Entity
@Table(name = "rosters")
public class Roster {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    public void transition(String newStatus) {
        this.status = newStatus;
    }

    public void markPublished(Instant at) {
        this.status = STATUS_PUBLISHED;
        this.publishedAt = at;
    }

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 120, nullable = false)
    private String name;
    @Column(length = 64, nullable = false)
    private String department;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    @Column(length = 16, nullable = false)
    private String status;
    @Column(name = "score_json", length = 4000)
    private String scoreJson;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Roster() {
    }

    public Roster(String id, String name, String department, LocalDate startDate, LocalDate endDate, String status, String scoreJson, Instant publishedAt, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.scoreJson = scoreJson;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getDepartment() {
        return department;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public String getStatus() {
        return status;
    }
    public String getScoreJson() {
        return scoreJson;
    }
    public Instant getPublishedAt() {
        return publishedAt;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
}
