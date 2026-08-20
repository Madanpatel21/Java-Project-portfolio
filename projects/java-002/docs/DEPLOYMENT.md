# JAVA-002 — Deployment Guide

## Local production-like
```bash
cp .env.example .env && docker compose up --build   # postgres, rabbitmq, app, prometheus, grafana
```
Startup order is health-check gated. Verify: `curl localhost:8080/actuator/health`.

## Container
```bash
docker build -t p2p-reconciliation:1.0.0 .
docker run --rm -p 8080:8080 -e SPRING_PROFILES_ACTIVE=dev p2p-reconciliation:1.0.0
```

## CI
`.github/workflows/ci.yml` — verify + static analysis + packaging on pushes to `projects/java-002/`.
