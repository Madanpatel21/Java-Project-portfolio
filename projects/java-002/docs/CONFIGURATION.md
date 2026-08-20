# JAVA-002 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only | HS256 secret — rotate |
| `app.security.lockout.max-attempts` | 5 | failures before lock |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.scheduler.enabled` | true | posting scheduler on/off |
| `app.rate-limit.auth-per-minute` | 10 | auth endpoint limit |
| `app.rate-limit.verify-per-minute` | 60 | verification endpoint limit |

Tolerance rules are data, not config: seeded in `V3__tolerance_rules.sql`, editable via
`POST /api/v1/tolerance-rules/{id}` (ADMIN).
