# ADR-0002 — Per-step dual control with distinct approvers

**Status:** accepted

## Context
A single approver must never be able to satisfy a multi-approver step alone.

## Decision
Each step declares approversRequired; the engine counts DISTINCT approvers per step and blocks repeats.

## Consequences
+ Fraud-resistant chains. − Slightly more coordination per step (accepted).
