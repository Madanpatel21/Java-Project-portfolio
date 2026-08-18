# ADR-0001 — Modular monolith for the national registry

**Status:** accepted · **Date:** 2026-08-18

## Context
Registration decisions, ledger appends and person mutations must be one transaction; a national
registry is a single legal truth store.

## Decision
Single deployable with package-isolated bounded contexts; bus abstraction (RabbitMQ in local) for
external consumers.

## Consequences
+ Atomic legal records; simple operations. − Horizontal scaling limited to reads (documented).
