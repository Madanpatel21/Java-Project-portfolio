# JAVA-002 — Testing Guide

```bash
mvn test                      # 25 tests (unit/IT/security; Testcontainers PG auto-skips without Docker)
mvn verify                    # + package
mvn verify -Pstatic-analysis  # + Checkstyle + SpotBugs (Max effort)
jmeter -n -t jmeter/plan.jmx -JTOKEN=<bearer>
```

## Suite inventory (25 tests)
- MatchingEngineTest (11) — match correctness incl. tolerance boundaries and fuzzy supplier match
- P2PFlowIT (6) — end-to-end loop, idempotency, duplicates, waiver → posting, PO view
- SecurityIT (7) — role matrix, four-eyes, admin-only rule edits, injection, lockout
- PostgreSQLMigrationIT (1) — Flyway V1–V3 on PostgreSQL 16 (CI; skipped without Docker)
