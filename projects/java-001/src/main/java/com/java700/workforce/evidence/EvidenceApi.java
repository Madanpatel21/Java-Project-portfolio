package com.java700.workforce.evidence;

import java.time.Instant;

public final class EvidenceApi {

    private EvidenceApi() {
    }

    public record EntryView(long seq, String aggregateType, String aggregateId, String eventType,
                            String actor, String payload, String prevHash, String hash,
                            Instant occurredAt) {

        static EntryView from(EvidenceEntry e) {
            return new EntryView(e.getSeq(), e.getAggregateType(), e.getAggregateId(), e.getEventType(),
                    e.getActor(), e.getPayload(), e.getPrevHash(), e.getHash(), e.getOccurredAt());
        }
    }

    public record VerificationView(boolean valid, int entriesChecked, Long brokenSeq,
                                   String expectedHash, String actualHash) {
    }
}
