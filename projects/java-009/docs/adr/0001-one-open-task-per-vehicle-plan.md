# ADR-0001: Exactly one open task per vehicle + plan

**Status:** Accepted · **Date:** 2026-08-20

## Context
Repeated forecast runs (Quartz every 30 minutes) must not duplicate tasks.

## Decision
The forecast upserts: an open (SCHEDULED/DUE/OVERDUE) task for a vehicle+plan is
updated in place (status + due point); a new task is created only when none exists.
Task identity is the (vehicleId, planId) pair among open tasks.

## Consequences
- Forecast runs are idempotent and safe to run concurrently.
- Task history = completed tasks only, which is exactly the service ledger we want.
