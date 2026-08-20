# ARCHITECTURE — Expense Fraud & Policy Analytics Engine (JAVA-008)

## Style
Modular monolith on Spring Boot 3.5.3 / Java 21 (virtual threads). One deployable,
seven business tables, an in-process event bus that upgrades to RabbitMQ via a
property flip (`app.messaging.enabled`). No external service is required at runtime
beyond a database — the scoring pipeline is fully embedded.

## Modules
| Module | Responsibility |
|---|---|
| `api` | REST controllers: claims, cases, tips, admin |
| `domain` | JPA entities + repositories (claims, policy rules, violations, duplicate groups, fraud cases, tips, peer baselines) |
| `service` | `ScoringService`, `CaseService`, `ClaimService`, `TipService`, `BaselineService`, `BaselineScheduler`, `Api` records |
| `security` | JWT issuance/validation, Argon2 local IdP, RBAC roles, lockout |
| `messaging` | `FraudEvents` records, in-process `DirectEventBus`, `RabbitEventBus` + DLX consumers |
| `observability` | Micrometer counters, risk-score distribution, scoring timer |
| `common` | audit log, idempotency, correlation IDs, rate limiting, problem+json errors |

## Scoring pipeline (the core)
1. **Policy rules** — data-driven rows (`policy_rules`, seeded by V3) evaluated by
   comparator: `GREATER_THAN`, `MISSING_RECEIPT`, `ROUND_AMOUNT`, `MERCHANT_CONTAINS`.
   Weight: BLOCKER 45 / VIOLATION 25 / WARNING 10.
2. **Weekend-mileage detector** — MILEAGE claims dated Sat/Sun get +20 (fabrication pattern).
3. **Peer outlier detector** — z-score of the amount against the department+category
   baseline (mean/std-dev, min 5 samples). z > 3.5 → +30, z > 2.5 → +20.
4. **Duplicate/split clustering (JGraphT)** — candidate claims (same employee, same
   merchant, ±30 days) become graph vertices; edges join exact duplicates
   (|Δamount| ≤ 1 %, |Δdate| ≤ 14 d) or split receipts (same day, each under cap,
   sum over cap). Connected components of size ≥ 2 persist as `duplicate_groups`
   evidence rows (+30 exact / +15 split).

Score = sum of weights clamped to [0,100]. Tier: HIGH ≥ 65, MEDIUM ≥ 35, LOW < 35.
Every reason is stored as JSON on the claim AND inside the fraud case — an auditor
can re-derive any score from persisted evidence.

## Case workflow (four-eyes)
`OPEN → REVIEWED → CONFIRMED_FRAUD | CLEARED`. Opening is automatic for HIGH/BLOCKER
claims. The second investigator must differ from the first (enforced in
`CaseService.decide`). Decisions drive the claim state machine.

## Data model
`expense_claims` → `rule_violations` (1:N), `fraud_cases` (1:1, high risk),
`duplicate_groups` (M:N evidence), `peer_baselines` (aggregates), `tips`,
`policy_rules`. Common kit tables: `local_users`, `audit_log`, `idempotency_record`.

## Scaling notes
- Single-node: H2 in-memory or PostgreSQL; scoring is CPU-light and synchronous.
- Horizontal: move claim ingestion behind the `ClaimSubmitted` event, scale scoring
  consumers; JGraphT clustering is windowed (30-day pool) so memory stays bounded.
