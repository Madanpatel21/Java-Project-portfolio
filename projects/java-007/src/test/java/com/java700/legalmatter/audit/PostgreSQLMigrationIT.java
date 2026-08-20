package com.java700.legalmatter.audit;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** Verifies all Flyway migrations on real PostgreSQL 16 (auto-skipped without Docker; runs in CI). */
@Testcontainers(disabledWithoutDocker = true)
class PostgreSQLMigrationIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationsApplyAndCommonSchemaIsQueryable() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();
        flyway.migrate();
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("select count(*) from audit_log")) {
                rs.next();
                assertThat(rs.getLong(1)).isZero();
            }
            try (ResultSet rs = st.executeQuery("select count(*) from idempotency_record")) {
                rs.next();
                assertThat(rs.getLong(1)).isZero();
            }
        }
    }
}
