package com.java700.workforce.policy.rules;

import com.java700.workforce.access.Grant;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Access not re-certified within the policy window. */
@Component
public class RecertOverdueEvaluator implements RuleEvaluator {

    @Override
    public RuleType type() {
        return RuleType.RECERT_OVERDUE;
    }

    @Override
    public List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext ctx) {
        List<ViolationCandidate> out = new ArrayList<>();
        for (Grant g : ctx.activeGrants()) {
            if (g.getRecertDueAt() != null && g.getRecertDueAt().isBefore(ctx.now())) {
                out.add(new ViolationCandidate(g.getUserId(), rule.policyCode(), type(), rule.severity(),
                        g.getResourceName(),
                        "Recertification overdue (due " + g.getRecertDueAt() + ") for " + g.getResourceName()));
            }
        }
        return out;
    }
}
