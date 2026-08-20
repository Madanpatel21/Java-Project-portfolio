# SECURITY — Fleet Maintenance Planning System (JAVA-009)

## Identity & access
Stateless JWT (HMAC-SHA256, Argon2, lockout after 5 failures) + `@PreAuthorize`
matrix over seven roles:
- `DRIVER` — submit own odometer readings; read vehicle status
- `FLEET_MANAGER` — vehicles, plans, forecast, open/reject work orders
- `MECHANIC` — start/complete work orders
- `PARTS_CLERK` — inventory, restock, resolve parts holds
- `COMPLIANCE_OFFICER` — record inspections, compliance report
- `AUDITOR` — read-only evidence across the fleet
- `ADMIN` — plan toggling, vehicle status, everything

## Tamper detection
- Readings below the last recorded odometer → HTTP 409 (possible rollback),
  audit-logged, never persisted.
- Readings exceeding the physically plausible range (max 1,500 km/day × 3, floor
  1,500 km) are accepted but flagged `SUSPICIOUS_JUMP` and raised as a
  `TamperFlagged` event; full history is available to managers/auditors.

## Inventory & compliance guards
- Reservations are deducted from available stock atomically inside the open-WO
  transaction; shortfalls produce PARTS_HOLD, never over-issue.
- FAILED inspection → COMPLIANCE_HOLD; only a PASS releases the vehicle.
- Every business transition is audit-logged with the acting principal.

## Abuse resistance
Rate limiting on auth and read endpoints; correlation IDs; validation-first
error handling (400 before 404/409); `JWT_SECRET` must be rotated in production.
