# JAVA-007 — Deployment Guide
```bash
cp .env.example .env && docker compose up --build
docker build -t legal-matter:1.0.0 .
```
CI: `.github/workflows/ci.yml` on pushes to `projects/java-007/`.
