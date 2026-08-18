# JAVA-211 — Antimicrobial Stewardship Tracker

**Tier 2 (Expert) · Industry: Healthcare / Pharmacy**

Hospital-grade antimicrobial stewardship: guideline-governed prescription review, pharmacist
interventions with prescriber acceptance, **culture-driven drug-bug mismatch alerts and
de-escalation suggestions**, time-boxed pre-authorization for restricted drugs, DOT/DDD
utilization metrics and CLSI-style antibiogram aggregation.

> Antimicrobial resistance is a top-10 global health threat. This platform operationalizes the
> stewardship loop: prescribe → review → intervene → measure — with every clinical decision audited.

## Quickstart (zero dependencies)

```bash
# Prerequisites: JDK 21+ and Maven 3.9+
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# or
mvn -DskipTests package && java -jar target/antimicrobial-stewardship-1.0.0.jar --spring.profiles.active=dev
```

- Swagger UI: http://localhost:8080/swagger-ui.html · Health: http://localhost:8080/actuator/health

### Demo credentials (dev seed)

| User | Role | Password |
|---|---|---|
| pharmacist | PHARMACIST (reviews + interventions) | `Password123!` |
| prescriber | PRESCRIBER (orders, accepts/rejects) | `Password123!` |
| idphysician | ID_PHYSICIAN (restricted-drug approvals) | `Password123!` |
| microbiologist | MICROBIOLOGIST (cultures) | `Password123!` |
| infectioncontrol | INFECTION_CONTROL (read-only analytics) | `Password123!` |
| admin | STEWARDSHIP_ADMIN | `Password123!` |

Seeded clinical scenario: **Ada (ICU-1)** has empiric ceftriaxone (review due at 48h), pip-tazo
(renal dose needed — CrCl ≈ 28) and metronidazole (redundant anaerobic coverage); **Alan (MED-2)**
has empiric amox-clav for UTI.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token -H "Content-Type: application/json" \
  -d '{"username":"pharmacist","password":"Password123!"}' | jq -r .accessToken)

# evaluate the empiric ceftriaxone (finds IV→PO eligibility + more)
curl -s http://localhost:8080/api/v1/stewardship/evaluate/<RX_ID> -H "Authorization: Bearer $TOKEN" | jq

# utilization analytics
curl -s "http://localhost:8080/api/v1/metrics/utilization?from=2026-08-01T00:00:00Z&to=2026-08-19T00:00:00Z" \
  -H "Authorization: Bearer $TOKEN" | jq
```

## Full local production-like stack

```bash
cp .env.example .env
docker compose up --build    # PostgreSQL 16 · RabbitMQ 3.13 · Prometheus · Grafana
```

## Architecture (one paragraph)

Modular monolith (Spring Boot 3.5 / Java 21, virtual threads) — **patients/admissions/labs**,
**drug catalog (WHO DDD-aligned)**, **versioned stewardship guidelines**, **prescription lifecycle**
with restricted-drug pre-authorization, **rule engine** (MAX_DURATION, IV_TO_PO_ELIGIBILITY,
RENAL_ADJUSTMENT via Cockcroft-Gault, DRUG_BUG_MISMATCH, DE_ESCALATION_CANDIDATE,
REDUNDANT_COVERAGE), **review tasks** (time-based + culture-driven, deduplicated per trigger),
**interventions** (PROPOSED→ACCEPTED/REJECTED with mandatory rejection reasons), **antibiogram**
(first-isolate dedup, 30-isolate reporting gate) and **utilization metrics** (DOT, patient-days,
DOT/1000, DDD). Events flow over a bus abstraction: RabbitMQ with DLX in the `local` profile,
in-process in `dev`. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Security (one paragraph)

Stateless JWT (dev HS256 local IdP with Argon2id + lockout; local profile: OIDC-ready), RBAC
matrix + method security (pharmacist proposes, prescriber decides, ID physician authorizes,
infection control is read-only), **PHI masking** at the API boundary, RFC 7807 errors with
correlation ids, idempotency keys, rate-limited auth, security headers, clinical audit log.
Full [threat model](docs/SECURITY.md) with OWASP mapping.

## Verification status (quality gate)

| Gate | Result |
|---|---|
| `mvn verify` — 30 tests (unit/IT/security; Testcontainers PG in CI) | ✅ 0 failures |
| `mvn verify -Pstatic-analysis` (Checkstyle + SpotBugs Max) | ✅ 0 violations, 0 bugs |
| Live clinical smoke test | ✅ 8 scenarios verified (see below) |

Live-verified flows: PHI masking · rule-engine evaluation (IV→PO + renal + redundancy) ·
culture R-isolate → **DRUG_BUG_MISMATCH review task auto-created** · intervention accepted →
IV→PO applied · meropenem restricted pre-auth (PENDING→APPROVED→ACTIVE) · DOT/1000 + DDD
metrics · antibiogram gating · scheduled review scan.
