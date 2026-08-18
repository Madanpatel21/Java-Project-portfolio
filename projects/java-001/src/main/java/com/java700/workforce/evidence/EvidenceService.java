package com.java700.workforce.evidence;

import com.java700.workforce.common.api.Problems;
import com.java700.workforce.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appends to the hash-chained evidence ledger. Every compliance-relevant domain decision
 * (access requested/approved/rejected, grant created/revoked/expired, violation detected/
 * remediated, recertification decision, export completed, policy activation) is evidenced
 * here in the same transaction as the state change.
 */
@Service
public class EvidenceService {

    private final EvidenceRepository repository;
    private final DatabaseLock lock;
    private final Metrics metrics;
    private final Clock clock;

    public EvidenceService(EvidenceRepository repository, DatabaseLock lock, Metrics metrics, Clock clock) {
        this.repository = repository;
        this.lock = lock;
        this.metrics = metrics;
        this.clock = clock;
    }

    public EvidenceEntry append(String aggregateType, String aggregateId, String eventType,
                                String actor, Map<String, Object> payload) {
        String canonical = HashChain.canonicalJson(payload);
        return lock.withLock(() -> {
            EvidenceEntry last = repository.findTopByOrderBySeqDesc().orElse(null);
            String prevHash = last == null ? HashChain.GENESIS_PREV_HASH : last.getHash();
            String hash = HashChain.hash(prevHash, canonical);
            EvidenceEntry entry = new EvidenceEntry(last == null ? 1L : last.getSeq() + 1,
                    aggregateType, aggregateId, eventType, actor, canonical, prevHash, hash,
                    Instant.now(clock));
            EvidenceEntry saved = repository.save(entry);
            metrics.incrementEvidenceAppends();
            return saved;
        });
    }

    /** Verifies the entire ledger; returns detailed failure info on tamper. */
    @Transactional(readOnly = true)
    public HashChain.Verification verifyChain() {
        List<EvidenceEntry> all = repository.findAll(org.springframework.data.domain.Sort.by("seq"));
        return HashChain.verify(all);
    }

    /** Verifies the most recent {@code n} links (cheap health-check variant). */
    @Transactional(readOnly = true)
    public HashChain.Verification verifyRecent(int n) {
        List<EvidenceEntry> all = repository.findAll(org.springframework.data.domain.Sort.by("seq"));
        List<EvidenceEntry> tail = all.subList(Math.max(0, all.size() - n), all.size());
        return HashChain.verify(tail);
    }

    @Transactional(readOnly = true)
    public List<EvidenceEntry> entries(String aggregateType, String aggregateId) {
        return repository.findByAggregateTypeAndAggregateIdOrderBySeqAsc(aggregateType, aggregateId);
    }

    @Transactional(readOnly = true)
    public EvidenceEntry bySeq(long seq) {
        return repository.findById(seq)
                .orElseThrow(() -> new Problems.NotFound("Evidence entry not found"));
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}
