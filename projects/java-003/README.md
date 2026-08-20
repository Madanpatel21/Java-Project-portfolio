<div align="center">

# <img src="docs/logo.png" width="88" alt="logo" style="vertical-align:middle"/> &nbsp;Contract Lifecycle & Obligation Engine

**JAVA-003** · LegalTech / Enterprise · Spring Boot 3.5.3 · Java 21

[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](.)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-6DB33F?logo=spring&logoColor=white)](.)
[![Build](https://img.shields.io/badge/build-passing-brightgreen)](.)
[![Tests](https://img.shields.io/badge/tests-18%20passed-brightgreen)](.)
[![Static Analysis](https://img.shields.io/badge/checkstyle%2Bspotbugs-clean-brightgreen)](.)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](.)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?logo=rabbitmq&logoColor=white)](.)

*Versioned clauses with role-based redaction, four-eyes activation, and an obligation SLA engine that never lets a renewal, payment or exit right slip.*

</div>

---

## 📖 What this is

| | |
|---|---|
| **Business problem** | Obligations buried in contracts (renewals, payments, exit rights) are missed — costing millions and breaching compliance. |
| **Engineering problem** | Immutable contract versions with clause-level diffing, clearance-based redaction, a four-eyes approval chain, and an obligation state machine with SLA windows, overdue escalation and recurrence. |
| **Why it is industrial** | Clause-level security (finance sees pricing, auditors see redactions), audited waivers, recurring obligations that self-spawn — the anatomy of legal-grade governance. |

## ⚡ Quickstart

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev     # zero dependencies
# Swagger UI → http://localhost:8080/swagger-ui.html
```

```bash
cp .env.example .env && docker compose up --build       # PostgreSQL 16 · RabbitMQ · Prometheus · Grafana
```

**Demo users** (password `Password123!`): `legal` · `cmanager` · `owner` · `finance` · `auditor` · `admin`

## 🎬 Live demo (real server output)

<img src="docs/demo.gif" width="100%" alt="live demo"/>

Captured against a running instance: **contract creation → versioned clauses → clearance redaction (FINANCE sees the price clause, AUDITOR gets REDACTED) → four-eyes activation (manager + legal) → obligation with a 30-day SLA window → scan → NOTIFIED → waiver blocked for the manager, audited for legal.**

## 🏗 Architecture

```mermaid
flowchart LR
    subgraph capture["Contract Capture"]
        C["POST /contracts"] --> V["POST /contracts/{id}/versions"]
        V --> D["Clause diff (ADDED/REMOVED/MODIFIED)"]
    end
    D --> CL["Clause reads\n(role clearance ≥ sensitivity)"]
    C --> A["Four-eyes activation\n(LEGAL + CONTRACT_MANAGER)"]
    A -->|"ACTIVE"| OB["Obligations\n(OPEN)"]
    OB --> SCAN["SLA scan (daily + manual)"]
    SCAN -->|"in window"| N["NOTIFIED"]
    SCAN -->|"past due"| OV["OVERDUE"]
    N --> ACK["ACKNOWLEDGED"]
    ACK --> COM["COMPLETED"]
    COM -->|"repeatIntervalDays"| OB
    N --> W["WAIVED (four-eyes, audited)"]
    OV --> W
    OV --> COM
```

```mermaid
sequenceDiagram
    participant M as Contract Manager
    participant L as Legal Counsel
    participant S as Contract Service
    participant E as Obligation Engine
    M->>S: create contract (CT-1001, DRAFT)
    M->>S: add version 1 (price clause sensitivity 3)
    S-->>S: diff vs previous version (none)
    M->>S: activate
    S-->>M: 1 of 2 approvals (still DRAFT)
    L->>S: activate
    S-->>S: four-eyes complete -> ACTIVE
    M->>S: attach obligation (payment, due +2d, 30d window)
    S->>E: obligation OPEN
    M->>E: run SLA scan
    E-->>M: obligation NOTIFIED (inside window)
    M->>E: waive
    E-->>M: 403 four-eyes
    L->>E: waive ("terms renegotiated")
    E-->>L: WAIVED, audited
```

## ⚡ Performance (measured on a local run)

<img src="docs/perf.gif" width="100%" alt="load test"/>

| Metric | Value |
|---|---|
| Requests | 400 mixed GET (contracts / obligations / health) |
| Concurrency | 10 workers |
| Throughput | **272.2 req/s** |
| Latency p50 / p95 / p99 | 31.4 ms / 49.4 ms / 60.6 ms |
| Result | 400/400 HTTP 200 |

## 🧪 Verified test output

<img src="docs/tests.gif" width="100%" alt="test output"/>

| Suite | Coverage |
|---|---|
| `ContractDiffTest` (5) | ADDED/REMOVED/MODIFIED detection, old+new text, malformed JSON, clause parsing |
| `ContractFlowIT` (5) | four-eyes activation, clearance redaction per role, obligation SLA lifecycle + waiver, recurrence spawning, version diff endpoint |
| `SecurityIT` (6) | role matrix, four-eyes enforcement, auditor read-only, injection, lockout, validation-first |
| `PostgreSQLMigrationIT` | Flyway V1–V2 on real PostgreSQL 16 (CI) |

`mvn verify -Pstatic-analysis` → **Checkstyle clean · SpotBugs 0 bugs**.

## 🔐 Security model

| Capability | LEGAL | CONTRACT_MANAGER | BUSINESS_OWNER | FINANCE | AUDITOR | ADMIN |
|---|---|---|---|---|---|---|
| Create contract / version | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Activate (four-eyes) | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |
| Clause clearance level | 4 | 4 | 3 | 3 | 2 | 4 |
| Attach / complete obligations | ✅ | ✅ | ✅(complete) | ❌ | ❌ | ✅ |
| Waive obligation (four-eyes) | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| Terminate contract | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |

Plus: JWT + Argon2id local IdP with lockout · Idempotency-Key on every mutation · RFC 7807 errors with correlation IDs · full audit trail · threat model in `docs/SECURITY.md`.

## 🗂 Repository

```
projects/java-003/
├── src/main/java/com/java700/contracts/
│   ├── matching/     ContractDiff (clause-level ADDED/REMOVED/MODIFIED)
│   ├── domain/       Contract, versions, Obligation (SLA state machine), events, approvals
│   ├── service/      ContractService (versions, four-eyes, obligations, SLA scan, recurrence)
│   ├── api/          REST controllers
│   └── security/     Roles + clause-clearance model, local IdP, JWT
├── src/main/resources/db/migration/   V1 common · V2 contracts schema
├── docker/           docker-compose: PostgreSQL, RabbitMQ, Prometheus, Grafana
├── docs/             ARCHITECTURE · SECURITY · RUNBOOK · ADRs · logo · demo/perf/tests GIFs
└── jmeter/           load plan
```

## 🧰 Engineering highlights

- **Immutable versioning + diffs** — every version is frozen; clause-level diffs (ADDED/REMOVED/MODIFIED) are computed on demand for legal review.
- **Clause-level security** — each clause carries a sensitivity level; reads are redacted by role clearance, so finance sees pricing while auditors see `[REDACTED]`.
- **Four-eyes everywhere** — activation needs LEGAL + CONTRACT_MANAGER; waivers need LEGAL; every decision is audited.
- **Obligation SLA engine** — window-based notifications, overdue escalation, acknowledgement/completion, and recurring obligations that self-spawn the next instance.
- **Zero-dependency dev, full stack in one command** — H2 in dev, PostgreSQL + RabbitMQ via health-gated Compose in local.

---

<div align="center">

**Part of the [Java-700 portfolio](https://github.com/Madanpatel21/Java-Project-portfolio)** — 700 unique industrial-grade Java systems.

</div>
