# Java 700 — Industrial-Grade Java Project Portfolio

A portfolio of **exactly 700 unique, industrial-grade Java system designs** — production-oriented
Spring Boot / Java 21+ architectures spanning banking, healthcare, manufacturing, telecom,
cybersecurity, logistics, energy, transport, data/AI infrastructure, developer platforms and
government systems.

| Tier | Range | Count |
|------|-------|-------|
| Advanced | JAVA-001 … JAVA-100 | 100 |
| Expert | JAVA-101 … JAVA-250 | 150 |
| Architect | JAVA-251 … JAVA-400 | 150 |
| Enterprise Platform | JAVA-401 … JAVA-550 | 150 |
| Omega | JAVA-551 … JAVA-700 | 150 |

## Repository layout

```
java-700-portfolio/
├── README.md                    ← you are here
├── catalog/
│   ├── MASTER-INDEX.md          ← all 700 projects, one table
│   ├── enterprise-business-platforms.md
│   ├── banking-fintech-insurance.md
│   ├── healthcare-pharma-life-sciences.md
│   ├── manufacturing-industrial-iot-robotics.md
│   ├── telecom-networking-media.md
│   ├── cybersecurity-identity-secrets.md
│   ├── logistics-supply-chain-fleet.md
│   ├── energy-utilities-grid.md
│   ├── automotive-aerospace-transportation.md
│   ├── data-ai-infrastructure.md
│   ├── developer-platform-infrastructure.md
│   └── government-compliance-public-infrastructure.md
├── data/catalog.json            ← machine-readable full catalog (all 23 fields/project)
├── scripts/                     ← catalog source blocks + assembler/generators
└── projects/                    ← implemented codebases (one folder per implemented ID)
```

## How it works

1. Pick a Project ID from the master index (e.g. `JAVA-347`).
2. That project gets implemented in full: complete Maven project, domain model, Flyway
   migrations, security architecture, REST APIs, tests (unit / integration / security /
   resilience), Docker + docker-compose local stack, observability (Prometheus/Grafana/
   OpenTelemetry), seed data, API docs, threat model, runbook and ADRs.
3. The finished codebase is **committed and pushed to this GitHub repository** under
   `projects/<ID>/`, verified green, and the local scratch copy is then removed to keep the
   workspace lean.

## Ground rules (from the master spec)

- Java is the primary language; Spring Boot 3.x + Java 21+ baseline, modern features
  (virtual threads, records, sealed types, pattern matching).
- Every project is production-oriented with serious security, auditability, failure
  handling, observability and automated testing — never a tutorial CRUD app.
- Local-first: everything runs on a local machine via Docker Compose (PostgreSQL, Redis,
  Kafka/RabbitMQ, Keycloak, Prometheus, Grafana, Jaeger, MinIO, etc.). No mandatory cloud
  or paid APIs.
- Each project is architecturally justified (modular monolith unless distribution is
  genuinely required) and demonstrates skills that hold up in senior Java interviews.
