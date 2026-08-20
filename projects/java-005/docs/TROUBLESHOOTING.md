# JAVA-005 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| 400 on definition | malformed model / no START node | validate the definition JSON |
| 409 on start | business key already used for this definition | use a new key or fetch the instance |
| 409 on task complete | already decided | idempotent replay returns the instance state |
| Instance stuck WAITING_TIMER | timer not yet due | scheduler resumes when due; or run POST /scheduler/timers |
| PostgreSQLMigrationIT skipped | no Docker | CI covers it |
