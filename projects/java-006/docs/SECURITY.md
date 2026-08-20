# JAVA-006 — Threat Model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Requester self-approves | SoD in-domain (409/403) — tested |
| T2 | Single approver pushes a multi-control step | per-step distinct-approver count |
| T3 | Wrong role approves | per-step role gating |
| T4 | Policy change rewrites history | requests pin the policy version id |
| T5 | Replayed decisions | Idempotency-Key + decision guards |
| T6 | Tampered evidence | append-only approval_decisions |
| T7 | Credential attacks | Argon2id, lockout, generic errors, rate limit |
| T8 | Injection | parameterized JPA; tests |

OWASP: A01 RBAC+SoD · A02 Argon2id · A03 parameterized · A04 threat-model-first · A05 headers ·
A07 lockout · A08 append-only evidence · A09 correlation ids.
