# JAVA-006 — Architecture Overview

**Modular monolith.** Policy versions, chain advancement and decision records must be one
transaction — audit integrity requires it. Bus abstraction (RabbitMQ in `local`) is the seam
for downstream consumers.

## Modules
```
domain     Policy (one ACTIVE version), PolicyVersion (immutable), ApprovalChain (JSON steps),
           ApprovalRequest (pinned to policy version), ApprovalDecision (evidence)
service    ApprovalService — chain advance with per-step dual control, SoD, escalation; ChainParser
api        REST controllers (RBAC + idempotency)
```

## Key flows
- **Policy activation** → new version supersedes the previous; requests always capture the ACTIVE version id.
- **Request** → bound to chain + policy version; due date sets the SLA.
- **Decision** → role-gated per step; distinct-approver dual control; step satisfied → advance; final step → APPROVED; any REJECT → REJECTED.
- **Escalation** → stale PENDING requests get a renewed SLA + event.

## Concurrency and failure handling
| Concern | Mechanism |
|---|---|
| Duplicate decisions | Idempotency-Key + distinct-approver guard |
| Step races | status/step checks inside the transaction; @Version optimistic locking |
| Double activation | per-policy version sequencing in one transaction |
| Broker down | resilience4j retry; dev profile unaffected |
