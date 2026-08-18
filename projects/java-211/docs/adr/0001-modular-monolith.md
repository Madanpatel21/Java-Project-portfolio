# ADR-0001 — Modular monolith for the stewardship loop

**Status:** accepted · **Date:** 2026-08-18

## Context
Intervention acceptance mutates therapy and records the decision atomically; audit and clinical
state must never diverge.

## Decision
Single deployable with package-isolated bounded contexts; messaging seam (bus abstraction) for
future extraction of analytics consumers.

## Consequences
+ Transactional integrity of clinical decisions; simpler local operations.
− Horizontal scaling limited to read paths (documented).
