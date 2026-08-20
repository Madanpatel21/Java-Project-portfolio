# ADR-0002 — Append-only disposition proofs

**Status:** accepted

## Context
Auditors must be able to verify what was destroyed, when and by whom.

## Decision
Every disposition appends an immutable proof (document id, sha256, class, executor, time, action).

## Consequences
+ Re-verifiable destruction. − Proof records are retained permanently (accepted).
