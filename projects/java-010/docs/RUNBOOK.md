# RUNBOOK — Capacity & Shift Rostering Optimizer (JAVA-010)

| Symptom | Action |
|---|---|
| Optimize returns infeasible score | Read the explain endpoint: constraint with hard<0 names the bottleneck (skill mix or weekly caps). Adjust demand or workforce. |
| Publish returns 409 | Roster has unassigned slots or was never optimized — optimize first |
| Swap stuck PENDING | Manager approves/rejects via `POST /api/v1/swaps/{id}/decide` |
| Swap approval 409 | Target lacks skill / unavailable / double-booked — reassign the target |
| Employee cannot see shifts | Check the employee's `user_id` link and that the roster is published |
| Slow solves | Raise `app.roster.solver-time-limit` or split rosters per department |

## Metrics to watch
`roster_rosters_optimized_total`, `roster_optimization_duration_seconds`,
`roster_swaps_approved_total` vs `roster_swaps_rejected_total`.
