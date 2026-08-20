# ADR-0002 — Ethical walls enforced at read time

**Status:** accepted

## Context
Screening finds conflicts after engagement; access must be revocable instantly.

## Decision
Walls are role-based matter exclusions evaluated on every read (409).

## Consequences
+ Instant, audited exclusion. − Slightly more per-read evaluation (accepted).
