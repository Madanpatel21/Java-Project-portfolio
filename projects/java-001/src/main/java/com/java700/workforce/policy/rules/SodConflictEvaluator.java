package com.java700.workforce.policy.rules;

import com.java700.workforce.access.Grant;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Segregation-of-duties: a user holding two conflicting roles on the same resource
 * (e.g. APPROVER + REQUESTER, ADMIN + AUDITOR) is a high-severity violation.
 */
@Component
public class SodConflictEvaluator implements RuleEvaluator {

    @Override
    public RuleType type() {
        return RuleType.SOD_CONFLICT;
    }

    @Override
    public List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext ctx) {
        List<String[]> pairs = new ArrayList<>();
        for (var p : rule.params().path("conflictPairs")) {
            pairs.add(new String[]{p.get(0).asText(), p.get(1).asText()});
        }
        Map<String, List<Grant>> byUserResource = new HashMap<>();
        for (Grant g : ctx.activeGrants()) {
            byUserResource.computeIfAbsent(g.getUserId() + "|" + g.getResourceName(), k -> new ArrayList<>()).add(g);
        }
        List<ViolationCandidate> out = new ArrayList<>();
        byUserResource.forEach((key, grants) -> {
            List<String> roles = grants.stream().flatMap(g -> g.roles().stream()).map(String::toUpperCase).toList();
            for (String[] pair : pairs) {
                if (roles.contains(pair[0]) && roles.contains(pair[1])) {
                    Grant g = grants.get(0);
                    out.add(new ViolationCandidate(g.getUserId(), rule.policyCode(), type(), rule.severity(),
                            g.getResourceName(),
                            "Segregation of duties conflict: roles " + pair[0] + " and " + pair[1]
                                    + " held together on " + g.getResourceName()));
                    break;
                }
            }
        });
        return out;
    }
}
