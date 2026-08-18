# JAVA-700 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| 403 on approve as registrar | method security (by design) | use a SUPERVISOR/ADMIN account |
| 409 on approve with dual-role account | in-domain SoD | a different supervisor must approve |
| 400 on verify with a national id | malformed or bad checksum | check the id format (10 digits) |
| Ledger verify DOWN | tamper or sequence gap | see runbook incident procedure |
| Dedup candidates not appearing | threshold or blocking key | check `app.registry.dedup.threshold`; blocking requires same sex+DOB+initial |
| PostgreSQLMigrationIT skipped | no Docker | run `docker compose up postgres` locally or rely on CI |
| Test DB pollution across ITs | shared H2 context | tests clean via `TestDb.clean` in `@BeforeEach` |
