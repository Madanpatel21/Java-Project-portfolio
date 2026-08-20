# CONFIGURATION — Fleet Maintenance Planning System (JAVA-009)

| Key | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev value (rotate!) | HMAC signing key |
| `app.scheduler.enabled` | true | Quartz forecast job |
| `app.scheduler.forecast-cron` | `0 0/30 * * * ?` | forecast cadence |
| `app.fleet.due-window-km` | 1500 | odometer DUE window |
| `app.fleet.forecast-horizon-km` | 3000 | odometer SCHEDULED horizon |
| `app.fleet.due-window-days` | 7 | calendar DUE window |
| `app.fleet.forecast-horizon-days` | 14 | calendar SCHEDULED horizon |
| `app.messaging.enabled` | false | RabbitMQ event publishing |
| `app.rate-limit.auth-per-minute` | 10 | login throttling |
| `SPRING_DATASOURCE_URL` | H2 (dev) | JDBC URL in prod |

Maintenance plans (interval type/value, compliance flag, parts kit) live in the
database and are editable by admins without a rebuild.
