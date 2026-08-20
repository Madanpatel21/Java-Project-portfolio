# CONFIGURATION — Capacity & Shift Rostering Optimizer (JAVA-010)

| Key | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev value (rotate!) | HMAC signing key |
| `SOLVER_TIME_LIMIT` / `app.roster.solver-time-limit` | 5s | per-solve termination (passed as SolverConfigOverride) |
| `app.messaging.enabled` | false | RabbitMQ event publishing |
| `app.rate-limit.auth-per-minute` | 10 | login throttling |
| `SPRING_DATASOURCE_URL` | H2 (dev) | JDBC URL in prod |

Labor rules (thresholds, weights, active flags) live in the `roster_rules` table
(V3 seed) and mirror the compiled constraint set; constraint weights are code —
rule rows document the policy.
