# ADR-0002 — Culture reports are one-way final

**Status:** accepted · **Date:** 2026-08-18

## Context
Retracting susceptibility data after it has driven clinical decisions creates irreproducible
alert histories.

## Decision
`report()` is final; re-reporting returns 409. Corrections flow as a new culture with amended
isolates, preserving the original decision trail.

## Consequences
+ Reproducible audit chains; no silent rewrites of clinical evidence.
− Operational friction for data-entry errors (mitigated by pre-report editing).
