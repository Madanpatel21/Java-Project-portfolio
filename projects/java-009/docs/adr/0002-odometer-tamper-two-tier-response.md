# ADR-0002: Two-tier odometer tamper response

**Status:** Accepted · **Date:** 2026-08-20

## Context
Readings arrive from drivers and shops; tampering ranges from honest errors to fraud.

## Decision
- Reading < last recorded → hard reject (HTTP 409, audit-logged, not persisted).
- Reading > last recorded + physically plausible maximum (1,500 km/day × 3, floor
  1,500 km) → accepted but flagged SUSPICIOUS_JUMP and published as an event.

## Consequences
- The meter can never move backwards, protecting the scheduling engine's anchors.
- Suspicious data is quarantined for review instead of blocking operations.
- A full history ledger (odometer_entries) supports audits and dispute resolution.
