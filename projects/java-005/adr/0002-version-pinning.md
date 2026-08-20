# ADR-0002 — Instances pin their definition version

**Status:** accepted

## Context
Redefining a workflow mid-flight corrupts process history.

## Decision
New versions deprecate the previous; instances execute their own snapshot forever.

## Consequences
+ Reproducible process history. − Long-running instances keep old semantics (accepted).
