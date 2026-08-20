# ADR-0001: Synchronous explainable scoring at ingestion

**Status:** Accepted · **Date:** 2026-08-20

## Context
Claims need a risk signal before any approval decision. Async scoring would create a
window where managers could approve unscored claims.

## Decision
Scoring runs synchronously inside the claim submission transaction. Every point is
persisted (violations, groups, reasons JSON) before the API returns. The
`ClaimScored` event is still published for downstream consumers.

## Consequences
- No unscored-claim window; strongest possible policy guard.
- Submission latency includes scoring (measured p95 ≈ 100 ms on dev hardware).
- Horizontal scaling moves ingestion behind events later without API changes.
