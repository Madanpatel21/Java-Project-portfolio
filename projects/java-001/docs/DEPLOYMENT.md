# JAVA-001 — Deployment Guide

## Local production-like (Docker Compose)

```bash
cp .env.example .env          # review/rotate all secrets
docker compose up --build     # postgres, redis, rabbitmq, keycloak, app, prometheus, grafana, jaeger
```

Startup order is health-check gated (app waits on all four dependencies). Verify:

```bash
curl http://localhost:8080/actuator/health            # UP (incl. evidence-chain indicator)
curl http://localhost:9090/api/v1/targets             # prometheus scraping app
open http://localhost:3000/d/java001                  # grafana dashboard
```

### Keycloak tokens (local profile)
```
curl -X POST http://localhost:8081/realms/java700/protocol/openid-connect/token \
  -d grant_type=password -d client_id=workforce-api \
  -d username=auditor -d password=Password123!
```

## Container image build

```bash
docker build -t workforce-compliance:1.0.0 .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev workforce-compliance:1.0.0
```

## CI (GitHub Actions)
`.github/workflows/ci.yml` — runs on pushes touching `projects/java-001/`: full verify
(including the Testcontainers PostgreSQL migration test), static analysis, packaging, artifact upload.
