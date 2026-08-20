# JAVA-003 — Testing Guide

```bash
mvn test                      # 18 tests (unit/IT/security; Testcontainers PG auto-skips without Docker)
mvn verify -Pstatic-analysis  # + Checkstyle + SpotBugs (Max effort)
```

## Suite inventory (18 tests)
- ContractDiffTest (5) — diff correctness, old/new text, malformed JSON, clause parsing
- ContractFlowIT (5) — four-eyes activation, clearance redaction, obligation SLA + waiver,
  recurrence, version diff endpoint
- SecurityIT (6) — role matrix, four-eyes, auditor read-only, injection, lockout, validation-first
- PostgreSQLMigrationIT (1) — Flyway V1–V2 on PostgreSQL 16 (CI; skipped without Docker)
