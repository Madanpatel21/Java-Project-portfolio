# JAVA-004 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.govault.content-dir` | ./data/content | content store directory |
| `app.security.jwt.secret` | dev-only | rotate |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process |
| `app.scheduler.enabled` | true | retention scan on/off |
| `app.rate-limit.auth-per-minute` | 10 | auth limit |

Retention classes are data: seeded in `V3__retention_rules.sql` (R0..R7).
