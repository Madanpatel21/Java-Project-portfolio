package com.java700.crvs.registration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java700.crvs.common.api.PageResponse;
import com.java700.crvs.common.api.Problems;
import com.java700.crvs.common.audit.AuditLogService;
import com.java700.crvs.common.web.IdempotencyService;
import com.java700.crvs.ledger.LedgerService;
import com.java700.crvs.messaging.DomainEvent;
import com.java700.crvs.messaging.DomainEventBus;
import com.java700.crvs.observability.Metrics;
import com.java700.crvs.registry.NationalIdGenerator;
import com.java700.crvs.registry.Office;
import com.java700.crvs.registry.Person;
import com.java700.crvs.offices.OfficeRepository;
import com.java700.crvs.registry.PersonRepository;
import com.java700.crvs.registration.Registration.Status;
import com.java700.crvs.registration.Registration.Type;
import com.java700.crvs.security.LocalUser;
import com.java700.crvs.security.LocalUserRepository;
import com.java700.crvs.security.SecurityUtil;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Life-event registration with four-eyes control. On approval:
 * <ul>
 *   <li>BIRTH — creates the Person (national id + checksum), appends BIRTH to both chains.</li>
 *   <li>MARRIAGE — links two ACTIVE persons, appends MARRIAGE to the primary person's chain.</li>
 *   <li>DEATH — marks the person DECEASED (propagates to verification/certificates), appends DEATH.</li>
 *   <li>CORRECTION — applies an amendment preserving the original values in the ledger payload.</li>
 * </ul>
 * Segregation of duties: the registrar who captured a record may not approve it.
 */
@Service
public class RegistrationService {

    private final RegistrationRepository repository;
    private final PersonRepository personRepository;
    private final OfficeRepository officeRepository;
    private final LocalUserRepository localUserRepository;
    private final LedgerService ledger;
    private final NationalIdGenerator nationalIds;
    private final DomainEventBus bus;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final ObjectMapper mapper;
    private final Clock clock;

    public RegistrationService(RegistrationRepository repository, PersonRepository personRepository,
                               OfficeRepository officeRepository, LocalUserRepository localUserRepository,
                               LedgerService ledger, NationalIdGenerator nationalIds, DomainEventBus bus,
                               IdempotencyService idempotency, AuditLogService audit, Metrics metrics,
                               ObjectMapper mapper, Clock clock) {
        this.repository = repository;
        this.personRepository = personRepository;
        this.officeRepository = officeRepository;
        this.localUserRepository = localUserRepository;
        this.ledger = ledger;
        this.nationalIds = nationalIds;
        this.bus = bus;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.mapper = mapper;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- capture

    @Transactional
    public RegistrationApi.CreateResponse createBirth(RegistrationApi.BirthRequest req,
                                                      String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "REGISTRATION_BIRTH");
        if (existing != null) {
            return new RegistrationApi.CreateResponse(existing, repository.findById(existing)
                    .map(r -> r.getStatus().name()).orElse("PENDING"));
        }
        try {
            LocalUser registrar = currentUser();
            String officeId = requireOffice(registrar);
            Map<String, Object> payload = new HashMap<>();
            payload.put("fullName", req.fullName());
            payload.put("dob", req.dob().toString());
            payload.put("sex", req.sex());
            payload.put("placeOfBirth", req.placeOfBirth());
            payload.put("parentNames", req.parentNames());
            Registration r = save(Type.BIRTH, null, null, payload, officeId, registrar);
            idempotency.complete(idempotencyKey, r.getId(), 201);
            return new RegistrationApi.CreateResponse(r.getId(), r.getStatus().name());
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public RegistrationApi.CreateResponse createMarriage(RegistrationApi.MarriageRequest req,
                                                         String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "REGISTRATION_MARRIAGE");
        if (existing != null) {
            return new RegistrationApi.CreateResponse(existing, repository.findById(existing)
                    .map(r -> r.getStatus().name()).orElse("PENDING"));
        }
        try {
            Person a = person(req.personId());
            Person b = person(req.spousePersonId());
            validateMarriage(a, b);
            LocalUser registrar = currentUser();
            String officeId = requireOffice(registrar);
            Map<String, Object> payload = new HashMap<>();
            payload.put("spouseNationalId", b.getNationalId());
            payload.put("spouseName", b.getFullName());
            Registration r = save(Type.MARRIAGE, a.getId(), b.getId(), payload, officeId, registrar);
            idempotency.complete(idempotencyKey, r.getId(), 201);
            return new RegistrationApi.CreateResponse(r.getId(), r.getStatus().name());
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public RegistrationApi.CreateResponse createDeath(RegistrationApi.DeathRequest req,
                                                      String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "REGISTRATION_DEATH");
        if (existing != null) {
            return new RegistrationApi.CreateResponse(existing, repository.findById(existing)
                    .map(r -> r.getStatus().name()).orElse("PENDING"));
        }
        try {
            Person p = person(req.personId());
            if (p.getStatus() == Person.Status.DECEASED) {
                throw new Problems.Conflict("Person is already registered as deceased");
            }
            LocalUser registrar = currentUser();
            String officeId = requireOffice(registrar);
            Map<String, Object> payload = new HashMap<>();
            payload.put("cause", req.cause());
            Registration r = save(Type.DEATH, p.getId(), null, payload, officeId, registrar);
            idempotency.complete(idempotencyKey, r.getId(), 201);
            return new RegistrationApi.CreateResponse(r.getId(), r.getStatus().name());
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    @Transactional
    public RegistrationApi.CreateResponse createCorrection(RegistrationApi.CorrectionRequest req,
                                                           String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "REGISTRATION_CORRECTION");
        if (existing != null) {
            return new RegistrationApi.CreateResponse(existing, repository.findById(existing)
                    .map(r -> r.getStatus().name()).orElse("PENDING"));
        }
        try {
            Person p = person(req.personId());
            LocalUser registrar = currentUser();
            String officeId = requireOffice(registrar);
            Map<String, Object> payload = new HashMap<>();
            payload.put("field", req.field());
            payload.put("newValue", req.newValue());
            payload.put("reason", req.reason());
            payload.put("oldValue", currentValue(p, req.field()));
            Registration r = save(Type.CORRECTION, p.getId(), null, payload, officeId, registrar);
            idempotency.complete(idempotencyKey, r.getId(), 201);
            return new RegistrationApi.CreateResponse(r.getId(), r.getStatus().name());
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    // ---------------------------------------------------------------- decision

    @Transactional
    public RegistrationApi.RegistrationView decide(String id, boolean approve, String note,
                                                   String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "REGISTRATION_DECISION");
        Registration r;
        try {
            r = repository.findById(id)
                    .orElseThrow(() -> new Problems.NotFound("Registration not found"));
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
        if (existing != null) {
            return view(r);
        }
        if (r.getStatus() != Status.PENDING) {
            throw new Problems.Conflict("Registration is already decided");
        }
        LocalUser supervisor = currentUser();
        if (supervisor.getId().equals(r.getRegistrarId())) {
            throw new Problems.Conflict(
                    "Segregation of duties: the registrar may not approve their own capture");
        }
        if (!approve) {
            r.decide(Status.REJECTED, supervisor.getId(), supervisor.getUsername(),
                    Instant.now(clock), note);
            repository.save(r);
            audit.record("REGISTRATION_REJECTED", "REGISTRATION", r.getId(), note);
            idempotency.complete(idempotencyKey, r.getId(), 200);
            return view(r);
        }
        applyApproval(r, supervisor);
        r.decide(Status.APPROVED, supervisor.getId(), supervisor.getUsername(),
                Instant.now(clock), note);
        repository.save(r);
        metrics.incrementRegistrationsApproved();
        metrics.setOpenRegistrations(repository.countByStatus("PENDING"));
        audit.record("REGISTRATION_APPROVED", "REGISTRATION", r.getId(),
                r.getType().name() + " approved by " + supervisor.getUsername());
        idempotency.complete(idempotencyKey, r.getId(), 200);
        return view(r);
    }

    /** Applies the domain effect of an approval; ledger appends happen in the same transaction. */
    private void applyApproval(Registration r, LocalUser supervisor) {
        Map<String, Object> payload = parse(r.getPayloadJson());
        switch (r.getType()) {
            case BIRTH -> {
                Person person = new Person(UUID.randomUUID().toString(), nationalIds.next(),
                        (String) payload.get("fullName"),
                        java.time.LocalDate.parse((String) payload.get("dob")),
                        (String) payload.get("sex"), (String) payload.get("placeOfBirth"),
                        (String) payload.get("parentNames"),
                        regionOf(r.getOfficeId()), Instant.now(clock));
                personRepository.save(person);
                r.linkPerson(person.getId());
                ledger.append(person.getId(), "BIRTH", supervisor.getUsername(), payload);
                bus.publish(new PersonRegistered(UUID.randomUUID().toString(), Instant.now(clock),
                        person.getId(), person.getFullName(), person.getDob(), person.getSex(),
                        person.getParentNames()));
            }
            case MARRIAGE -> {
                Person a = personRepository.findById(r.getPersonId()).orElseThrow();
                ledger.append(a.getId(), "MARRIAGE", supervisor.getUsername(), payload);
            }
            case DEATH -> {
                Person p = personRepository.findById(r.getPersonId()).orElseThrow();
                p.markDeceased(Instant.now(clock));
                personRepository.save(p);
                ledger.append(p.getId(), "DEATH", supervisor.getUsername(), payload);
                bus.publish(new DeathRegistered(UUID.randomUUID().toString(), Instant.now(clock),
                        p.getId(), p.getNationalId()));
            }
            case CORRECTION -> {
                Person p = personRepository.findById(r.getPersonId()).orElseThrow();
                Map<String, Object> before = Map.of(
                        "fullName", p.getFullName(),
                        "dob", p.getDob().toString(),
                        "placeOfBirth", p.getPlaceOfBirth(),
                        "parentNames", p.getParentNames() == null ? "" : p.getParentNames());
                String field = (String) payload.get("field");
                String newValue = (String) payload.get("newValue");
                switch (field) {
                    case "fullName" -> p.applyCorrection(newValue, p.getDob(), p.getPlaceOfBirth(),
                            p.getParentNames());
                    case "dob" -> p.applyCorrection(p.getFullName(),
                            java.time.LocalDate.parse(newValue), p.getPlaceOfBirth(), p.getParentNames());
                    case "placeOfBirth" -> p.applyCorrection(p.getFullName(), p.getDob(), newValue,
                            p.getParentNames());
                    case "parentNames" -> p.applyCorrection(p.getFullName(), p.getDob(),
                            p.getPlaceOfBirth(), newValue);
                    default -> throw new Problems.BadRequest("Unsupported correction field: " + field);
                }
                personRepository.save(p);
                Map<String, Object> amendment = new HashMap<>();
                amendment.put("field", field);
                amendment.put("oldValue", payload.get("oldValue"));
                amendment.put("newValue", newValue);
                amendment.put("reason", payload.get("reason"));
                amendment.put("before", before);
                ledger.append(p.getId(), "AMENDMENT", supervisor.getUsername(), amendment);
            }
        }
    }

    // ---------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public PageResponse<RegistrationApi.RegistrationView> pending(String officeId, int page, int size) {
        PageRequest pr = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt"));
        var result = officeId == null
                ? repository.findByStatus("PENDING", pr)
                : repository.findByStatusAndOfficeId("PENDING", officeId, pr);
        return PageResponse.from(result.map(r -> view(r)));
    }

    @Transactional(readOnly = true)
    public List<RegistrationApi.RegistrationView> historyFor(String personId) {
        return repository.findByPersonIdOrderByCreatedAtDesc(personId).stream()
                .map(r -> view(r)).toList();
    }

    private RegistrationApi.RegistrationView view(Registration r) {
        return RegistrationApi.RegistrationView.from(r, parse(r.getPayloadJson()));
    }

    // ---------------------------------------------------------------- helpers

    private Registration save(Type type, String personId, String spouseId,
                              Map<String, Object> payload, String officeId, LocalUser registrar) {
        try {
            String json = mapper.writeValueAsString(payload);
            return repository.save(new Registration(UUID.randomUUID().toString(), type, personId,
                    spouseId, json, officeId, registrar.getId(), registrar.getUsername(),
                    Instant.now(clock)));
        } catch (JsonProcessingException e) {
            throw new Problems.BadRequest("Invalid payload");
        }
    }

    private Map<String, Object> parse(String json) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private LocalUser currentUser() {
        return localUserRepository.findByUsername(SecurityUtil.currentUsername())
                .orElseThrow(() -> new Problems.NotFound("Authenticated user not found"));
    }

    private String requireOffice(LocalUser user) {
        if (user.getOfficeId() == null || user.getOfficeId().isBlank()) {
            throw new Problems.BadRequest("Registrar is not assigned to a registry office");
        }
        officeRepository.findById(user.getOfficeId())
                .orElseThrow(() -> new Problems.NotFound("Registry office not found"));
        return user.getOfficeId();
    }

    private String regionOf(String officeId) {
        return officeRepository.findById(officeId)
                .map(Office::getRegion)
                .orElseThrow(() -> new Problems.NotFound("Registry office not found"));
    }

    private Person person(String id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Person not found"));
    }

    private void validateMarriage(Person a, Person b) {
        if (a.getId().equals(b.getId())) {
            throw new Problems.BadRequest("Cannot marry a person to themselves");
        }
        if (a.getStatus() == Person.Status.DECEASED || b.getStatus() == Person.Status.DECEASED) {
            throw new Problems.Conflict("Cannot register a marriage involving a deceased person");
        }
        boolean alreadyMarried = repository.findAll().stream()
                .filter(r -> r.getType() == Type.MARRIAGE)
                .filter(r -> r.getStatus() == Status.APPROVED)
                .anyMatch(r -> r.getPersonId().equals(a.getId())
                        && r.getSpousePersonId().equals(b.getId()));
        if (alreadyMarried) {
            throw new Problems.Conflict("Marriage between these persons is already registered");
        }
    }

    private String currentValue(Person p, String field) {
        return switch (field) {
            case "fullName" -> p.getFullName();
            case "dob" -> p.getDob().toString();
            case "placeOfBirth" -> p.getPlaceOfBirth();
            case "parentNames" -> p.getParentNames() == null ? "" : p.getParentNames();
            default -> throw new Problems.BadRequest("Unsupported correction field: " + field);
        };
    }

    public record PersonRegistered(String eventId, Instant occurredAt, String personId,
                                   String fullName, java.time.LocalDate dob, String sex,
                                   String parentNames) implements DomainEvent {
    }

    public record DeathRegistered(String eventId, Instant occurredAt, String personId,
                                  String nationalId) implements DomainEvent {
    }
}
