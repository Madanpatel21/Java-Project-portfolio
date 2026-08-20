# TROUBLESHOOTING — Capacity & Shift Rostering Optimizer (JAVA-010)

| Symptom | Cause | Fix |
|---|---|---|
| `0hard` but soft never improves | Solver hit the time limit | Raise `app.roster.solver-time-limit` |
| Hard violation on Skill match | Demand exceeds qualified capacity | Add skilled employees or lower demand |
| 409 on swap request | Not your assignment / target lacks skill / target unavailable | Check the conflict detail message |
| Swap approved but assignment unchanged | Exchange moved both assignments | Inspect both employees' schedules |
| Publish 409 with full coverage | Score JSON missing — optimize first | POST /rosters/{id}/optimize |
| Context fails with TimefoldProperties | Stray `timefold.*` properties in yml | Timefold config goes via SolverConfigOverride in code |
| Slow first solve | Solver warm-up / JIT | Subsequent solves are faster; check `roster_optimization_duration_seconds` |
