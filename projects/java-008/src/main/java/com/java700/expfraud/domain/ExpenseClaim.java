package com.java700.expfraud.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** A submitted expense claim and its computed fraud risk score. */
@Entity
@Table(name = "expense_claims")
public class ExpenseClaim {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_SCORED = "SCORED";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_UNDER_REVIEW = "UNDER_REVIEW";
    public static final String STATUS_CONFIRMED_FRAUD = "CONFIRMED_FRAUD";

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "claim_no", length = 32, nullable = false, unique = true)
    private String claimNo;

    @Column(name = "employee_id", length = 36, nullable = false)
    private String employeeId;

    @Column(name = "employee_name", length = 120, nullable = false)
    private String employeeName;

    @Column(length = 64, nullable = false)
    private String department;

    @Column(length = 32, nullable = false)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 8, nullable = false)
    private String currency;

    @Column(length = 160)
    private String merchant;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(length = 500)
    private String description;

    @Column(name = "receipt_ref", length = 64)
    private String receiptRef;

    @Column(length = 24, nullable = false)
    private String status;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Column(name = "score_version", nullable = false)
    private int scoreVersion;

    @Column(name = "reasons_json", length = 4000)
    private String reasonsJson;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ExpenseClaim() {
    }

    public ExpenseClaim(String id, String claimNo, String employeeId, String employeeName,
                        String department, String category, BigDecimal amount, String currency,
                        String merchant, LocalDate expenseDate, String description, String receiptRef,
                        String status, int riskScore, int scoreVersion, Instant submittedAt, Instant createdAt) {
        this.id = id;
        this.claimNo = claimNo;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.category = category;
        this.amount = amount;
        this.currency = currency;
        this.merchant = merchant;
        this.expenseDate = expenseDate;
        this.description = description;
        this.receiptRef = receiptRef;
        this.status = status;
        this.riskScore = riskScore;
        this.scoreVersion = scoreVersion;
        this.submittedAt = submittedAt;
        this.createdAt = createdAt;
    }

    public void applyScore(int newScore, String newReasonsJson) {
        this.riskScore = newScore;
        this.scoreVersion += 1;
        this.reasonsJson = newReasonsJson;
    }

    public void transition(String newStatus) {
        this.status = newStatus;
    }

    public String getId() {
        return id;
    }

    public String getClaimNo() {
        return claimNo;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getDepartment() {
        return department;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getMerchant() {
        return merchant;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public String getReceiptRef() {
        return receiptRef;
    }

    public String getStatus() {
        return status;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public int getScoreVersion() {
        return scoreVersion;
    }

    public String getReasonsJson() {
        return reasonsJson;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
