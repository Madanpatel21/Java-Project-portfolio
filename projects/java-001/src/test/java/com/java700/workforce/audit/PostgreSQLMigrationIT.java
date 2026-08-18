package com.java700.workforce.audit;

import static org.assertj.core.api.Assertions.assertThat;import com.java700.workforce.WorkforceComplianceApplication;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies the Flyway migrations against a real PostgreSQL 16 instance.
 * Skipped automatically when Docker is unavailable (e.g. local dev without Docker);
 * CI runs it inside GitHub Actions.
 */
@Testcontainers(disabledWithoutDocker = true)
class PostgreSQLMigrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationsApplyAndSchemaIsQueryable() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();
        flyway.migrate();
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("select count(*) from evidence_entry")) {
                rs.next();
                assertThat(rs.getLong(1)).isZero();
            }
            try (ResultSet rs = st.executeQuery("select count(*) from policy_version")) {
                rs.next();
                assertThat(rs.getLong(1)).isEqualTo(1);
            }
        }
    }
}
