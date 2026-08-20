# JAVA-007 — Threat Model (STRIDE summary)
| # | Threat | Controls |
|---|---|---|
| T1 | Firm takes a conflicting matter | graph-walk screening; CONFLICT blocks; records kept |
| T2 | Missed court deadline | rule-driven computation + MISSED escalation + audit |
| T3 | Walled team reads a matter | ethical walls enforced at read time (409) |
| T4 | Replayed submissions | Idempotency-Key on parties/matters/screens/deadlines |
| T5 | Fuzzy-match gaming | POTENTIAL findings force analyst review |
| T6 | Credential attacks | Argon2id, lockout, generic errors, rate limit |
| T7 | Injection | parameterized JPA; tests |

OWASP: A01 RBAC+walls · A02 Argon2id · A03 parameterized · A04 threat-model-first ·
A05 headers · A07 lockout · A08 recorded checks · A09 correlation ids.
