# DEPLOYMENT — Capacity & Shift Rostering Optimizer (JAVA-010)

## Artifact
```bash
mvn -DskipTests package
java -jar target/roster-optimizer-1.0.0.jar --spring.profiles.active=prod --server.port=8080
```

## Docker Compose
```bash
cp .env.example .env   # JWT_SECRET, DB password, MESSAGING_ENABLED, SOLVER_TIME_LIMIT
docker compose up --build
```

## Profiles
- `dev` — H2 in-memory, seeded workforce, 5s solver limit
- `test` — H2, 3s solver limit, rate limits lifted
- `prod` — PostgreSQL via env; solver limit via `SOLVER_TIME_LIMIT`

## Sizing the solver
21 slots × 5 employees solves in <1s of construction; 5s finds the optimum for
this scale. Hundreds of employees/shifts: raise the limit or partition by
department. CI runs the solver in REPRODUCIBLE mode for deterministic results.
