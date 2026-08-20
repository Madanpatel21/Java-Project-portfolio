# JAVA-003 — Threat Model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Unauthorized clause disclosure | sensitivity × role-clearance redaction (tested: FINANCE vs AUDITOR) |
| T2 | Contract activated without governance | four-eyes (LEGAL + CONTRACT_MANAGER), per-role dedup |
| T3 | Obligation silently waived | waiver restricted to LEGAL/ADMIN, audited with reason |
| T4 | Replayed mutations | Idempotency-Key claim/complete/abandon |
| T5 | Missed deadlines | SLA windows + overdue escalation + scheduled scan |
| T6 | Tampered history | append-only obligation events; immutable versions |
| T7 | Credential attacks | Argon2id, progressive lockout, generic errors, rate limit |
| T8 | Privilege escalation | RBAC matrix + method security (SecurityIT) |
| T9 | Injection | parameterized JPA; validation-first input handling; malicious-input tests |

OWASP mapping: A01 four-eyes matrix · A02 Argon2id · A03 parameterized + validation-first ·
A04 threat-model-first · A05 security headers · A07 lockout+TTL · A08 immutable versions +
audit · A09 correlation ids.
