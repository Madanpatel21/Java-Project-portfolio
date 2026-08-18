# JAVA-001 — Security Model & Threat Model

## 1. Assets and trust boundaries

| Asset | Sensitivity | Protection |
|---|---|---|
| Evidence ledger (payloads, hash chain) | Critical (regulatory) | append-only, hash-chained, single-writer, tamper detection + health signal |
| Access grants & decisions | Critical | RBAC + SoD + dual control + optimistic locking + full audit |
| PII (emails, usernames, org units) | High | masked at API boundary, least-privilege reads (self-only for employees) |
| Export bundles | High | HMAC-SHA256 signature, auditor-only endpoints, re-verification API |
| Local IdP passwords | High | Argon2id + progressive lockout + generic error messages |
| JWTs | High | short TTL (30m), HS256 dev / RS256 via Keycloak in local profile |

## 2. Authentication & authorization

- **Authentication**: stateless bearer JWTs. Dev profile: built-in IdP (`/api/v1/auth/token`,
  Argon2id, lockout after 5 failures for 15 min, generic 404 messages to avoid user enumeration).
  Local profile: Keycloak OIDC (`realm java700`, RS256, issuer validation).
- **Authorization matrix**:

| Endpoint group | EMPLOYEE | ACCESS_MANAGER | COMPLIANCE_OFFICER | COMPLIANCE_ADMIN | AUDITOR | INTEGRATION |
|---|---|---|---|---|---|---|
| users read | self only | ✅ | ✅ | ✅ | ✅ | ❌ |
| access request | self only | ✅ | – | ✅ | ❌ | ❌ |
| approve/reject | ❌ | ✅ (not own) | ❌ | ✅ (not own) | ❌ | ❌ |
| revoke grants | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ |
| violations lifecycle | ❌ | ❌ | ✅ | ✅ | read-only | ❌ |
| correlation run | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| policy versioning | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| recert campaigns | ❌ | decide | read | ✅ | read | ❌ |
| exports | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ |
| event ingest | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |

- **Domain-level checks** (not just endpoint-level): SoD (requester ≠ approver, one decision per
  approver, dual control), employee self-only requests, least-privilege user reads.

## 3. Threat model (STRIDE summary)

| # | Threat | Likelihood | Impact | Controls |
|---|---|---|---|---|
| T1 | Attacker replays an ingest/approve request | High | Medium | Idempotency-Key records (REQUIRES_NEW claim, unique constraint, replay returns original resource) |
| T2 | Requester self-approves or single approver pushes through | Medium | High | SoD + dual control enforced in-domain + evidence of every decision |
| T3 | Insider mutates evidence entries in the DB | Medium | Critical | Hash chaining detects any payload/link/sequence mutation; verify endpoint + health indicator; append-only discipline (no update/delete paths) |
| T4 | Stolen/brute-forced credentials | Medium | High | Argon2id, progressive lockout, generic errors, short JWT TTL, security event logs |
| T5 | Forged/expired tokens | Medium | High | JWT signature + exp/iss validation (resource server), forged-token tests |
| T6 | Privilege escalation via endpoint | Medium | High | Method security + RBAC matrix; SecurityIT covers escalation attempts per role |
| T7 | SQL injection | Medium | High | JPA parameterized queries only; malicious-input tests |
| T8 | PII leakage via APIs | Medium | Medium | @Masked serializer, self-only reads, no PII in logs |
| T9 | Export bundle tampered in transit/at rest | Low | High | HMAC-SHA256 over bundle bytes; server-side re-verification endpoint |
| T10 | Rate-limit bypass / abuse of auth + ingest | Medium | Medium | Fixed-window limiter (in-memory dev / Redis local) on auth + ingest paths |
| T11 | Duplicate event injection (source replay) | Medium | Medium | unique (source, external_id) + idempotency; duplicate 409 |
| T12 | XSS via Swagger/error content | Low | Low | CSP `default-src 'none'`, frame-deny, JSON problem responses |
| T13 | Downtime hides compliance failures | Medium | High | Liveness/readiness probes + evidence-chain health indicator; outbox rescan for exports |

## 4. OWASP Top 10 (2021) mapping

A01 Broken Access Control → RBAC matrix + SoD + self-only rules (tested) ·
A02 Cryptographic Failures → Argon2id, AES-free design (hash-chain integrity instead), HMAC exports ·
A03 Injection → parameterized queries only (tested) ·
A04 Insecure Design → threat-model-first, SoD in domain not just UI ·
A05 Security Misconfiguration → security headers, CSRF disabled only for stateless bearer API (documented), short TTLs ·
A06 Vulnerable Components → OWASP dependency-check profile in CI ·
A07 Auth Failures → lockout, generic errors, token expiry, forged-token tests ·
A08 Software/Data Integrity → evidence hash chain + export HMAC ·
A09 Logging Failures → correlation-id on every log line, security event logs, business audit table ·
A10 SSRF → no outbound fetches from user input (external systems are local-only)
