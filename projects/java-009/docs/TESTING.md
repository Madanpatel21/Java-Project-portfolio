# TESTING — Fleet Maintenance Planning System (JAVA-009)

## Gate
`mvn verify -Pstatic-analysis` must end with:
- `Tests run: 17, Failures: 0, Errors: 0, Skipped: 1` (PostgreSQLMigrationIT skips without Docker)
- `You have 0 Checkstyle violations.`
- `BugInstance size is 0`

## Suites
| Suite | Coverage |
|---|---|
| `ForecastIT` (4) | due/overdue from meter+calendar, priorities, idempotency, overdue flip, RBAC |
| `OdometerIT` (4) | rollback 409, suspicious jump flag, normal accept, role boundaries |
| `WorkOrderIT` (4) | reservation→issue, PARTS_HOLD→restock→retry, reject releases, RBAC |
| `ComplianceIT` (4) | hold/release lifecycle, report, overdue compliance forecast, validation |
| `PostgreSQLMigrationIT` (1) | Flyway V1–V3 on PostgreSQL 16 (CI) |

## Notes
- Shared H2 context; `TestDb.clean` resets mutable tables (plans/parts seeds excluded —
  parts are re-seeded per test where needed).
- Scheduler disabled in tests (`app.scheduler.enabled=false`); forecast invoked via API.
- Cost assertions pin exact BigDecimal values so pricing changes fail loudly.
