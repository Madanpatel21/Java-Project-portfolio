package com.java700.stewardship.audit;

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

/** Verifies Flyway migrations on real PostgreSQL 16 (auto-skipped without Docker; runs in CI). */
@Testcontainers(disabledWithoutDocker = true)
class PostgreSQLMigrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationsApplyAndSeedIsQueryable() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();
        flyway.migrate();
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("select count(*) from antimicrobial_drugs")) {
                rs.next();
                assertThat(rs.getLong(1)).isEqualTo(13);
            }
            try (ResultSet rs = st.executeQuery(
                    "select count(*) from stewardship_guidelines where status = 'ACTIVE'")) {
                rs.next();
                assertThat(rs.getLong(1)).isEqualTo(1);
            }
        }
    }
}
