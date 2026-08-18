package com.java700.workforce.policy;

import com.java700.workforce.policy.RuleEngine.Severity;

/** A rule hit before persistence; deduplicated by (userId, policyCode, ruleType, resource). */
public record ViolationCandidate(
        String userId,
        String policyCode,
        RuleType ruleType,
        Severity severity,
        String resource,
        String description) {
}
