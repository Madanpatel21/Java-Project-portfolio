# JAVA-003 — Architecture Overview

## Style and rationale
**Modular monolith.** Version creation, activation decisions and obligation state changes must be
one transaction — legal records cannot tolerate partial writes. The bus abstraction (RabbitMQ in
`local`) is the seam for downstream consumers (CLM sync, ERP, notification services).

## Modules
```
matching   ContractDiff — clause-level ADDED/REMOVED/MODIFIED over frozen versions
domain     Contract, ContractVersion (immutable), Obligation (SLA state machine),
           ObligationEvent (append-only history), Approval (governance decisions)
service    ContractService — versioning, four-eyes activation, clause redaction,
           obligation lifecycle, SLA scan, recurrence
api        REST controllers (RBAC + idempotency headers)
security   Roles + clearance model, local IdP (Argon2id + lockout), JWT resource server
messaging  bus abstraction: DirectEventBus (dev) | RabbitEventBus + DLX (local)
common     correlation ids, RFC 7807, idempotency, audit log, rate limiting
```

## Key flows
- **Versioning** — content JSON (clauses with number/title/text/sensitivity) is frozen per version;
  diffs are computed from the frozen snapshots.
- **Activation** — DRAFT → ACTIVE only after two distinct governance roles (LEGAL + CONTRACT_MANAGER)
  approve; rejections are recorded; terminated contracts are immutable.
- **Clause reads** — role clearance (0..4) vs clause sensitivity; insufficient clearance → redacted.
- **Obligation SLA** — OPEN → (window) NOTIFIED → ACKNOWLEDGED → COMPLETED; past due → OVERDUE;
  LEGAL/ADMIN waive with reason; `repeatIntervalDays` spawns the next instance on completion.
- **Scan** — scheduled daily + on-demand; idempotent state transitions only.

## Concurrency and failure handling
| Concern | Mechanism |
|---|---|
| Duplicate mutations | Idempotency-Key records (REQUIRES_NEW claim) |
| Double approvals | per-role dedup inside the activation transaction |
| Obligation races | @Version optimistic locking; status checks in-transaction |
| Scan re-runs | idempotent (only legal transitions apply) |
| Broker down | resilience4j retry; dev profile unaffected |
