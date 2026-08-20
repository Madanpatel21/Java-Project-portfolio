# ADR-0001 — Pure interpreter engine

**Status:** accepted

## Context
Workflow semantics must be unit-testable without infrastructure and reusable across transports.

## Decision
The engine computes action plans (CREATE_TASK/SET_TIMER/RECORD_STEP/COMPLETE/UPDATE_VARS) with no
I/O; the service applies plans transactionally.

## Consequences
+ Deterministic tests; clear separation. − Two-phase design (plan/apply) must stay in sync.
