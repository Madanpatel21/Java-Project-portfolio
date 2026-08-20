# RUNBOOK — Expense Fraud & Policy Analytics Engine (JAVA-008)

## Daily operations
| Symptom | Action |
|---|---|
| High-risk claim not in queue | Check claim status: `UNDER_REVIEW` claims appear in `/api/v1/cases` (case workflow), not the manager flow |
| Baseline looks stale | `POST /api/v1/admin/baselines/recompute` (admin) or wait for the 02:15 nightly job |
| Suspicious score but no reason | Every score persists reasons — `GET /api/v1/claims/{id}` (investigator) or `GET /api/v1/cases/{id}` |
| Tip backlog | `GET /api/v1/tips` (investigator) → `POST /api/v1/tips/{id}/review` with outcome |
| Rule too strict | `POST /api/v1/admin/rules/{code}/active?active=false` (admin) — fully data-driven policy |
| Locked-out user | Wait for lock expiry (15 min) or reset `failed_attempts`/`locked_until` in `local_users` |

## Incident response
1. Confirm fraud via case: investigator 1 `RECOMMEND_FRAUD`, investigator 2 `CONFIRM_FRAUD`.
2. The claim becomes `CONFIRMED_FRAUD` — downstream payroll integration reads this state.
3. Extract the evidence package: reasons JSON + violations + duplicate group (claim numbers,
   merchant, confidence) for the audit trail.

## Metrics to watch (Prometheus)
- `expfraud_claims_scored_total` / `expfraud_cases_opened_total` — case load
- `expfraud_scoring_duration_seconds` — pipeline latency
- `expfraud_cases_confirmed_fraud_total` vs `expfraud_cases_cleared_total` — precision
- `expfraud_claims_risk_score` distribution — score drift after rule changes
