package com.java700.crvs.certificates;


import com.java700.crvs.certificates.Certificate.Status;
import com.java700.crvs.certificates.Certificate.Type;
import com.java700.crvs.common.api.Problems;
import com.java700.crvs.common.audit.AuditLogService;
import com.java700.crvs.common.web.IdempotencyService;
import com.java700.crvs.ledger.HashChain;
import com.java700.crvs.ledger.LedgerService;
import com.java700.crvs.observability.Metrics;
import com.java700.crvs.registry.Person;
import com.java700.crvs.registry.PersonRepository;
import com.java700.crvs.security.SecurityUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Civil-status certificate issuance. Tokens embed a content-derived hash so a certificate's
 * contents can be verified offline against the printed QR code; revocation is instant.
 */
@Service
public class CertificateService {

    private final CertificateRepository repository;
    private final PersonRepository personRepository;
    private final LedgerService ledger;
    private final IdempotencyService idempotency;
    private final AuditLogService audit;
    private final Metrics metrics;
    private final Clock clock;
    private final String pepper;

    public CertificateService(CertificateRepository repository, PersonRepository personRepository,
                              LedgerService ledger, IdempotencyService idempotency,
                              AuditLogService audit, Metrics metrics, Clock clock,
                              @Value("${app.registry.certificate-pepper}") String pepper) {
        this.repository = repository;
        this.personRepository = personRepository;
        this.ledger = ledger;
        this.idempotency = idempotency;
        this.audit = audit;
        this.metrics = metrics;
        this.clock = clock;
        this.pepper = pepper;
    }

    @Transactional
    public CertificateApi.CertificateView issue(String personId, String type, String idempotencyKey) {
        String existing = idempotency.begin(idempotencyKey, "CERTIFICATE");
        if (existing != null) {
            return CertificateApi.CertificateView.from(load(existing));
        }
        try {
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new Problems.NotFound("Person not found"));
            Type certType = parseType(type);
            if (certType == Type.DEATH && person.getStatus() != Person.Status.DECEASED) {
                throw new Problems.Conflict("Death certificates require a registered death");
            }
            if (certType != Type.DEATH && person.getStatus() == Person.Status.DECEASED) {
                throw new Problems.Conflict("Cannot issue " + certType.name().toLowerCase()
                        + " certificates for a deceased person");
            }
            String content = HashChain.canonicalJson(Map.of(
                    "personNationalId", person.getNationalId(),
                    "fullName", person.getFullName(),
                    "dob", person.getDob().toString(),
                    "sex", person.getSex(),
                    "placeOfBirth", person.getPlaceOfBirth(),
                    "type", certType.name()));
            String contentHash = sha256Hex(content + "|" + pepper);
            String token = contentHash.substring(0, 16) + sha256Hex(
                    UUID.randomUUID() + "|" + contentHash).substring(0, 16);
            Certificate certificate = new Certificate(UUID.randomUUID().toString(), personId,
                    certType, token, contentHash, Instant.now(clock), SecurityUtil.currentUsername());
            repository.save(certificate);
            ledger.append(personId, "CERTIFICATE_ISSUED", SecurityUtil.currentUsername(),
                    Map.of("certificateId", certificate.getId(), "type", certType.name(),
                            "contentHash", contentHash));
            metrics.incrementCertificatesIssued();
            audit.record("CERTIFICATE_ISSUED", "CERTIFICATE", certificate.getId(),
                    certType.name() + " certificate for person " + personId);
            idempotency.complete(idempotencyKey, certificate.getId(), 201);
            return CertificateApi.CertificateView.from(certificate);
        } catch (RuntimeException e) {
            idempotency.abandon(idempotencyKey);
            throw e;
        }
    }

    /** Public verification by token: reveals identity only on a valid, non-revoked token. */
    @Transactional(readOnly = true)
    public CertificateApi.VerificationView verify(String token) {
        Certificate certificate = repository.findByToken(token)
                .orElseThrow(() -> new Problems.NotFound("Certificate not found"));
        Person person = personRepository.findById(certificate.getPersonId())
                .orElseThrow(() -> new Problems.NotFound("Person not found"));
        boolean valid = certificate.getStatus() == Status.VALID;
        return new CertificateApi.VerificationView(token, valid, certificate.getStatus().name(),
                certificate.getType().name(),
                valid ? person.getFullName() : "***",
                valid ? person.getDob().toString() : "***",
                valid ? person.getNationalId() : "***");
    }

    @Transactional
    public CertificateApi.CertificateView revoke(String id, String reason) {
        Certificate certificate = load(id);
        if (certificate.getStatus() != Status.VALID) {
            throw new Problems.Conflict("Certificate is already revoked");
        }
        certificate.revoke(SecurityUtil.currentUsername(), Instant.now(clock), reason);
        repository.save(certificate);
        ledger.append(certificate.getPersonId(), "CERTIFICATE_REVOKED",
                SecurityUtil.currentUsername(),
                Map.of("certificateId", id, "reason", reason == null ? "" : reason));
        audit.record("CERTIFICATE_REVOKED", "CERTIFICATE", id, reason);
        return CertificateApi.CertificateView.from(certificate);
    }

    @Transactional(readOnly = true)
    public List<CertificateApi.CertificateView> forPerson(String personId) {
        return repository.findByPersonIdOrderByIssuedAtDesc(personId).stream()
                .map(CertificateApi.CertificateView::from).toList();
    }

    private Certificate load(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new Problems.NotFound("Certificate not found"));
    }

    private static Type parseType(String type) {
        try {
            return Type.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Problems.BadRequest("Certificate type must be BIRTH, MARRIAGE or DEATH");
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
