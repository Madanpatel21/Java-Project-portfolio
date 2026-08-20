# JAVA-004 — Testing Guide

```bash
mvn test                      # 12 tests (unit/IT/security; Testcontainers PG auto-skips)
mvn verify -Pstatic-analysis  # + Checkstyle + SpotBugs (Max effort)
```
Suites: GovernanceFlowIT (full loop, hold protection, clearance download, search, idempotency) ·
SecurityIT (role matrix, validation-first, lockout, injection) · PostgreSQLMigrationIT (CI).
