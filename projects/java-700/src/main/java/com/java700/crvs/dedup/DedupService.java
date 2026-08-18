package com.java700.crvs.dedup;


import com.java700.crvs.common.api.Problems;
import com.java700.crvs.common.audit.AuditLogService;
import com.java700.crvs.dedup.DedupCandidate.Status;
import com.java700.crvs.messaging.DomainEventHandler;
import com.java700.crvs.observability.Metrics;
import com.java700.crvs.registry.PersonRepository;
import com.java700.crvs.registration.RegistrationService.PersonRegistered;
import com.java700.crvs.security.SecurityUtil;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Candidate adjudication + event-driven scanning on new births. */
@Service
public class DedupService {

    private static final Logger log = LoggerFactory.getLogger(DedupService.class);

    private final DedupRepository repository;
    private final PersonRepository personRepository;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final DedupEngine engine;

    public DedupService(DedupRepository repository, PersonRepository personRepository,
                        AuditLogService audit, Metrics metrics, Clock clock,
                        @Value("${app.registry.dedup.threshold:0.85}") double threshold) {
        this.repository = repository;
        this.personRepository = personRepository;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.engine = new DedupEngine(threshold);
    }

    /** Scans the registry snapshot against the new person; raises OPEN candidates. */
    @Transactional
    public int scanFor(PersonRegistered event) {
        var subject = personRepository.findById(event.personId()).orElse(null);
        if (subject == null) {
            return 0;
        }
        List<com.java700.crvs.registry.Person> snapshot = personRepository
                .findByDobAndSex(subject.getDob(), subject.getSex());
        int created = 0;
        for (DedupEngine.ScoredPair pair : engine.findCandidates(snapshot, subject)) {
            boolean exists = repository.existsByPersonAIdAndPersonBIdAndStatus(
                    pair.personAId(), pair.personBId(), "OPEN");
            if (exists) {
                continue;
            }
            repository.save(new DedupCandidate(UUID.randomUUID().toString(), pair.personAId(),
                    pair.personBId(), BigDecimal.valueOf(pair.score()), Instant.now(clock)));
            metrics.incrementDedupCandidates();
            created++;
        }
        if (created > 0) {
            log.info("Dedup scan: {} candidate(s) raised for person {}", created, event.personId());
        }
        return created;
    }

    @Transactional
    public DedupApi.CandidateView confirm(String id) {
        DedupCandidate c = load(id);
        if (c.getStatus() != Status.OPEN) {
            throw new Problems.Conflict("Candidate is already decided");
        }
        c.decide(Status.CONFIRMED, SecurityUtil.currentUsername(), Instant.now(clock));
        repository.save(c);
        audit.record("DEDUP_CONFIRMED", "DEDUP_CANDIDATE", id,
                "Confirmed duplicate: " + c.getPersonAId() + " <-> " + c.getPersonBId());
        return DedupApi.CandidateView.from(c);
    }

    @Transactional
    public DedupApi.CandidateView dismiss(String id) {
        DedupCandidate c = load(id);
        if (c.getStatus() != Status.OPEN) {
            throw new Problems.Conflict("Candidate is already decided");
        }
        c.decide(Status.DISMISSED, SecurityUtil.currentUsername(), Instant.now(clock));
        repository.save(c);
        audit.record("DEDUP_DISMISSED", "DEDUP_CANDIDATE", id,
                "Dismissed duplicate: " + c.getPersonAId() + " <-> " + c.getPersonBId());
        return DedupApi.CandidateView.from(c);
    }

    @Transactional(readOnly = true)
    public List<DedupApi.CandidateView> open() {
        return repository.findByStatus("OPEN").stream()
                .map(DedupApi.CandidateView::from).toList();
    }

    private DedupCandidate load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Dedup candidate not found"));
    }

    /** Event handler: every registered birth is scanned against the registry. */
    @org.springframework.stereotype.Component
    public static class OnPersonRegistered implements DomainEventHandler<PersonRegistered> {

        private final DedupService service;

        public OnPersonRegistered(DedupService service) {
            this.service = service;
        }

        @Override
        public Class<PersonRegistered> supportedType() {
            return PersonRegistered.class;
        }

        @Override
        @Transactional
        public void handle(PersonRegistered event) {
            service.scanFor(event);
        }
    }
}
