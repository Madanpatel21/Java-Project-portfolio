# JAVA-003 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only | HS256 secret — rotate |
| `app.security.lockout.max-attempts` | 5 | failures before lock |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.scheduler.enabled` | true | obligation SLA scan on/off |
| `app.rate-limit.auth-per-minute` | 10 | auth endpoint limit |
