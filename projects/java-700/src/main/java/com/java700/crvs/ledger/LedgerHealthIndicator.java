package com.java700.crvs.ledger;

import java.util.Map;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Liveness-grade integrity: the last 100 global-chain links must chain cleanly. */
@Component
public class LedgerHealthIndicator implements HealthIndicator {

    private static final int CHECK_WINDOW = 100;

    private final LedgerService service;

    public LedgerHealthIndicator(LedgerService service) {
        this.service = service;
    }

    @Override
    public Health health() {
        try {
            HashChain.Verification v = service.verifyGlobalRecent(CHECK_WINDOW);
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
