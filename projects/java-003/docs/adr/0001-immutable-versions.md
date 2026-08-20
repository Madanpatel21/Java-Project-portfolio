# ADR-0001 — Contract versions are immutable

**Status:** accepted

## Context
Legal review requires proving what a counterparty agreed to at any point in time.

## Decision
Every version is frozen at creation; corrections flow as new versions; diffs are computed between frozen snapshots.

## Consequences
+ Audit-grade legal history. − No in-place edits (accepted: that is the point).
