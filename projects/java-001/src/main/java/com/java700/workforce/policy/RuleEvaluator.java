package com.java700.workforce.policy;

import com.java700.workforce.access.Grant;
import com.java700.workforce.identity.UserProfile;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Evaluates one rule type against the current workforce state snapshot. */
public interface RuleEvaluator {

    RuleType type();

    List<ViolationCandidate> evaluate(RuleEngine.RuleDefinition rule, EvaluationContext context);

    /**
     * Read-only snapshot assembled by the correlation job:
     * active grants, users by id, latest access-event per user, and the evaluation instant.
     */
    record EvaluationContext(
            Instant now,
            List<Grant> activeGrants,
            Map<String, UserProfile> usersById,
            Map<String, Instant> lastAccessEventByUser) {

        public EvaluationContext {
            activeGrants = List.copyOf(activeGrants);
            usersById = Map.copyOf(usersById);
            lastAccessEventByUser = Map.copyOf(lastAccessEventByUser);
        }
    }
}
