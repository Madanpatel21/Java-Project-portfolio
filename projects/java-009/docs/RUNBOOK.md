# RUNBOOK — Fleet Maintenance Planning System (JAVA-009)

| Symptom | Action |
|---|---|
| No tasks appearing | Run `POST /api/v1/scheduling/forecast/run` (fleet manager); check plan `active` and category match |
| Duplicate tasks per vehicle | Not possible by design — forecast is idempotent per vehicle+plan; open a ticket with task numbers if seen |
| Work order stuck on PARTS_HOLD | Clerk restocks the shortfall part, then `POST /api/v1/work-orders/{id}/retry-parts` |
| Odometer submission rejected | Reading is below last recorded — verify meter; tamper attempts are audit-logged |
| SUSPICIOUS_JUMP flag | Review the odometer history (manager/auditor); follow up with the driver/shop |
| Vehicle on COMPLIANCE_HOLD | Last inspection FAILED — repair, then record a PASS inspection (compliance officer) |
| Quartz job not running | Check `app.scheduler.enabled` and `app.scheduler.forecast-cron`; manual endpoint is always available |

## Metrics to watch
`fleet_tasks_overdue_total` (SLA risk), `fleet_workorders_parts_hold_total`
(stock-outs), `fleet_odometer_tamper_flags_total` (fraud), `fleet_inspections_failed_total`.
