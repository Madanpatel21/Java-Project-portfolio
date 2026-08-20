# JAVA-007 — Architecture Overview

**Modular monolith.** Screening results, deadline state and wall enforcement must be one
transactional unit. Bus abstraction (RabbitMQ in `local`) is the seam for downstream consumers.

## Modules
```
screening  ConflictScreener (pure graph walk + fuzzy matching), NameNormalizer
domain     Party, Matter, MatterParty (graph edges), ConflictCheck, DeadlineRule,
           MatterDeadline, EthicalWall
service    LegalService — screening, deadline computation/missed scan, walls; DeadlineScheduler
api        REST controllers (RBAC + idempotency)
```

## Key flows
- **Screening** → normalize names → match adverse names (exact, then fuzzy) → walk matters for
  direct adversity → CONFLICT/POTENTIAL/CLEAR + recorded check.
- **Deadlines** → jurisdiction rules (signed day offsets) applied to a trigger date; past-due
  OPEN deadlines flip to MISSED on a schedule.
- **Walls** → roles listed on a matter are blocked at read time (409).

## Concurrency and failure handling
| Concern | Mechanism |
|---|---|
| Duplicate parties/matters/checks | Idempotency-Key + unique constraints |
| Deadline double-completion | status guard |
| Scan re-runs | idempotent status transitions |
| Broker down | resilience4j retry; dev profile unaffected |
