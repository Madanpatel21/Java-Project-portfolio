package com.java700.crvs.ledger;

import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serializes ledger appends. PostgreSQL transaction-scoped advisory lock across instances;
 * JVM lock fallback on H2 (dev/test).
 */
@Component
public class DatabaseLock {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLock.class);
    private static final long LEDGER_LOCK_ID = 0x43525653L;

    private final JdbcTemplate jdbc;
    private final ReentrantLock inMemory = new ReentrantLock(true);

    public DatabaseLock(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Transactional
    public <T> T withLock(LockedAction<T> action) {
        boolean advisory = tryAdvisoryLock();
        if (!advisory) {
            inMemory.lock();
        }
        try {
            return action.run();
        } finally {
            if (!advisory) {
                inMemory.unlock();
            }
        }
    }

    private boolean tryAdvisoryLock() {
        try {
            jdbc.execute("SELECT pg_advisory_xact_lock(" + LEDGER_LOCK_ID + ")");
            return true;
        } catch (RuntimeException e) {
            log.debug("PostgreSQL advisory lock unavailable, using JVM lock: {}", e.getMessage());
            return false;
        }
    }

    @FunctionalInterface
    public interface LockedAction<T> {
        T run();
    }
}
