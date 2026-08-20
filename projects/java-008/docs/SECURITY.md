# SECURITY — Expense Fraud & Policy Analytics Engine (JAVA-008)

## Identity & access
- **Stateless JWT** (HMAC-SHA256, 30-min TTL) issued by the local IdP after Argon2
  verification; lockout after 5 failures for 15 minutes.
- **RBAC matrix** enforced by `@PreAuthorize`:
  - `EMPLOYEE` — submit/view own claims (masked identities)
  - `MANAGER` — approve/reject LOW & MEDIUM claims only
  - `FRAUD_INVESTIGATOR` — queue, evidence, first/second-eye case decisions, tips
  - `AUDITOR` — full unmasked evidence, stats, baselines (read-only)
  - `ADMIN` — policy rule toggling, baseline recompute

## Anti-fraud domain controls
- **Policy guard**: `ClaimService.decide` refuses manager action on any claim with
  score ≥ 65 or a BLOCKER violation (HTTP 409) — only the case workflow can resolve it.
- **Four-eyes**: the deciding investigator must differ from the reviewer; violations
  return 409 and are audit-logged.
- **Explainability**: risk scores are never opaque — every point maps to a persisted
  reason (rule code, weight, message, observed vs expected).

## Privacy
- **PII masking**: `employeeName` is masked (`R•••• K•••••`) for non-privileged readers;
  investigators/auditors see full evidence.
- **Whistleblower channel**: `POST /api/v1/tips` is anonymous (permitAll), captures no
  submitter identity, and returns only a tip number for follow-up.

## Abuse resistance
- Idempotent claim ingestion (`Idempotency-Key`); rate limiting on auth and read
  endpoints; correlation IDs on every request; full audit log of business transitions.
- Input validation-first: malformed payloads fail before any entity is loaded
  (400 before 404/409).

## Secrets
`JWT_SECRET` (≥ 32 bytes) and the demo password must be rotated via environment in
any non-dev deployment — see `CONFIGURATION.md`.
