# TROUBLESHOOTING — Expense Fraud & Policy Analytics Engine (JAVA-008)

| Symptom | Cause | Fix |
|---|---|---|
| 401 on `/api/v1/tips` GET | Tip lists require investigator/auditor/admin | POST is open; GET needs a role |
| 409 `four-eyes violated` | Same investigator reviewing and deciding | Use a second investigator account |
| 409 `must go through the four-eyes case workflow` | Claim is HIGH risk or has a BLOCKER | Resolve via `/api/v1/cases` workflow |
| 429 on login | Rate limit (default 10/min) | Wait 60 s; raise `auth-per-minute` in dev |
| No `PEER-OUTLIER` reason | < 5 baseline samples or zero std-dev | Submit more claims, then recompute baselines |
| Duplicate group not created | Claims outside the ±30 day / 1 % tolerance window | Verify merchant spelling + dates |
| Flyway validation error | Schema drift vs entity mapping | Run `mvn flyway:validate`; check V1–V3 checksums |
| Testcontainers error locally | No Docker daemon | Expected — PostgreSQLMigrationIT auto-skips |
| Tip review 409 | Tip already closed | Each tip is reviewed once |
