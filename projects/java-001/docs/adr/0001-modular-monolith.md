# ADR-0001 — Modular monolith over microservices

**Status:** accepted · **Date:** 2026-08-18

## Context
The compliance domain requires transactional coupling between access decisions and evidence
entries. A distributed split would introduce distributed-transaction complexity without a scaling
driver.

## Decision
Single deployable with package-isolated bounded contexts; messaging seam behind a bus abstraction
(RabbitMQ in the local profile, in-process dispatch in dev) so contexts can be extracted later.

## Consequences
+ Atomicity between domain writes and evidence appends; simpler local operations.
+ Seams (bus, cache, rate-limit store, idempotency) are extractable.
− Horizontal scaling limited to read paths and the broker-backed async paths (documented).
