# JAVA-005 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only | rotate |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.scheduler.enabled` | true | timer resume + escalation scans |
| `app.rate-limit.auth-per-minute` | 10 | auth limit |
