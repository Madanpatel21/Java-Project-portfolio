# JAVA-003 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| 409 on activate | role already decided | the other governance role must decide |
| 403 on waive as manager | four-eyes by design | use LEGAL or ADMIN |
| Clause shows [REDACTED] | clearance < sensitivity | escalate role or lower clause sensitivity (via new version) |
| 400 on obligation create | invalid type/criticality (validated first) | use PAYMENT/RENEWAL/DELIVERY/EXIT_RIGHT/COMPLIANCE/INSURANCE/OTHER |
| PostgreSQLMigrationIT skipped | no Docker | `docker compose up postgres` locally or rely on CI |
