# ADR-0001 — Modular monolith for P2P reconciliation

**Status:** accepted

## Context
Match decisions, exception state and posting records must be one transaction for audit integrity.

## Decision
Single deployable with package-isolated contexts; bus abstraction (RabbitMQ in local) for downstream consumers.

## Consequences
+ Atomic audit trails; simple local operations. − Horizontal scaling limited to reads (documented).
