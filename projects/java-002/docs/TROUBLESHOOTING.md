# JAVA-002 — Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| 409 on invoice ingest | duplicate number+supplier or in-flight idempotency key | verify with `GET /invoices`; retry with a fresh key |
| 403 on waive as AP_CLERK | four-eyes by design | use AP_MANAGER/ADMIN |
| Invoice stuck EXCEPTION | open exceptions remain | decide every exception (waive/reject) → APPROVED |
| Postings missing | invoice not APPROVED yet | check invoice status; run `POST /batch` |
| PostgreSQLMigrationIT skipped | no Docker | `docker compose up postgres` locally or rely on CI |
| Tests polluted across ITs | shared H2 context | new ITs must call `TestDb.clean` in `@BeforeEach` |
