package com.java700.stewardship.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Resets mutable test data between tests (single shared H2 context across IT classes). */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "interventions", "review_tasks", "susceptibility_results", "isolates", "cultures",
            "restricted_authorizations", "prescriptions", "lab_values", "admissions", "patients",
            "idempotency_record", "audit_log", "local_user_roles", "local_users");

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
    }
}
