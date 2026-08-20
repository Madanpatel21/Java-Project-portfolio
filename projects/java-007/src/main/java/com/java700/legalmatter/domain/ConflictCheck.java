package com.java700.legalmatter.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** A recorded conflict screening result. */
@Entity
@Table(name = "conflict_checks")
public class ConflictCheck {

    public enum Result {
        CLEAR, POTENTIAL, CONFLICT
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "requested_by", nullable = false, length = 120)
    private String requestedBy;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    @Column(name = "subject_name", nullable = false, length = 200)
    private String subjectName;

    @Column(name = "adverse_names", nullable = false, length = 2000)
    private String adverseNames;

    @Column(name = "result", nullable = false, length = 16)
    private String result;

    @Column(name = "details_json", nullable = false, length = 4000)
    private String detailsJson;

    protected ConflictCheck() {
    }

    public ConflictCheck(String id, String requestedBy, Instant checkedAt, String subjectName,
                         String adverseNames, Result result, String detailsJson) {
        this.id = id;
        this.requestedBy = requestedBy;
        this.checkedAt = checkedAt;
        this.subjectName = subjectName;
        this.adverseNames = adverseNames;
        this.result = result.name();
        this.detailsJson = detailsJson;
    }

    public String getId() {
        return id;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public Instant getCheckedAt() {
        return checkedAt;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getAdverseNames() {
        return adverseNames;
    }

    public Result getResult() {
        return Result.valueOf(result);
    }

    public String getDetailsJson() {
        return detailsJson;
    }
}
