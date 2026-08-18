# JAVA-001 — Workforce Compliance Evidence Platform

**Tier 1 (Advanced) · Industry: Enterprise HR / Compliance**

A production-oriented platform that correlates access grants, approvals, policy versions and
access events into a **hash-chained, tamper-evident evidence ledger**, detects compliance
violations with a typed rule engine, manages recertification campaigns and produces
**HMAC-signed auditor export bundles**.

> Regulators fine organizations that cannot evidence *who had access to what, when, and under
> whose approval*. This system makes that chain provable and tamper-evident.

## Quickstart (zero dependencies)

```bash
# Prerequisite: JDK 21+ and Maven 3.9+
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# or
mvn -DskipTests package && java -jar target/workforce-compliance-1.0.0.jar --spring.profiles.active=dev
```

The dev profile uses embedded H2 (PostgreSQL mode), the built-in identity provider
(Argon2id + progressive lockout) and in-process event dispatch — **no Docker needed**.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Actuator:  http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus

### Demo credentials (dev seed)

| User | Role | Password |
|---|---|---|
| alice / bob | EMPLOYEE | `Password123!` |
| carol / dave | ACCESS_MANAGER (dual-control pair) | `Password123!` |
| eve | COMPLIANCE_OFFICER | `Password123!` |
| frank | COMPLIANCE_ADMIN | `Password123!` |
| grace | AUDITOR (read-only) | `Password123!` |
| integrator | INTEGRATION | `Password123!` |

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"frank","password":"Password123!"}' | jq -r .accessToken)

# trigger correlation → expect 5 violations seeded for alice
curl -s -X POST http://localhost:8080/api/v1/compliance/run -H "Authorization: Bearer $TOKEN" | jq
curl -s http://localhost:8080/api/v1/violations -H "Authorization: Bearer $TOKEN" | jq
curl -s http://localhost:8080/api/v1/evidence/verify -H "Authorization: Bearer $TOKEN" | jq
```

## Full local production-like stack

```bash
cp .env.example .env
docker compose up --build
```

Brings up: **PostgreSQL 16 · Redis 7 · RabbitMQ 3.13 (with DLX) · Keycloak 26 · Prometheus ·
Grafana · Jaeger** and the app itself in the `local` profile (OIDC federation, broker messaging,
Redis caches/rate-limits, ECS-structured logs, OTLP traces).

| Service | URL |
|---|---|
| API + Swagger | http://localhost:8080/swagger-ui.html |
| Keycloak admin | http://localhost:8081 (admin/admin) |
| RabbitMQ console | http://localhost:15672 |
| Prometheus | http://localhost:9090 |
| Grafana (dashboard "JAVA-001 Workforce Compliance") | http://localhost:3000 |
| Jaeger | http://localhost:16686 |

## Architecture (one paragraph)

Modular monolith (Spring Boot 3.5 / Java 21, virtual threads) — **identity & RBAC**, **versioned
policy store**, **access lifecycle with dual control**, **hash-chained evidence ledger** (SHA-256,
canonical JSON, DB advisory lock), **correlation engine** (5 typed rule evaluators), **violation
lifecycle** (OPEN→ACKNOWLEDGED→REMEDIATED→CLOSED), **recertification campaigns**, and **auditor
exports** (JSONL + HMAC-SHA256 + manifest). Events flow over a bus abstraction: RabbitMQ with
dead-letter queues in the `local` profile, in-process dispatch in `dev`. An outbox rescan
re-queues stale export jobs after crashes. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Security (one paragraph)

Stateless JWT (dev: HS256 local issuer; local profile: Keycloak OIDC), RBAC matrix + method
security, **segregation of duties** and **dual control** enforced in the domain, Argon2id +
progressive lockout for the local IdP, AES-style evidence tamper detection via hash chaining,
PII masking at the API boundary, rate limiting (in-memory/Redis), idempotency keys with replay
protection, RFC 7807 errors with correlation ids, security headers (CSP, frame-deny), and a full
[threat model](docs/SECURITY.md) with OWASP coverage.

## Repository layout

```
docs/                  README, architecture, threat model, ADRs, runbook, deployment, testing, config
src/main/java/...      modular monolith (packages = bounded contexts)
src/main/resources/    application.yml + dev/local/test profiles, Flyway migrations, logback
src/test/java/...      43 tests: unit, integration (MockMvc + H2 pg-mode), security/abuse, chain tamper
docker/                Dockerfile, docker-compose, prometheus, grafana, keycloak, rabbitmq
jmeter/                load/smoke plan
.github/workflows/     CI: verify + static analysis + package
checkstyle/            checkstyle rules + spotbugs exclude policy
```

## Verification status (quality gate)

| Gate | Result |
|---|---|
| `mvn verify` (43 tests: unit/IT/security/chain-tamper/resilience) | ✅ 0 failures (1 Testcontainers-PG test auto-skips without Docker; runs in CI) |
| `mvn verify -Pstatic-analysis` (Checkstyle + SpotBugs Max) | ✅ 0 violations, 0 bugs |
| Live smoke test (auth, SoD, dual control, export HMAC, chain verify) | ✅ verified end-to-end |
| Health (liveness/readiness + evidence-chain indicator) | ✅ UP |
| `mvn verify -Psecurity-scan` (OWASP dependency-check) | run in CI (needs NVD feed) |
