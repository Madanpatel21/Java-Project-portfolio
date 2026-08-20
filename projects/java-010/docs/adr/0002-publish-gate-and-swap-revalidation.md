# ADR-0002: Publish gate + swap re-validation

**Status:** Accepted · **Date:** 2026-08-20

## Context
An unstaffed slot published to employees is an operational incident; an approved
swap based on stale skills is a safety incident.

## Decision
- Roster publish requires an optimized score AND zero unassigned slots (409 otherwise).
- Swap approvals re-run skill, availability and double-booking checks at decision
  time, not just at request time; mismatches fail the decision.

## Consequences
- Managers cannot ship incomplete rosters by accident.
- Slightly higher latency on swap decisions (a few reads) in exchange for safety.
