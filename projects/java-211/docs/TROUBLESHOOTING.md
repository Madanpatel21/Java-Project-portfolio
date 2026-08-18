# JAVA-211 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Schema-validation: missing table` | migration drift | run `mvn flyway:migrate` against target DB; check `flyway_schema_history` |
| 409 on culture report | cultures are final | create a new culture for re-sampling |
| Restricted drug stuck PENDING | no ID approval | approve via API; check pending list |
| Intervention 400 on reject | rejection requires a reason | include `response` in the body |
| No review tasks appear | schedulers disabled or trigger window | check `app.scheduler.enabled`; `reviewDue` needs empiric + >48h |
| PostgreSQLMigrationIT skipped | no Docker | run `docker compose up postgres` locally or rely on CI |
| Test DB pollution across ITs | shared H2 context | tests clean via `TestDb.clean` in `@BeforeEach`; keep it in new ITs |
