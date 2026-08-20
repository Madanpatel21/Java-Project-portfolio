# CONFIGURATION — Expense Fraud & Policy Analytics Engine (JAVA-008)

| Key | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev value (rotate!) | HMAC signing key, ≥ 32 bytes |
| `JWT_TTL_MINUTES` | 30 | token lifetime |
| `app.security.lockout.max-attempts` | 5 | failed logins before lock |
| `app.security.lockout.lock-minutes` | 15 | lock duration |
| `app.fraud.high-threshold` | 65 | HIGH tier → auto case |
| `app.fraud.medium-threshold` | 35 | MEDIUM tier floor |
| `app.fraud.duplicate-lookback-days` | 30 | clustering window |
| `app.fraud.min-peer-samples` | 5 | baseline samples required for outlier test |
| `app.scheduler.enabled` | true | nightly baseline recompute |
| `app.scheduler.baseline-cron` | `0 15 2 * * *` | cron expression |
| `app.messaging.enabled` | false | RabbitMQ event publishing |
| `app.rate-limit.auth-per-minute` | 10 | login throttling |
| `SPRING_DATASOURCE_URL` | H2 mem (dev) | JDBC URL in prod |

Policy rules themselves live in the database (`policy_rules`, seeded by Flyway V3) and
can be toggled live by an admin — no rebuild required.
