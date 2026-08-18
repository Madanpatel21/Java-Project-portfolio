package com.java700.workforce.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Resets mutable test data between tests (single shared H2 context across IT classes). */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "recert_decision", "approval", "access_request", "violation", "export_job",
            "idempotency_record", "access_event", "access_grant", "evidence_entry",
            "local_user_roles", "local_users", "user_roles", "users");

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
        jdbc.update("alter table evidence_entry alter column seq restart with 1");
    }
}
