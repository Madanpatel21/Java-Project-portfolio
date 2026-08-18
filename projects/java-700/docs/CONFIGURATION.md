# JAVA-700 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only value | HS256 signing secret — **rotate** |
| `app.security.jwt.ttl-minutes` | 30 | token lifetime |
| `app.security.lockout.max-attempts` | 5 | failures before lock |
| `app.security.lockout.lock-minutes` | 15 | lock duration |
| `app.registry.dedup.enabled` | true | dedup scanning on births |
| `app.registry.dedup.threshold` | 0.85 | candidate score threshold |
| `app.registry.certificate-pepper` | dev-only value | certificate content-hash pepper — **rotate** |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.seed.enabled` | false (true in dev) | demo offices/users |
| `app.rate-limit.auth-per-minute` | 10 | auth endpoint limit |
| `app.rate-limit.verify-per-minute` | 60 | verification endpoint limit |

Profiles: `dev` (H2 + local IdP + in-process bus, seeded) · `local` (PostgreSQL + RabbitMQ via compose) ·
`test` (H2, relaxed limits).
