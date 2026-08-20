# DEPLOYMENT — Expense Fraud & Policy Analytics Engine (JAVA-008)

## Artifact
```bash
mvn -DskipTests package
java -jar target/expense-fraud-1.0.0.jar --spring.profiles.active=prod --server.port=8080
```

## Docker Compose (PostgreSQL 16 + RabbitMQ + app)
```bash
cp .env.example .env    # set JWT_SECRET, DB password, MESSAGING_ENABLED
docker compose up --build
```
The image is a multi-stage build (Maven → Temurin 21 JRE, non-root user).

## Profiles
- `dev` — in-memory H2, seeded role accounts, verbose logging (default)
- `test` — H2, scheduler disabled, rate limits lifted
- `prod` — PostgreSQL (env-driven), scheduler enabled, messaging optional

## CI (`.github/workflows/ci.yml`)
1. `mvn verify -Pstatic-analysis` (checkstyle + spotbugs + 16 tests)
2. PostgreSQL 16 service container → `PostgreSQLMigrationIT` runs for real
3. Package artifact upload

## Operations
- Graceful shutdown (20 s), liveness/readiness probes on `/actuator/health`
- Nightly baseline recompute: `app.scheduler.baseline-cron` (default `0 15 2 * * *`)
- Event publishing toggled by `app.messaging.enabled`; DLX queues guarantee no message loss
