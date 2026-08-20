package com.java700.govault.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Resets mutable test data between tests (single shared H2 context across IT classes).
 * EXTEND this list per project with the project's own tables.
 */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "disposition_proofs", "hold_entries", "legal_holds", "documents",
            "audit_log", "idempotency_record", "local_user_roles", "local_users");

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
    }
}
