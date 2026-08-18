package com.java700.workforce.policy.rules;

import com.java700.workforce.access.Grant;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Privileged access held continuously beyond the maximum allowed duration. */
@Component
public class StandingPrivilegeEvaluator implements RuleEvaluator {

    @Override
    public RuleType type() {
        return RuleType.STANDING_PRIVILEGE;
    }

    @Override
    public List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext ctx) {
        long maxDays = rule.params().path("maxDays").asLong(90);
        List<String> privileged = new ArrayList<>();
        rule.params().path("privilegedRoles").forEach(n -> privileged.add(n.asText().toUpperCase()));
        List<ViolationCandidate> out = new ArrayList<>();
        for (Grant g : ctx.activeGrants()) {
            boolean isPrivileged = g.roles().stream().map(String::toUpperCase).anyMatch(privileged::contains);
            if (isPrivileged && g.getGrantedAt().plus(maxDays, java.time.temporal.ChronoUnit.DAYS)
                    .isBefore(ctx.now())) {
                out.add(new ViolationCandidate(g.getUserId(), rule.policyCode(), type(), rule.severity(),
                        g.getResourceName(),
                        "Standing privileged access exceeds " + maxDays + " days on " + g.getResourceName()));
            }
        }
        return out;
    }
}
