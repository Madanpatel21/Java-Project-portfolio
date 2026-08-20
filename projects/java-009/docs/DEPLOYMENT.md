# DEPLOYMENT — Fleet Maintenance Planning System (JAVA-009)

## Artifact
```bash
mvn -DskipTests package
java -jar target/fleet-maintenance-1.0.0.jar --spring.profiles.active=prod --server.port=8080
```

## Docker Compose (PostgreSQL 16 + RabbitMQ + app)
```bash
cp .env.example .env   # JWT_SECRET, DB password, MESSAGING_ENABLED
docker compose up --build
```
Multi-stage image (Maven → Temurin 21 JRE, non-root user).

## Profiles
- `dev` — H2 in-memory, seeded accounts, Quartz on (30-min cron)
- `test` — H2, scheduler off, rate limits lifted
- `prod` — PostgreSQL via env, scheduler + messaging as configured

## Scheduler
Quartz RAM job store (single node). For multi-node: switch to the JDBC job store
(Quartz clustering) — the forecast itself is already idempotent and safe to run
concurrently.

## CI
`mvn verify -Pstatic-analysis` + PostgreSQL 16 service container (real migration IT)
+ artifact packaging.
