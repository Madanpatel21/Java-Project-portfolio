# ADR-0002 — Clause-level clearance redaction

**Status:** accepted

## Context
Blanket document access over-exposes pricing and exit terms; blanket hiding blocks operations.

## Decision
Clauses carry sensitivity (0..4); roles carry clearance; reads redact insufficient-clearance clauses.

## Consequences
+ Least-disclosure without workflow friction. − Sensitivity metadata must be maintained per clause (documented).
