# JAVA-004 — Architecture Overview

**Modular monolith.** Disposition decisions and their proofs must be one transaction — audit
integrity requires it. The bus abstraction (RabbitMQ in `local`) is the seam for downstream
consumers (WORM archival, eDiscovery exports).

## Modules
```
domain     Document (classification x retention x hold state), LegalHold, HoldEntry,
           RetentionRule (R0-R7), DispositionProof (append-only)
service    DocumentService — upload/quarantine/classify/holds/retention-scan,
           TextExtractor (txt/md/csv), ContentHasher (sha256)
api        REST controllers + DevTimeController (Profile("dev") only)
security   Roles + classification-clearance model, local IdP, JWT
messaging  bus abstraction (Direct | RabbitMQ + DLX)
common     correlation ids, RFC 7807, idempotency, audit log, rate limiting
```

## Key flows
- **Upload** → sha256 fingerprint + extracted text → QUARANTINED → classify → ACTIVE.
- **Legal hold** → hold + entries; document legal_hold=true; release recomputes across holds.
- **Retention scan** → ACTIVE docs past their class retention: hold → protect; REVIEW → human
  queue; else dispose → DispositionProof appended + content deleted.
- **Download** → clearance >= classification ordinal, else 409.

## Failure handling
| Concern | Mechanism |
|---|---|
| Duplicate uploads/classifications | Idempotency-Key |
| Concurrent hold/dispose races | @Version optimistic locking |
| Content I/O failures | 503 with correlation id; metadata remains consistent |
| Scan re-runs | idempotent (status-transition guarded) |
