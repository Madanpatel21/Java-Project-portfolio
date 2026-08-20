# JAVA-004 — Threat Model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Unauthorized content disclosure | classification x role-clearance on download (tested) |
| T2 | Destruction without authority | retention scan + proof; REVIEW classes need humans |
| T3 | Destruction during litigation | legal-hold protection (multi-hold correct release) |
| T4 | Tampered destruction records | append-only proofs with content hashes |
| T5 | Replayed uploads | Idempotency-Key |
| T6 | Malicious content uploads | size caps (Tomcat), type-narrow extraction |
| T7 | Credential attacks | Argon2id, lockout, generic errors, rate limit |
| T8 | Privilege escalation | RBAC matrix + method security |
| T9 | Injection | parameterized JPA; search parameterized; tests |

OWASP: A01 RBAC+clearance · A02 Argon2id · A03 parameterized · A04 threat-model-first ·
A05 headers · A07 lockout · A08 append-only proofs · A09 correlation ids.
