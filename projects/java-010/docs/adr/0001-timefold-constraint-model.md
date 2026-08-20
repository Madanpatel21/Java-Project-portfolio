# ADR-0001: Timefold constraint model for rostering

**Status:** Accepted · **Date:** 2026-08-20

## Context
Rostering is a classic constraint-satisfaction problem; hand-rolled heuristics
would grow unbounded as labor rules change.

## Decision
Model each demand headcount unit as a PlannedShift planning entity with Employee
as the planning variable. Encode 7 hard constraints (skill, availability, one
shift/day, weekly hours, night→morning rest, max 6 days, night cap) and 2 soft
objectives (coverage, squared-hours fairness) in a ConstraintProvider.

## Consequences
- Feasibility == labor-law compliance; the solver proves or refutes it.
- Scores are explainable per constraint match (auditor evidence).
- Rule changes are code changes + catalogue rows — trade-off accepted for speed.
