# JAVA-005 — Threat Model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Unauthorized process modification | definition creation/versioning is PROCESS_ADMIN/ADMIN only |
| T2 | Approver self-serving | task completion restricted to APPROVER/ADMIN roles; results audited |
| T3 | Replayed task completions | Idempotency-Key + status guard |
| T4 | Malicious definition injection | model validation at creation (400 on malformed) |
| T5 | Expression injection in gateways | tiny evaluator over typed variables only — no reflection/scripting |
| T6 | Credential attacks | Argon2id, lockout, generic errors, rate limit |
| T7 | Privilege escalation | RBAC matrix + method security (SecurityIT) |
| T8 | SQL injection | parameterized JPA; tests |
| T9 | Tampered execution trace | workflow_steps append-only |

OWASP: A01 RBAC matrix · A02 Argon2id · A03 parameterized + no scripting in expressions ·
A04 threat-model-first · A05 headers · A07 lockout · A08 append-only trace · A09 correlation ids.
