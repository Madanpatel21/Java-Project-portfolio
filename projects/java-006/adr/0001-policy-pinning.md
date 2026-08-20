# ADR-0001 — Requests pin the policy version

**Status:** accepted

## Context
Decisions must be auditable against the rules that were in force at decision time.

## Decision
Every request stores the ACTIVE policy version id at creation; version changes never touch history.

## Consequences
+ Reproducible governance. − Policy fixes apply only to new requests (accepted).
