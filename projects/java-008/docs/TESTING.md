# TESTING — Expense Fraud & Policy Analytics Engine (JAVA-008)

## Gate
`mvn verify -Pstatic-analysis` must end with:
- `Tests run: 16, Failures: 0, Errors: 0, Skipped: 1` (PostgreSQLMigrationIT skips without Docker)
- `You have 0 Checkstyle violations.`
- `BugInstance size is 0`

## Suites
| Suite | Coverage |
|---|---|
| `ScoringIT` (6) | rule violations, weekend mileage, round amounts, ATM blocker, duplicate cluster, peer outlier |
| `CaseWorkflowIT` (3) | four-eyes lifecycle, manager policy-block, cleared→approved, invalid transitions |
| `ClaimGuardIT` (4) | idempotency, manager decisions, validation-first, PII masking, role boundaries |
| `TipIT` (2) | anonymous intake, restricted lists, double-review guard, payload validation |
| `PostgreSQLMigrationIT` (1) | Flyway V1–V3 against real PostgreSQL 16 (Testcontainers, CI only) |

## Local runs
```bash
mvn test                       # unit + integration on H2 (in-memory, PostgreSQL mode)
mvn verify -Pstatic-analysis   # + Checkstyle + SpotBugs
```

## Design notes
- Tests share one H2 context; `TestDb.clean` resets all mutable tables between tests.
- `policy_rules` is seeded by Flyway V3 and deliberately excluded from cleanup.
- Scoring assertions pin exact point contributions, so detector weight changes fail tests loudly.
