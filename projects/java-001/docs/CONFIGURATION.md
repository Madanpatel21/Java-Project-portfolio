# JAVA-001 — Configuration Reference

All settings live in `application.yml` (+ profile overlays). Every secret is injectable via env.

| Property | Default | Meaning |
|---|---|---|
| `app.security.jwt.secret` | dev-only value | HS256 signing secret (dev/test) — **must rotate** |
| `app.security.jwt.issuer` | http://localhost:8080 | token issuer; in local profile it is the Keycloak realm URI |
| `app.security.jwt.ttl-minutes` | 30 | access-token lifetime |
| `app.security.lockout.max-attempts` | 5 | failures before lock |
| `app.security.lockout.lock-minutes` | 15 | lock duration |
| `app.security.export.signing-key` | dev-only value | HMAC key for export bundles — **must rotate** |
| `app.security.export.outbox-dir` | ./data/exports | export bundle directory |
| `app.messaging.enabled` | false | RabbitMQ (true) vs in-process bus |
| `app.redis.enabled` | false | Redis caches/rate-limits (true) vs in-memory |
| `app.seed.enabled` | false (true in dev) | demo seed data |
| `app.correlation.enabled` | true | correlation scheduler on/off |
| `app.correlation.cron` | 0 */5 * * * * | correlation schedule |
| `app.correlation.recert-interval-days` | 90 | default recert due offset |
| `app.sweep.enabled` | true | grant expiry sweep |
| `app.sweep.fixed-delay-ms` | 60000 | sweep interval |
| `app.retention.access-events-days` | 90 | access-event retention hint (runbook) |
| `app.rate-limit.ingest-per-minute` | 120 | ingest path limit |
| `app.rate-limit.auth-per-minute` | 10 | auth path limit |

Profiles: `dev` (H2 + built-in IdP + in-process bus, seeded) · `local` (full stack via compose) ·
`test` (H2, no schedulers, relaxed rate limits).
