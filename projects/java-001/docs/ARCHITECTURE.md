# JAVA-001 — Architecture Overview

## 1. Architectural style and why

**Modular monolith** — one deployable, package-isolated bounded contexts. Chosen because:

- The domain is one strongly-consistent unit: decisions, grants, evidence and violations must be
  transactional with each other (an access decision and its evidence entry commit atomically).
- A microservice split would force distributed transactions across the evidence chain — the one
  thing that must never fork — for zero scaling benefit at this stage.
- The seams (messaging bus abstraction, idempotency service, policy cache, rate-limit store) are
  real boundaries that can be split later without rewriting the domain.

## 2. Module map (package = bounded context)

```
common.api        PageResponse, RFC 7807 exception hierarchy
common.web        correlation-id filter, global problem handler, rate limiting, idempotency
common.audit      business audit log service
common.masking    @Masked PII serializer
common.cache      cache manager selection (in-memory | Redis)
security          RBAC roles, JWT converter, security filter chain, local IdP (Argon2id+lockout)
identity          user directory (PII-masked views, least-privilege reads)
policy            versioned policies + typed rule engine (5 evaluators)
access            access requests → dual-control approvals → grants → revocation/expiry
events            access-event ingestion (rate-limited, idempotent, source-dedup)
evidence          hash-chained ledger (SHA-256, canonical JSON, advisory-lock serialization)
compliance        correlation engine + violation lifecycle + scheduler
recert            recertification campaigns and keep/revoke decisions
audit             signed export bundles (JSONL + HMAC) with outbox rescan
observability     typed Micrometer metrics
```

## 3. Key flows

### Access request → grant (dual control)
1. `POST /api/v1/access-requests` — requester records the request; evidence entry appended in the
   same transaction.
2. Two **distinct** approvers approve via `POST .../approve` with an `Idempotency-Key`.
   - SoD: requester cannot approve; one approver cannot decide twice.
3. Second approval → request APPROVED + grant ACTIVE (recert due = now + 90d) + evidence.

### Evidence ledger
- Every entry: `hash = SHA-256(prevHash || canonicalJson(payload))`, seq via BIGSERIAL.
- Single-writer: PostgreSQL `pg_advisory_xact_lock` (fallback JVM lock on H2).
- `GET /api/v1/evidence/verify` re-verifies the whole chain; actuator health indicator verifies the
  last 100 links on every probe. Sequence gaps and payload/link tampering both fail verification.

### Correlation engine
- Scheduled (cron) + on-demand (`POST /api/v1/compliance/run`).
- Builds a read-only snapshot (active grants, users, latest activity) and evaluates every rule of
  the active `ACCESS_GOVERNANCE` policy version: SOD_CONFLICT, CERT_EXPIRED, RECERT_OVERDUE,
  STANDING_PRIVILEGE, INACTIVE_ACCOUNT.
- Dedup: a new violation is created only if none is OPEN/ACKNOWLEDGED for
  (user, policy, rule, resource).
- Each violation links the evidence entry that proves its detection.

### Export
1. `POST /api/v1/audit/exports` (idempotent) → PENDING job + `ExportRequested` event.
2. Worker builds JSONL (evidence + grants + violations + events, user-scoped), HMAC-SHA256 signs it.
3. Download + server-side re-verification endpoints. Outbox rescan re-queues jobs stale > 2 min
   (crash recovery).

## 4. Concurrency control

| Concern | Mechanism |
|---|---|
| Evidence chain single writer | advisory lock (PG) / ReentrantLock (H2) |
| Idempotency-key races | unique constraint + REQUIRES_NEW claim; replay returns original resource |
| Duplicate access events | unique (source, external_id) + idempotency record |
| Grant state changes | `@Version` optimistic locking |
| Dual control | distinct-approver checks inside the same transaction as the decision |
| Correlation reruns | dedup against OPEN/ACKNOWLEDGED violations |

## 5. Failure handling

| Failure | Behaviour |
|---|---|
| Broker down (local profile) | resilience4j retry on publish; direct-bus profile unaffected |
| Export worker crashes mid-build | job PENDING; outbox rescan re-queues after 2 min |
| Duplicate HTTP retry | Idempotency-Key replay returns original resource |
| DB advisory lock unavailable | JVM lock fallback (single instance semantics, logged) |
| Tampered ledger | verify endpoint + health indicator go DOWN with broken-seq details |
| Violation event lost | correlation reruns re-detect (dedup keeps state consistent) |

## 6. Performance notes

- Virtual threads (`spring.threads.virtual.enabled`) — Tomcat + JDBC-scale IO.
- Hot rules cached (policy cache), invalidated on version activation.
- Rate limiting in front of auth + ingest endpoints (Redis-backed in local profile).
- Event ingestion is publish-then-persist (async) with idempotent persister.
- Evidence export streams a snapshot per job; the chain itself is append-only and index-free by
  design (seq PK).
