# JAVA-700 — Architecture Overview

## 1. Style and rationale

**Modular monolith** — one deployable with package-isolated bounded contexts. A national registry
is a single strongly-consistent truth store: a registration decision, its ledger append and the
person mutation must be one transaction. The bus abstraction (RabbitMQ in local) is the seam for
external consumers (statistics services, other agencies) without splitting the core.

## 2. Module map

```
registry        Person lifetime records, offices, NationalIdGenerator (ISO 7064-style checksum)
registration    four-eyes life events: BIRTH / MARRIAGE / DEATH / CORRECTION
ledger          dual hash-chained ledger: global chain + per-person chain, advisory-lock writer
certificates    content-hash tokens, issuance rules (deceased-aware), verification, revocation
dedup           Jaro-Winkler scoring over blocking keys + adjudication workflow
statistics      vital statistics: births/deaths/marriages/natural-increase per region
verification    third-party identity verification (deceased status propagates instantly)
messaging       bus abstraction (Direct | RabbitMQ+DLX), typed handlers
common          correlation ids, RFC 7807, idempotency, masking, audit log, rate limiting
security        RBAC matrix, local IdP (Argon2id + lockout), JWT resource server
```

## 3. Key flows

### Birth → person (four-eyes)
Registrar captures (PENDING, idempotent) → supervisor approves (SoD: not the capturing registrar —
403 at method security for pure registrars, 409 in-domain for dual-role users) → Person created
with checksummed national ID + BIRTH appended to BOTH chains + `PersonRegistered` event → dedup
scan against the same blocking block.

### Death → deceased propagation
Death approved → person DECEASED + DEATH ledger entry + `DeathRegistered` event. Verification
endpoint, certificate issuance rules and marriage validation all consult live status — a death
immediately ends that identity's legal use.

### Certificate lifecycle
Issuance computes a content hash (identity fields + type + pepper) and a token; verification by
token discloses identity only while VALID; revocation appends CERTIFICATE_REVOKED to the person's
chain and flips verification to masked/invalid.

### Dual-chain integrity
Every event stores `(prevGlobalHash, globalHash, chainSeq, prevChainHash, chainHash)` over the
same canonical payload. `verifyGlobal()` re-walks the whole registry; `verifyPerson()` re-walks
one life. Health indicator checks the global tail window on every probe.

## 4. Concurrency and failure handling

| Concern | Mechanism |
|---|---|
| Ledger single writer | PostgreSQL advisory lock (JVM-lock fallback on H2) |
| Duplicate captures/decisions | Idempotency-Key records (REQUIRES_NEW claim) |
| Concurrent decisions | status checks inside the transaction + unique constraints |
| Dedup idempotency | (personA, personB, OPEN) existence check before insert |
| Broker down (local) | resilience4j retry on publish; dev profile unaffected |
| Tampered ledger | verify endpoints + health indicator report the broken sequence |
