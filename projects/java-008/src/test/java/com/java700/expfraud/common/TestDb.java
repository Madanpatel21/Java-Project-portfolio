package com.java700.expfraud.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Resets mutable test data between tests (single shared H2 context across IT classes).
 * EXTEND this list per project with the project's own tables.
 */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "audit_log", "idempotency_record", "local_user_roles", "local_users",
            "rule_violations", "duplicate_groups", "fraud_cases", "tips",
            "peer_baselines", "expense_claims");
    // NOTE: policy_rules is intentionally NOT cleaned — it is seeded by Flyway V3 and shared.

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
    }
}
