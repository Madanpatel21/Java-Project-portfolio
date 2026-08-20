# JAVA-002 — Threat Model (STRIDE summary)

| # | Threat | Controls |
|---|---|---|
| T1 | Duplicate invoice paid twice | unique (invoice_number, supplier_id); idempotent ingest; duplicate 409 |
| T2 | Clerk waives a critical exception alone | waive endpoint requires AP_MANAGER (four-eyes); audited waiver |
| T3 | Over-billing slips through | quantity vs received comparison beyond tolerance → CRITICAL OVER_BILLING |
| T4 | Replayed HTTP mutations | Idempotency-Key claim/complete/abandon |
| T5 | Price inflation vs PO | PRICE_VARIANCE rule with tolerance %; beyond tolerance → CRITICAL |
| T6 | Currency/supplier fraud | supplier fuzzy match + currency equality checks |
| T7 | Credential attacks | Argon2id, progressive lockout, generic errors, auth rate limit, forged-token tests |
| T8 | Privilege escalation | RBAC matrix + method security (SecurityIT) |
| T9 | Injection | parameterized JPA only; malicious-input tests |
| T10 | Tampered GL postings | postings are append-only after creation; audit trail per batch |

OWASP mapping: A01 (broken access control) four-eyes matrix · A02 (crypto) Argon2id · A03
(injection) parameterized · A04 (insecure design) threat-model-first · A05 (misconfig) security
headers · A07 (auth failures) lockout+TTL · A08 (integrity) outbox+audit · A09 (logging)
correlation ids on every line.
