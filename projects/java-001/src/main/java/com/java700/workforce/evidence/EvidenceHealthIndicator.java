package com.java700.workforce.evidence;

import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Liveness-grade integrity check: the last 100 ledger links must chain cleanly. */
@Component
public class EvidenceHealthIndicator implements HealthIndicator {

    private static final int CHECK_WINDOW = 100;

    private final EvidenceService service;

    public EvidenceHealthIndicator(EvidenceService service) {
        this.service = service;
    }

    @Override
    public Health health() {
        try {
            HashChain.Verification v = service.verifyRecent(CHECK_WINDOW);
            Map<String, Object> details = Map.of(
                    "entriesChecked", v.entriesChecked(),
                    "totalEntries", service.count());
            if (v.valid()) {
                return Health.up().withDetails(details).build();
            }
            return Health.down()
                    .withDetail("brokenSeq", v.brokenSeq())
                    .withDetail("expectedHash", v.expectedHash())
                    .withDetail("actualHash", v.actualHash())
                    .build();
        } catch (RuntimeException e) {
            return Health.down(e).build();
        }
    }
}
