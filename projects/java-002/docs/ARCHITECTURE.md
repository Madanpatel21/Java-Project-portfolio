# JAVA-002 — Architecture Overview

## Style and rationale
**Modular monolith.** Reconciliation decisions, exception state changes and posting records must be
one transaction — a microservice split would force distributed transactions across the very
records auditors rely on. The event bus abstraction (RabbitMQ in the `local` profile) is the seam
for downstream consumers (ERP posting, supplier portals).

## Modules
```
matching    FuzzyNormalizer (item codes, supplier names) + MatchingEngine (pure/deterministic)
domain      PO, PO lines, GR, GR lines, Invoice, Invoice lines, ToleranceRule, MatchException,
            GlPosting, BatchRun, OutboxRecord
service     ReconciliationService — ingest, match, exception lifecycle, posting batch, schedulers
api         REST controllers (RBAC + idempotency headers) + validation records
security    Roles, local IdP (Argon2id + lockout), JWT resource server
messaging   bus abstraction: DirectEventBus (dev) | RabbitEventBus + DLX (local)
common      correlation ids, RFC 7807 problems, idempotency, audit log, rate limiting, masking
```

## Key flows
- **Ingest** → idempotency claim → unique-check (invoice number × supplier) → three-way match in
  the same transaction.
- **Match** → supplier fuzzy match (Jaro-Winkler ≥ 0.92) → currency check → per-line item match →
  price variance vs PO price, quantity vs aggregated received quantities — each against the active
  tolerance rule. Warnings auto-resolve; CRITICAL findings create exceptions (deduped per
  invoice × type) and set the invoice to EXCEPTION.
- **Exception lifecycle** → OPEN → assign → waive (AP_MANAGER four-eyes, audited, idempotent) or
  reject (invoice REJECTED). When every exception is decided and none rejected → APPROVED.
- **Posting** → APPROVED invoices produce debit 2000-GRNI / credit 2100-AP postings, idempotent
  per invoice; invoice → POSTED; `INVOICE_POSTED` written to the transactional outbox and
  published over the bus.

## Concurrency and failure handling
| Concern | Mechanism |
|---|---|
| Duplicate ingestion / decisions | Idempotency-Key records (REQUIRES_NEW claim, unique constraint) |
| Duplicate invoice | unique (invoice_number, supplier_id) → 409 |
| Invoice state races | @Version optimistic locking |
| Match reprocessing | exception dedup per invoice × type; batch is rerunnable |
| Posting re-runs | per-invoice posting existence check |
| Broker down (local) | resilience4j retry; outbox retains events; dev profile unaffected |
| Batch failures | batch_runs records RUNNING/COMPLETED with counters; nightly rerun heals |
