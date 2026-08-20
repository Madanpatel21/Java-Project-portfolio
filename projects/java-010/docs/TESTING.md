# TESTING — Capacity & Shift Rostering Optimizer (JAVA-010)

## Gate
`mvn verify -Pstatic-analysis` must end with:
- `Tests run: 6, Failures: 0, Errors: 0, Skipped: 1` (PostgreSQLMigrationIT skips without Docker)
- `You have 0 Checkstyle violations.`
- `BugInstance size is 0`

## Suites
| Suite | Coverage |
|---|---|
| `SolverIT` (2) | feasible full-coverage solve (21/21, 0hard), skill-match verification per assignment, constraint explanation, publish gate |
| `SwapIT` (3) | request→approve moves assignment, reject keeps it, skill-mismatch 409, RBAC boundaries |
| `PostgreSQLMigrationIT` (1) | Flyway V1–V3 on PostgreSQL 16 (CI) |

## Notes
- Solver time limit is 3s in the test profile (`app.roster.solver-time-limit`).
- Assertions target score feasibility + skill invariants, not specific assignments
  (solver is seeded-deterministic but move order varies).
- Publish-gate tests assert the 409 before optimization and 200 after.
