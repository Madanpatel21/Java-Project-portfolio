package com.java700.crvs.dedup;

import java.math.BigDecimal;
import java.time.Instant;

public final class DedupApi {

    private DedupApi() {
    }

    public record CandidateView(String id, String personAId, String personBId, BigDecimal score,
                                String status, Instant createdAt, String decidedBy,
                                Instant decidedAt) {

        static CandidateView from(DedupCandidate c) {
            return new CandidateView(c.getId(), c.getPersonAId(), c.getPersonBId(), c.getScore(),
                    c.getStatus().name(), c.getCreatedAt(), c.getDecidedBy(), c.getDecidedAt());
        }
    }
}
