# JAVA-211 — Security Model & Threat Model

## 1. Assets

| Asset | Sensitivity | Protection |
|---|---|---|
| Patient PHI (name, MRN, DOB) | High | masked at API boundary (`@Masked`), full data only in DB |
| Clinical decisions (interventions, approvals) | High | RBAC + state machines + full audit log with correlation ids |
| Restricted-drug authorizations | High | ID-physician-only, time-boxed, expiry enforcement |
| Susceptibility/microbiology data | Medium | microbiologist/ID write; infection-control read |
| Local IdP passwords | High | Argon2id + progressive lockout + generic errors |

## 2. Authorization matrix

| Capability | PHARMACIST | PRESCRIBER | ID_PHYSICIAN | MICROBIOLOGIST | INFECTION_CONTROL | ADMIN |
|---|---|---|---|---|---|---|
| order drugs | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ |
| restricted approval | ❌ | ❌ | ✅ | ❌ | ❌ | ✅ |
| propose intervention | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| accept/reject intervention | ❌ | ✅ | ✅ | ❌ | ❌ | ✅ |
| evaluate / reviews | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ |
| cultures | read | read | ✅ | ✅ | read | ✅ |
| antibiogram | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ |
| metrics | ✅ | ❌ | ✅ | ❌ | ✅ | ✅ |
| guideline versioning | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

## 3. Threat model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Prescriber self-authorizes restricted drugs | ID_PHYSICIAN role gate; audited decisions |
| T2 | Replayed order/intervention HTTP retry | Idempotency-Key (claim/complete/abandon) |
| T3 | Duplicate culture reporting | report is one-way final; 409 on re-report |
| T4 | PHI leakage via APIs/logs | @Masked serializer; no PHI in log lines; correlation ids only |
| T5 | Credential attacks | Argon2id, 5-attempt lockout, generic 404s, auth rate limit, forged-token tests |
| T6 | Privilege escalation | Method security per endpoint (SecurityIT matrix) |
| T7 | SQL injection | parameterized JPA only; malicious-input tests |
| T8 | Stale guideline application | versioned guidelines; every version activation audited |
| T9 | Missed critical alerts | culture-driven tasks dedupe but never suppress; audit entries; Prometheus counters |
| T10 | Antibiotogram misuse | minimum-isolate gate; first-isolate dedup rules |

## 4. OWASP Top 10 (2021) mapping

A01 Broken Access Control → RBAC matrix + method security (tested) · A02 Cryptographic Failures →
Argon2id, no secrets in logs · A03 Injection → parameterized queries (tested) · A04 Insecure Design
→ threat-model-first, one-way culture reporting · A05 Security Misconfiguration → security headers,
stateless bearer API (CSRF disabled, documented) · A06 Vulnerable Components → dependency-check
profile in CI · A07 Auth Failures → lockout, short TTL, forged-token tests · A08 Software/Data
Integrity → versioned guidelines, audit trails · A09 Logging Failures → correlation ids on every
line · A10 SSRF → no outbound fetches from user input.
