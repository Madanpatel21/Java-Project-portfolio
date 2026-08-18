# JAVA-001 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| Health DOWN with `evidence` detail broken | ledger tampered | check `/api/v1/evidence/verify` for `brokenSeq`, restore from backup, investigate |
| `Connection refused` to localhost:6379/5672 in dev | broker indicators probing disabled brokers | dev profile disables those indicators; ignore, or use the local profile |
| Test fails with unique-constraint on usernames | shared H2 context across IT classes | tests reset tables via `TestDb.clean` in `@BeforeEach`; add new ITs accordingly |
| 409 with "Idempotency-Key is still being processed" | concurrent retry while first request in flight | retry after completion; or the key was abandoned by a failed validation (fixed paths) |
| 429 on auth | lockout or rate limit | wait 15 min (lockout) / 1 min (window); check `app.rate-limit.*` |
| PostgreSQLMigrationIT skipped | no Docker | run `docker compose up postgres` locally or rely on CI |
| Export job stuck PENDING | worker crash | outbox rescan re-queues after 2 min; check broker in local profile |
| OWASP dependency-check fails in CI | NVD feed throttling | supply `NVD_API_KEY` secret to the workflow |
