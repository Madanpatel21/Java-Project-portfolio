package com.java700.expfraud.service;

import com.java700.expfraud.common.api.Problems;
import com.java700.expfraud.common.audit.AuditLogService;
import com.java700.expfraud.domain.DuplicateGroup;
import com.java700.expfraud.domain.DuplicateGroupRepository;
import com.java700.expfraud.domain.ExpenseClaim;
import com.java700.expfraud.domain.ExpenseClaimRepository;
import com.java700.expfraud.domain.PeerBaseline;
import com.java700.expfraud.domain.PeerBaselineRepository;
import com.java700.expfraud.domain.PolicyRule;
import com.java700.expfraud.domain.PolicyRuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Administration: policy rule maintenance, baseline operations and platform statistics. */
@Service
public class AdminService {

    private final PolicyRuleRepository rules;
    private final PeerBaselineRepository baselines;
    private final ExpenseClaimRepository claims;
    private final DuplicateGroupRepository groups;
    private final BaselineService baselineService;
    private final AuditLogService audit;

    public AdminService(PolicyRuleRepository rules, PeerBaselineRepository baselines,
                        ExpenseClaimRepository claims, DuplicateGroupRepository groups,
                        BaselineService baselineService, AuditLogService audit) {
        this.rules = rules;
        this.baselines = baselines;
        this.claims = claims;
        this.groups = groups;
        this.baselineService = baselineService;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<Api.RuleView> rules() {
        return rules.findAll().stream().map(this::toRuleView).toList();
    }

    @Transactional
    public Api.RuleView setActive(String code, boolean active, String username) {
        PolicyRule rule = rules.findById(code)
                .orElseThrow(() -> new Problems.NotFound("policy rule " + code));
        rule.setActive(active);
        PolicyRule saved = rules.save(rule);
        audit.record("RULE_TOGGLED", "policy_rule", saved.getCode(),
                "active=" + active + " by=" + username);
        return toRuleView(saved);
    }

    @Transactional
    public List<Api.BaselineView> recomputeBaselines() {
        List<PeerBaseline> result = baselineService.recomputeAll();
        return result.stream().map(this::toBaselineView).toList();
    }

    @Transactional(readOnly = true)
    public List<Api.BaselineView> baselines() {
        return baselineService.all().stream().map(this::toBaselineView).toList();
    }

    @Transactional(readOnly = true)
    public Api.StatsView stats() {
        double avg = claims.findAll().stream()
                .mapToInt(ExpenseClaim::getRiskScore)
                .average()
                .orElse(0.0);
        long groupsOpen = groups.findAll().stream()
                .filter(group -> DuplicateGroup.STATUS_OPEN.equals(group.getStatus()))
                .count();
        return new Api.StatsView(
                claims.countByStatus(ExpenseClaim.STATUS_SUBMITTED),
                claims.countByStatus(ExpenseClaim.STATUS_APPROVED),
                claims.countByStatus(ExpenseClaim.STATUS_REJECTED),
                claims.countByStatus(ExpenseClaim.STATUS_UNDER_REVIEW),
                claims.countByStatus(ExpenseClaim.STATUS_CONFIRMED_FRAUD),
                0, groupsOpen, 0, Math.round(avg * 100.0) / 100.0,
                baselines.count());
    }

    private Api.RuleView toRuleView(PolicyRule rule) {
        return new Api.RuleView(rule.getCode(), rule.getCategory(), rule.getComparator(),
                rule.getThreshold(), rule.getPattern(), rule.getSeverity(), rule.getMessage(),
                rule.isActive());
    }

    private Api.BaselineView toBaselineView(PeerBaseline baseline) {
        return new Api.BaselineView(baseline.getDepartment(), baseline.getCategory(),
                baseline.getMeanAmount(), baseline.getMedianAmount(), baseline.getP90Amount(),
                baseline.getStdDev(), baseline.getSampleCount(), baseline.getUpdatedAt());
    }
}
