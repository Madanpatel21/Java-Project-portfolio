package com.java700.workforce.policy.rules;

import com.java700.workforce.access.Grant;
import com.java700.workforce.policy.RuleEngine;
import com.java700.workforce.policy.RuleEvaluator;
import com.java700.workforce.policy.RuleType;
import com.java700.workforce.policy.ViolationCandidate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** A grant requiring a certification whose holder's certification is missing or expired. */
@Component
public class CertExpiryEvaluator implements RuleEvaluator {

    @Override
    public RuleType type() {
        return RuleType.CERT_EXPIRED;
    }

    @Override
    public List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext ctx) {
        List<String> certRequiredRoles = new ArrayList<>();
        rule.params().path("certRequiredRoles").forEach(n -> certRequiredRoles.add(n.asText().toUpperCase()));
        List<ViolationCandidate> out = new ArrayList<>();
        for (Grant g : ctx.activeGrants()) {
            boolean needsCert = g.roles().stream().map(String::toUpperCase).anyMatch(certRequiredRoles::contains);
            if (!needsCert) {
                continue;
            }
            var user = ctx.usersById().get(g.getUserId());
            if (user == null || user.getCertExpiresAt() == null
                    || user.getCertExpiresAt().isBefore(ctx.now())) {
                out.add(new ViolationCandidate(g.getUserId(), rule.policyCode(), type(), rule.severity(),
                        g.getResourceName(),
                        "Grant requires certification but it is missing or expired for " + g.getResourceName()));
            }
        }
        return out;
    }
}
