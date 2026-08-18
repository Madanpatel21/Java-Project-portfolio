package com.java700.workforce.evidence;

import java.util.concurrent.locks.ReentrantLock;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serializes evidence-chain appends. On PostgreSQL a transaction-scoped advisory lock
 * (pg_advisory_xact_lock) keeps appends safe across concurrent instances; on H2 (dev/test)
 * a JVM lock provides the same single-writer guarantee.
 */
@Component
public class DatabaseLock {

    private static final Logger log = LoggerFactory.getLogger(DatabaseLock.class);
    private static final long EVIDENCE_LOCK_ID = 0x4A415641L;

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
            if (advisory) {
                unlockAdvisory();
            } else {
                inMemory.unlock();
            }
        }
    }

    private boolean tryAdvisoryLock() {
        try {
            jdbc.execute("SELECT pg_advisory_xact_lock(" + EVIDENCE_LOCK_ID + ")");
            return true;
        } catch (RuntimeException e) {
            log.debug("PostgreSQL advisory lock unavailable, using JVM lock: {}", e.getMessage());
            return false;
        }
    }

    private void unlockAdvisory() {
        // transaction-scoped: released automatically at commit/rollback
    }

    @FunctionalInterface
    public interface LockedAction<T> {
        T run();
    }
}
