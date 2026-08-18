# ADR-0002 — Dual hash-chained ledger (global + per-person)

**Status:** accepted · **Date:** 2026-08-18

## Context
Whole-registry tamper evidence alone cannot prove one person's history cheaply; per-person chains
alone cannot prove the registry as a whole.

## Decision
Every event appends one entry linking both chains (global SHA-256 chain + per-person chain over
the same canonical payload). Verification endpoints cover both; the health indicator checks the
global tail window.

## Consequences
+ Both audit granularities from one append; O(n) verifications. − Slightly wider rows (documented).
