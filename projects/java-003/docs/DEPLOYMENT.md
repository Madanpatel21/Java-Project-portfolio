# JAVA-003 — Deployment Guide

```bash
cp .env.example .env && docker compose up --build   # postgres, rabbitmq, app, prometheus, grafana
docker build -t contract-obligations:1.0.0 .
```
CI: `.github/workflows/ci.yml` — verify + static analysis + packaging on pushes to `projects/java-003/`.
