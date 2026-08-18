# ADR-0003 — Certificate/verification endpoints disclose identity by design

**Status:** accepted · **Date:** 2026-08-18

## Context
Listings must be masked, but a certificate's legal purpose IS disclosure. Blanket masking would
make verification useless.

## Decision
Listings/search mask identity; certificate verification and person verification disclose full
identity ONLY for valid, non-revoked tokens/ids, and mask it otherwise.

## Consequences
+ Usable verification with least disclosure elsewhere. − Disclosure paths need strict
authorization (VERIFIER_CLIENT + rate limits) — documented and tested.
