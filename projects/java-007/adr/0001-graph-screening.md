# ADR-0001 — Conflict screening via graph walk

**Status:** accepted

## Context
Conflicts hide in second-degree relationships, not just direct name matches.

## Decision
Screen by walking matter-party edges: adverse parties sharing a matter with an existing client are CONFLICT.

## Consequences
+ Catches indirect adversity. − Requires the parties graph to be maintained (documented).
