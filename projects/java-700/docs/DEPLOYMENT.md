# JAVA-700 — Deployment Guide

## Local production-like (Docker Compose)
```bash
cp .env.example .env          # rotate JWT secret + certificate pepper
docker compose up --build     # postgres, rabbitmq, app, prometheus, grafana
```
Startup order is health-check gated. Verify: `curl localhost:8080/actuator/health`.

## Container image
```bash
docker build -t civil-registry:1.0.0 .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev civil-registry:1.0.0
```

## CI
`.github/workflows/ci.yml` — full verify (incl. Testcontainers PostgreSQL), static analysis,
packaging, artifact upload on pushes touching `projects/java-700/`.
