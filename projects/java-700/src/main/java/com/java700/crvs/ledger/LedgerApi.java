package com.java700.crvs.ledger;

import java.time.Instant;

public final class LedgerApi {

    private LedgerApi() {
    }

    public record EventView(Long globalSeq, String personId, String eventType, String payload,
                            String actor, Instant occurredAt, String globalHash, Long chainSeq,
                            String chainHash) {

        static EventView from(LifeEvent e) {
            return new EventView(e.getGlobalSeq(), e.getPersonId(), e.getEventType(), e.getPayload(),
                    e.getActor(), e.getOccurredAt(), e.getGlobalHash(), e.getChainSeq(),
                    e.getChainHash());
        }
    }

    public record VerificationView(boolean valid, int entriesChecked, Long brokenSeq,
                                   String expectedHash, String actualHash) {
    }
}
