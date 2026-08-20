package com.java700.p2p.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Matching tolerance rule (PRICE_VARIANCE | QUANTITY_VARIANCE | AMOUNT_VARIANCE). */
@Entity
@Table(name = "tolerance_rules")
public class ToleranceRule {

    public enum RuleType {
        PRICE_VARIANCE, QUANTITY_VARIANCE, AMOUNT_VARIANCE
    }

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "rule_type", nullable = false, length = 32)
    private String ruleType;

    @Column(name = "tolerance_pct", nullable = false, precision = 8, scale = 3)
    private BigDecimal tolerancePct;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ToleranceRule() {
    }

    public ToleranceRule(String id, RuleType ruleType, BigDecimal tolerancePct, String action) {
        this.id = id;
        this.ruleType = ruleType.name();
        this.tolerancePct = tolerancePct;
        this.action = action;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public RuleType getRuleType() {
        return RuleType.valueOf(ruleType);
    }

    public BigDecimal getTolerancePct() {
        return tolerancePct;
    }

    public String getAction() {
        return action;
    }

    public boolean isActive() {
        return active;
    }

    public void update(BigDecimal newTolerancePct, String newAction) {
        this.tolerancePct = newTolerancePct;
        this.action = newAction;
    }
}
