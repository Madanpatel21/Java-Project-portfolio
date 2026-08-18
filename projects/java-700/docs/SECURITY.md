# JAVA-700 — Security Model & Threat Model

## 1. Assets

| Asset | Sensitivity | Protection |
|---|---|---|
| Identity records (name, DOB, parents) | Critical | masked in listings; disclosed only via certificates/verification (legal purpose) |
| Life-event ledger | Critical | dual hash-chaining, append-only, single writer, health-verified |
| National IDs | High | checksummed generation; validity checked on verification |
| Certificates | High | content-hash tokens, revocation, masked-after-revocation |
| Local IdP credentials | High | Argon2id + progressive lockout + generic errors |

## 2. Authorization matrix

| Capability | REGISTRAR | SUPERVISOR | STATISTICIAN | VERIFIER_CLIENT | ADMIN |
|---|---|---|---|---|---|
| capture events | ✅ | ❌ | ❌ | ❌ | ❌ |
| approve/reject | ❌ | ✅ (not own) | ❌ | ❌ | ✅ (not own) |
| person search | ✅ | ✅ | ✅ | ❌ | ✅ |
| ledger verify | ❌ | ✅ | ✅ | ❌ | ✅ |
| issue certificates | ❌ | ✅ | ❌ | ❌ | ✅ |
| verify certificate/person | ❌ | ✅ | ❌ | ✅ | ✅ |
| revoke certificates | ❌ | ❌ | ❌ | ❌ | ✅ |
| dedup adjudication | ❌ | ❌ | ❌ | ❌ | ✅ |
| vital statistics | ❌ | ❌ | ✅ | ❌ | ✅ |

## 3. Threat model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Registrar self-approves a capture | method security (403) + in-domain SoD for dual-role users (409) — both tested |
| T2 | Duplicate identity (fraud) | dedup engine (Jaro-Winkler, blocking keys) + adjudication workflow |
| T3 | Post-mortem identity fraud | death approval → instant DECEASED propagation to verification/certificates/marriage |
| T4 | Ledger tampering by an insider | dual hash chains + sequence contiguity checks + health indicator |
| T5 | Forged national IDs | checksum validation on every verification |
| T6 | Replayed HTTP mutations | Idempotency-Key claim/complete/abandon |
| T7 | Certificate forgery/reuse | content-hash + pepper tokens; revocation instant |
| T8 | Credential attacks | Argon2id, lockout, generic errors, rate-limited auth, forged-token tests |
| T9 | Privilege escalation | RBAC matrix + method security (SecurityIT) |
| T10 | Injection | parameterized JPA only; malicious-input tests |
| T11 | PII leakage in listings/logs | @Masked serializer; correlation ids only in logs |

## 4. OWASP Top 10 (2021) mapping

A01 Broken Access Control → role matrix + SoD (tested) · A02 Cryptographic Failures → Argon2id,
pepper'd content hashes · A03 Injection → parameterized queries (tested) · A04 Insecure Design →
threat-model-first, four-eyes by construction · A05 Security Misconfiguration → security headers,
stateless bearer API (documented) · A06 Vulnerable Components → dependency-check profile in CI ·
A07 Auth Failures → lockout, TTLs, forged-token tests · A08 Software/Data Integrity → dual
hash-chained ledger · A09 Logging Failures → correlation ids everywhere · A10 SSRF → no outbound
fetches from user input.
