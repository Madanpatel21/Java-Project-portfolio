package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A data-driven policy rule evaluated by the scoring engine. */
@Entity
@Table(name = "policy_rules")
public class PolicyRule {

    public static final String SEVERITY_BLOCKER = "BLOCKER";
    public static final String SEVERITY_VIOLATION = "VIOLATION";
    public static final String SEVERITY_WARNING = "WARNING";

    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 40, nullable = false, unique = true)
    private String code;

    @Column(length = 32, nullable = false)
    private String category;

    @Column(length = 32, nullable = false)
    private String comparator;

    @Column(precision = 12, scale = 2)
    private BigDecimal threshold;

    @Column(length = 120)
    private String pattern;

    @Column(length = 16, nullable = false)
    private String severity;

    @Column(length = 500, nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    protected PolicyRule() {
    }

    public PolicyRule(String id, String code, String category, String comparator,
                      BigDecimal threshold, String pattern, String severity, String message,
                      boolean active, int sortOrder) {
        this.id = id;
        this.code = code;
        this.category = category;
        this.comparator = comparator;
        this.threshold = threshold;
        this.pattern = pattern;
        this.severity = severity;
        this.message = message;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    public void setActive(boolean newActive) {
        this.active = newActive;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getCategory() {
        return category;
    }

    public String getComparator() {
        return comparator;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public String getPattern() {
        return pattern;
    }

    public String getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}
