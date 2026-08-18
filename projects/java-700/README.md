# JAVA-700 — Digital ID & Civil Registry (CRVS)

**Tier 5 (Omega) · Industry: Government / Identity** — the final project of the 700-project catalog.

National civil registration and vital statistics: lifetime identity records, four-eyes
life-event registration (birth/marriage/death/correction), a **dual hash-chained event ledger**
(global registry chain + per-person life chain), checksummed national-ID generation, certificate
issuance/verification/revocation, fuzzy duplicate detection, deceased-status propagation and
vital-statistics analytics.

> A civil registry must answer one question for a lifetime: *who is this person, and what has
> their legal status been at every moment?* This system makes the answer provable — the ledger is
> tamper-evident at registry scale AND per-person, and a death registration instantly changes
> what banks and employers see.

## Quickstart (zero dependencies)

```bash
# Prerequisites: JDK 21+ and Maven 3.9+
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# or
mvn -DskipTests package && java -jar target/civil-registry-1.0.0.jar --spring.profiles.active=dev
```

- Swagger UI: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health

### Demo credentials (dev seed)

| User | Role | Password |
|---|---|---|
| registrar / registrar2 | REGISTRAR (NORTH / SOUTH office capture) | `Password123!` |
| supervisor | SUPERVISOR (four-eyes approvals) | `Password123!` |
| statistician | STATISTICIAN (read-only analytics) | `Password123!` |
| verifier | VERIFIER_CLIENT (third-party verification) | `Password123!` |
| admin | ADMIN (certificate revocation, dedup adjudication) | `Password123!` |

### Smoke flow

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token -H "Content-Type: application/json" \
  -d '{"username":"registrar","password":"Password123!"}' | jq -r .accessToken)

# capture a birth (PENDING) → supervisor approves → person + checksummed national id
curl -s -X POST http://localhost:8080/api/v1/registrations/birth -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: demo-1" -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","dob":"1990-05-17","sex":"F","placeOfBirth":"London","parentNames":"Byron Lovelace, Anne Isabella"}' | jq

# verify the global + per-person chains
curl -s http://localhost:8080/api/v1/ledger/verify -H "Authorization: Bearer <ADMIN_TOKEN>" | jq
```

## Full local production-like stack

```bash
cp .env.example .env
docker compose up --build    # PostgreSQL 16 · RabbitMQ 3.13 (DLX) · Prometheus · Grafana
```

## Architecture (one paragraph)

Modular monolith (Spring Boot 3.5 / Java 21, virtual threads) — **persons & offices**, **four-eyes
registrations** (registrar captures, supervisor decides; SoD enforced in-domain), **dual-chained
ledger** (every approved life event appends one entry linking both the global SHA-256 chain and
the person's own chain; advisory-lock single writer), **national-ID generator** (ISO 7064-style
checksum), **certificates** (content-hash tokens, QR-ready, revocable), **dedup engine**
(Jaro-Winkler scoring over blocking keys, adjudication workflow), **deceased-aware verification**
and **vital statistics** (births/deaths/marriages/natural-increase per region from the ledger).
Events flow over a bus abstraction: RabbitMQ with DLX in the `local` profile, in-process in `dev`.

## Security (one paragraph)

Stateless JWT (Argon2id local IdP + lockout in dev; OIDC-ready in local), RBAC matrix + method
security, four-eyes with segregation of duties, identity masking in listings (certificates and
verification disclose identity by design — that is their legal purpose), rate-limited auth and
verification endpoints, RFC 7807 errors with correlation ids, idempotency keys, security headers,
full audit log. Complete [threat model](docs/SECURITY.md) with OWASP mapping.

## Verification status (quality gate)

| Gate | Result |
|---|---|
| `mvn verify` — 37 tests (unit/IT/security; Testcontainers PostgreSQL in CI) | ✅ 0 failures |
| `mvn verify -Pstatic-analysis` (Checkstyle + SpotBugs Max) | ✅ 0 violations, 0 bugs |
| Live smoke test — 10 scenarios | ✅ verified end-to-end |

Live-verified: four-eyes birth (SoD 403 + dual-role 409) → person + checksummed national ID →
global + per-person chains valid → dedup candidate (score 0.9923) → certificate issue/verify/
revoke (identity masked after revocation) → death → DECEASED propagation → vital statistics →
full-chain verification across the complete lifecycle.
