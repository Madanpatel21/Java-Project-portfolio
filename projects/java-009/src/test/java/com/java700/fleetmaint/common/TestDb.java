package com.java700.fleetmaint.common;

import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Resets mutable test data between tests (single shared H2 context across IT classes).
 * EXTEND this list per project with the project's own tables.
 */
public final class TestDb {

    private static final List<String> TABLES = List.of(
            "audit_log", "idempotency_record", "local_user_roles", "local_users",
            "odometer_entries", "inspections", "part_reservations", "parts",
            "work_orders", "maintenance_tasks", "vehicles");
    // NOTE: maintenance_plans + plan_items are seeded by Flyway V3 (reference plans with
    // parts kits) and intentionally excluded from cleanup.

    private TestDb() {
    }

    public static void clean(JdbcTemplate jdbc) {
        for (String table : TABLES) {
            jdbc.update("delete from " + table);
        }
    }
}
