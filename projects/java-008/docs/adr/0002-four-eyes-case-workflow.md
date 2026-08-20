# ADR-0002: Four-eyes case workflow for high-risk claims

**Status:** Accepted · **Date:** 2026-08-20

## Context
A single investigator deciding fraud is a separation-of-duties failure; managers
approving their own team's high-risk claims is a classic fraud enabler.

## Decision
HIGH (≥ 65) or BLOCKER claims are locked from manager approval and opened as fraud
cases. Cases require two distinct investigators: one records a recommendation
(OPEN → REVIEWED), a different one makes the binding decision
(CONFIRM_FRAUD / CLEAR). Identity equality is enforced server-side.

## Consequences
- Collusion is mitigated but not eliminated (two-person control).
- Manager flow stays fast for the ~95 % low/medium-risk volume.
- The claim state machine gains UNDER_REVIEW and CONFIRMED_FRAUD states that
  downstream payroll systems must honour.
