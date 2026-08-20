package com.java700.legalmatter.common.audit;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Records registry audit entries in the same transaction as the business write. */
@Service
public class AuditLogService {

    public static final String CORRELATION_ID = "correlationId";

    private final AuditLogRepository repository;
    private final Clock clock;

    public AuditLogService(AuditLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void record(String action, String targetType, String targetId, String detail) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : "system";
        repository.save(new AuditLog(UUID.randomUUID().toString(), Instant.now(clock), principal,
                action, targetType, targetId, truncate(detail, 2000), MDC.get(CORRELATION_ID)));
    }

    private static String truncate(String s, int max) {
        return s == null ? null : (s.length() <= max ? s : s.substring(0, max));
    }
}
