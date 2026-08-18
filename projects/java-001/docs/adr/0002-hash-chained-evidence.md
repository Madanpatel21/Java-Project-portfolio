# ADR-0002 — Hash-chained append-only evidence ledger

**Status:** accepted · **Date:** 2026-08-18

## Context
Auditors must be able to prove that the compliance record has not been altered, without trusting
any single operator or requiring a blockchain dependency.

## Decision
Append-only `evidence_entry` table; each row hashes the previous hash plus the canonical JSON of
its payload (SHA-256). A single writer is guaranteed by a PostgreSQL transaction-scoped advisory
lock (JVM-lock fallback on H2). Verification re-checks payload hashes, links, and sequence
contiguity; the actuator health indicator verifies the tail window on every probe.

## Consequences
+ Tampering is detectable, not just assertable; zero external dependencies.
+ Read-optimized: verification is O(n) but trivially parallelizable per window.
− No deletes/updates by design; retention policy must be external (documented in runbook).
