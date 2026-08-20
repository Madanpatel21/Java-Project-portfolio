# SECURITY — Capacity & Shift Rostering Optimizer (JAVA-010)

## Identity & access
Stateless JWT (HMAC-SHA256), Argon2 hashing, lockout after 5 failures.
`@PreAuthorize` matrix:
- `EMPLOYEE` — own schedule, own availability, own swap requests
- `MANAGER` — rosters, optimization, publish, swap decisions
- `ADMIN` — employees, rules, everything
- `AUDITOR` — read-only: rosters, explanations, stats

## Domain guards
- **Publish gate**: rosters with unassigned slots or without a score refuse to
  publish (409) — an uncovered shift can never reach employees.
- **Swap re-validation**: approvals re-check target skill, availability and
  double-booking at decision time; mismatches fail fast at request time.
- **Score integrity**: the persisted breakdown is re-derived on demand from the
  same constraint set — tampered rosters fail to explain.

## Privacy
Self-service endpoints resolve the caller's workforce profile via a `user_id`
link; employees can never read other employees' schedules or swap requests.

## Abuse resistance
Rate limiting, correlation IDs, validation-first payload handling, full audit
trail (roster lifecycle + swap decisions). Rotate `JWT_SECRET` in production.
