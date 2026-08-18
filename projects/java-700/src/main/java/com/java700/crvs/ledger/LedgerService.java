package com.java700.crvs.ledger;

import com.java700.crvs.observability.Metrics;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appends to the dual-chained ledger. Written in the same transaction as the domain
 * mutation it evidences; never updated or deleted by application code.
 */
@Service
public class LedgerService {

    private final LifeEventRepository repository;
    private final DatabaseLock lock;
    private final Metrics metrics;
    private final Clock clock;

    public LedgerService(LifeEventRepository repository, DatabaseLock lock, Metrics metrics, Clock clock) {
        this.repository = repository;
        this.lock = lock;
        this.metrics = metrics;
        this.clock = clock;
    }

    public LifeEvent append(String personId, String eventType, String actor, Map<String, Object> payload) {
        String canonical = HashChain.canonicalJson(payload);
        return lock.withLock(() -> {
            LifeEvent lastGlobal = repository.findTopByOrderByGlobalSeqDesc().orElse(null);
            String prevGlobalHash = lastGlobal == null
                    ? HashChain.GENESIS_PREV_HASH : lastGlobal.getGlobalHash();
            String globalHash = HashChain.hash(prevGlobalHash, canonical);
            LifeEvent lastChain = repository.findTopByPersonIdOrderByChainSeqDesc(personId).orElse(null);
            String prevChainHash = lastChain == null
                    ? HashChain.GENESIS_PREV_HASH : lastChain.getChainHash();
            String chainHash = HashChain.hash(prevChainHash, canonical);
            LifeEvent event = new LifeEvent(lastGlobal == null ? 1L : lastGlobal.getGlobalSeq() + 1,
                    personId, eventType, canonical, actor, Instant.now(clock),
                    prevGlobalHash, globalHash,
                    lastChain == null ? 1L : lastChain.getChainSeq() + 1,
                    prevChainHash, chainHash);
            LifeEvent saved = repository.save(event);
            metrics.incrementLedgerAppends();
            return saved;
        });
    }

    /** Verifies the ENTIRE global chain. */
    @Transactional(readOnly = true)
    public HashChain.Verification verifyGlobal() {
        return HashChain.verify(repository.findAll(Sort.by("globalSeq")));
    }

    /** Verifies the last {@code n} global links (cheap health check). */
    @Transactional(readOnly = true)
    public HashChain.Verification verifyGlobalRecent(int n) {
        List<LifeEvent> all = repository.findAll(Sort.by("globalSeq"));
        List<LifeEvent> tail = all.subList(Math.max(0, all.size() - n), all.size());
        return HashChain.verify(tail);
    }

    /** Verifies one person's full life chain. */
    @Transactional(readOnly = true)
    public HashChain.Verification verifyPersonChain(String personId) {
        return HashChain.verifyPerson(repository.findByPersonIdOrderByChainSeqAsc(personId));
    }

    @Transactional(readOnly = true)
    public List<LifeEvent> personHistory(String personId) {
        return repository.findByPersonIdOrderByChainSeqAsc(personId);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}
