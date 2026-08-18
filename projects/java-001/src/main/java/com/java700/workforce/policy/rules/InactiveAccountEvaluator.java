package com.java700.workforce.policy.rules;

import com.java700.workforce.access.Grant;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Active grants on accounts with no access activity within the inactivity window. */
@Component
public class InactiveAccountEvaluator implements RuleEvaluator {

    @Override
    public RuleType type() {
        return RuleType.INACTIVE_ACCOUNT;
    }

    @Override
    public List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext ctx) {
        long inactiveDays = rule.params().path("inactiveDays").asLong(60);
        Instant threshold = ctx.now().minus(inactiveDays, java.time.temporal.ChronoUnit.DAYS);
        List<ViolationCandidate> out = new ArrayList<>();
        for (Grant g : ctx.activeGrants()) {
            Instant last = ctx.lastAccessEventByUser().get(g.getUserId());
            if (last == null || last.isBefore(threshold)) {
                out.add(new ViolationCandidate(g.getUserId(), rule.policyCode(), type(), rule.severity(),
                        g.getResourceName(),
                        "No access activity for over " + inactiveDays + " days on account with grant to "
                                + g.getResourceName()));
            }
        }
        return out;
    }
}
