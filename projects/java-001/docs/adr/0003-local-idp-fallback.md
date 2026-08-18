# ADR-0003 — Built-in identity provider for dev, Keycloak for the local production profile

**Status:** accepted · **Date:** 2026-08-18

## Context
The project must run on a clean machine with zero mandatory infrastructure, yet demonstrate
production-grade federation locally.

## Decision
Dev/test use a built-in IdP (Argon2id hashes, progressive lockout, HS256 JWTs). The `local`
profile switches to Keycloak OIDC (RS256, issuer validation). Both paths converge on the same
JWT converter + RBAC model.

## Consequences
+ Zero-dependency quickstart; realistic federation path; identical authorization model.
− Two token signatures to reason about (documented; realm config shipped in docker/).
