# JAVA-211 — Configuration Reference

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only value | HS256 signing secret — **rotate** |
| `app.security.jwt.ttl-minutes` | 30 | token lifetime |
| `app.security.lockout.max-attempts` | 5 | failures before lock |
| `app.security.lockout.lock-minutes` | 15 | lock duration |
| `app.stewardship.review-trigger-hours` | 48 | empiric review trigger |
| `app.stewardship.restricted-auth-hours` | 72 | restricted approval TTL |
| `app.stewardship.redundant-coverage-overlap-hours` | 24 | redundancy threshold |
| `app.stewardship.antibiogram-min-isolates` | 30 | reporting gate |
| `app.stewardship.isolate-dedup-days` | 7 | first-isolate window |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.seed.enabled` | false (true in dev) | demo scenario |
| `app.scheduler.enabled` | true | review scan / expiry sweeps |
| `app.rate-limit.auth-per-minute` | 10 | auth endpoint limit |

Profiles: `dev` (H2 + local IdP + in-process bus, seeded) · `local` (PostgreSQL + RabbitMQ via compose) ·
`test` (H2, schedulers off, relaxed limits).
