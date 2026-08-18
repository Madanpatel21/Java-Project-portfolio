package com.java700.crvs.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/** Resets mutable test data between tests (single shared H2 context across IT classes). */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "certificates", "dedup_candidates", "registrations", "life_events", "persons",
            "idempotency_record", "audit_log", "local_user_roles", "local_users", "offices");

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
        jdbc.update("alter table life_events alter column global_seq restart with 1");
    }
}
