# ADR-0002 — Four-eyes waivers for critical match exceptions

**Status:** accepted

## Context
Waiving a price/quantity exception approves paying more than the PO — a classic fraud vector.

## Decision
Waive/reject endpoints require AP_MANAGER (or ADMIN); every waiver is audited with note + actor + time.

## Consequences
+ Fraud-resistant exception handling. − Slightly slower exception throughput (acceptable: human-in-the-loop by design).
