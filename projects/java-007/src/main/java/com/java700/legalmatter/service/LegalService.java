package com.java700.legalmatter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.legalmatter.common.api.Problems;
import com.java700.legalmatter.common.audit.AuditLogService;
import com.java700.legalmatter.common.web.IdempotencyService;
import com.java700.legalmatter.domain.ConflictCheck;
import com.java700.legalmatter.domain.ConflictCheckRepository;
import com.java700.legalmatter.domain.DeadlineRule;
import com.java700.legalmatter.domain.DeadlineRuleRepository;
import com.java700.legalmatter.domain.EthicalWall;
import com.java700.legalmatter.domain.EthicalWallRepository;
import com.java700.legalmatter.domain.Matter;
import com.java700.legalmatter.domain.MatterDeadline;
import com.java700.legalmatter.domain.MatterDeadlineRepository;
import com.java700.legalmatter.domain.MatterParty;
import com.java700.legalmatter.domain.MatterPartyRepository;
import com.java700.legalmatter.domain.MatterRepository;
import com.java700.legalmatter.domain.Party;
import com.java700.legalmatter.domain.PartyRepository;
import com.java700.legalmatter.messaging.DomainEvent;
import com.java700.legalmatter.messaging.DomainEventBus;
import com.java700.legalmatter.screening.ConflictScreener;
import com.java700.legalmatter.screening.NameNormalizer;
import com.java700.legalmatter.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Legal matter management: parties graph, conflict screening, court deadlines, ethical walls. */
@Service
public class LegalService {

    private static final Logger log = LoggerFactory.getLogger(LegalService.class);

    private final PartyRepository partyRepository;
    private final MatterRepository matterRepository;
    private final MatterPartyRepository matterPartyRepository;
    private final ConflictCheckRepository checkRepository;
    private final DeadlineRuleRepository ruleRepository;
    private final MatterDeadlineRepository deadlineRepository;
    private final EthicalWallRepository wallRepository;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final ObjectMapper mapper;
    private final Clock clock;

    public LegalService(PartyRepository partyRepository, MatterRepository matterRepository,
                        MatterPartyRepository matterPartyRepository,
                        ConflictCheckRepository checkRepository,
                        DeadlineRuleRepository ruleRepository,
                        MatterDeadlineRepository deadlineRepository,
                        EthicalWallRepository wallRepository,
                        DomainEventBus bus, IdempotencyService idempotency,
                        AuditLogService audit, ObjectMapper mapper, Clock clock) {
        this.partyRepository = partyRepository;
        this.matterRepository = matterRepository;
        this.matterPartyRepository = matterPartyRepository;
        this.checkRepository = checkRepository;
        this.ruleRepository = ruleRepository;
        this.deadlineRepository = deadlineRepository;
        this.wallRepository = wallRepository;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- parties/matters

    @Transactional
    public String createParty(String name, String type, String idemKey) {
        String existing = idempotency.begin(idemKey, "PARTY");
        if (existing != null) {
            return existing;
        }
        try {
            Party.Type partyType;
            try {
                partyType = Party.Type.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new Problems.BadRequest("Type must be CLIENT, OPPONENT or RELATED");
            }
            Party party = new Party(UUID.randomUUID().toString(), name,
                    NameNormalizer.normalize(name), partyType, Instant.now(clock));
            partyRepository.save(party);
            audit.record("PARTY_CREATED", "PARTY", party.getId(), name);
            idempotency.complete(idemKey, party.getId(), 201);
            return party.getId();
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public String createMatter(String matterNo, String name, String clientPartyId,
                               String practiceArea, String idemKey) {
        String existing = idempotency.begin(idemKey, "MATTER");
        if (existing != null) {
            return existing;
        }
        try {
            if (matterRepository.findByMatterNo(matterNo).isPresent()) {
                throw new Problems.Conflict("Matter number already exists");
            }
            partyRepository.findById(clientPartyId)
                    .orElseThrow(() -> new Problems.NotFound("Client party not found"));
            Matter matter = new Matter(UUID.randomUUID().toString(), matterNo, name,
                    clientPartyId, practiceArea, Instant.now(clock));
            matterRepository.save(matter);
            matterPartyRepository.save(new MatterParty(UUID.randomUUID().toString(),
                    matter.getId(), clientPartyId, "CLIENT", Instant.now(clock)));
            audit.record("MATTER_OPENED", "MATTER", matter.getId(), matterNo);
            idempotency.complete(idemKey, matter.getId(), 201);
            return matter.getId();
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public void addMatterParty(String matterId, String partyId, String role) {
        Matter matter = loadMatter(matterId);
        partyRepository.findById(partyId)
                .orElseThrow(() -> new Problems.NotFound("Party not found"));
        if (!List.of("CLIENT", "OPPOSING", "ADVERSE", "WITNESS").contains(role.toUpperCase())) {
            throw new Problems.BadRequest("Role must be CLIENT, OPPOSING, ADVERSE or WITNESS");
        }
        matterPartyRepository.save(new MatterParty(UUID.randomUUID().toString(), matter.getId(),
                partyId, role.toUpperCase(), Instant.now(clock)));
        audit.record("MATTER_PARTY_ADDED", "MATTER", matterId, role + ": " + partyId);
    }

    // ---------------------------------------------------------------- conflict screening

    @Transactional
    public Api.ScreenView screen(String subjectName, List<String> adverseNames, String idemKey) {
        String existing = idempotency.begin(idemKey, "CONFLICT_CHECK");
        if (existing != null) {
            ConflictCheck check = checkRepository.findById(existing)
                    .orElseThrow(() -> new Problems.NotFound("Check not found"));
            return new Api.ScreenView(check.getId(), check.getRequestedBy(),
                    check.getCheckedAt(), check.getSubjectName(), check.getAdverseNames(),
                    check.getResult().name(), check.getDetailsJson());
        }
        try {
            ConflictScreener.ScreenResult result = ConflictScreener.screen(
                    partyRepository.findByActiveTrue(), matterPartyRepository.findAll(),
                    subjectName, adverseNames);
            String detailsJson;
            try {
                detailsJson = mapper.writeValueAsString(result.findings());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new IllegalStateException("Cannot serialize screening findings", e);
            }
            ConflictCheck check = new ConflictCheck(UUID.randomUUID().toString(),
                    SecurityUtil.currentUsername(), Instant.now(clock), subjectName,
                    String.join(", ", adverseNames),
                    ConflictCheck.Result.valueOf(result.result()), detailsJson);
            checkRepository.save(check);
            audit.record("CONFLICT_SCREENED", "CONFLICT_CHECK", check.getId(),
                    subjectName + " -> " + result.result());
            if ("CONFLICT".equals(result.result())) {
                bus.publish(new ConflictDetected(UUID.randomUUID().toString(), Instant.now(clock),
                        check.getId(), subjectName));
            }
            idempotency.complete(idemKey, check.getId(), 201);
            return new Api.ScreenView(check.getId(), check.getRequestedBy(), check.getCheckedAt(),
                    subjectName, check.getAdverseNames(), result.result(), detailsJson);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- deadlines

    @Transactional
    public List<Api.DeadlineView> computeDeadlines(String matterId, String jurisdiction,
                                                   LocalDate triggerDate, String idemKey) {
        String existing = idempotency.begin(idemKey, "DEADLINES");
        if (existing != null) {
            return deadlines(matterId);
        }
        try {
            Matter matter = loadMatter(matterId);
            int created = 0;
            for (DeadlineRule rule : ruleRepository.findByActiveTrue()) {
                if (!rule.getJurisdiction().equalsIgnoreCase(jurisdiction)) {
                    continue;
                }
                LocalDate due = triggerDate.plus(rule.getDaysOffset(), ChronoUnit.DAYS);
                deadlineRepository.save(new MatterDeadline(UUID.randomUUID().toString(),
                        matter.getId(), rule.getEventType(), jurisdiction, triggerDate, due));
                created++;
            }
            audit.record("DEADLINES_COMPUTED", "MATTER", matterId,
                    jurisdiction + " from " + triggerDate + " -> " + created + " deadlines");
            idempotency.complete(idemKey, matterId, 201);
            return deadlines(matterId);
        } catch (RuntimeException e) {
            idempotency.abandon(idemKey);
            throw e;
        }
    }

    @Transactional
    public Api.DeadlineView completeDeadline(String deadlineId) {
        MatterDeadline deadline = loadDeadline(deadlineId);
        if (deadline.getStatus() != MatterDeadline.Status.OPEN) {
            throw new Problems.Conflict("Only OPEN deadlines can be completed");
        }
        deadline.complete(SecurityUtil.currentUsername(), Instant.now(clock));
        deadlineRepository.save(deadline);
        audit.record("DEADLINE_COMPLETED", "MATTER_DEADLINE", deadlineId,
                "By " + SecurityUtil.currentUsername());
        return view(deadline);
    }

    @Transactional
    public int markMissed() {
        int missed = 0;
        for (MatterDeadline deadline : deadlineRepository.findByStatusAndDueAtBefore(
                "OPEN", LocalDate.now(clock))) {
            deadline.markMissed();
            deadlineRepository.save(deadline);
            bus.publish(new DeadlineMissed(UUID.randomUUID().toString(), Instant.now(clock),
                    deadline.getId(), deadline.getMatterId(), deadline.getEventType()));
            missed++;
        }
        if (missed > 0) {
            log.info("Deadline scan: {} deadlines missed", missed);
        }
        return missed;
    }

    // ---------------------------------------------------------------- ethical walls

    @Transactional
    public void addWall(String matterId, String roleName) {
        loadMatter(matterId);
        wallRepository.save(new EthicalWall(UUID.randomUUID().toString(), matterId, roleName));
        audit.record("ETHICAL_WALL_ADDED", "MATTER", matterId, roleName);
    }

    /** Matter access with ethical-wall enforcement. */
    @Transactional(readOnly = true)
    public Api.MatterView matterView(String matterId) {
        Matter matter = loadMatter(matterId);
        List<String> walls = wallRepository.findByMatterId(matterId).stream()
                .map(EthicalWall::getRoleName).toList();
        boolean blocked = walls.stream().anyMatch(w -> SecurityUtil.hasRole(w));
        if (blocked) {
            throw new Problems.Conflict("Access blocked by an ethical wall");
        }
        return new Api.MatterView(matter.getId(), matter.getMatterNo(), matter.getName(),
                matter.getStatus().name(), matter.getClientPartyId(), matter.getPracticeArea(),
                matter.getOpenedAt(), matter.getClosedAt());
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public List<Api.DeadlineView> deadlines(String matterId) {
        return deadlineRepository.findByMatterIdOrderByDueAtAsc(matterId).stream()
                .map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<Api.PartyView> parties() {
        return partyRepository.findAll().stream()
                .map(p -> new Api.PartyView(p.getId(), p.getName(), p.getType().name()))
                .toList();
    }

    private Matter loadMatter(String id) {
        return matterRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Matter not found"));
    }

    private MatterDeadline loadDeadline(String id) {
        return deadlineRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Deadline not found"));
    }

    private Api.DeadlineView view(MatterDeadline d) {
        return new Api.DeadlineView(d.getId(), d.getMatterId(), d.getEventType(),
                d.getJurisdiction(), d.getDueAt(), d.getStatus().name(), d.getCompletedBy());
    }

    public record ConflictDetected(String eventId, Instant occurredAt, String checkId,
                                   String subjectName) implements DomainEvent {
    }

    public record DeadlineMissed(String eventId, Instant occurredAt, String deadlineId,
                                 String matterId, String eventType) implements DomainEvent {
    }
}
