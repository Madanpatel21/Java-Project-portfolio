# TROUBLESHOOTING — Fleet Maintenance Planning System (JAVA-009)

| Symptom | Cause | Fix |
|---|---|---|
| 409 on odometer submit | Reading below last recorded value | Verify meter; escalate if genuine (audit trail intact) |
| 409 starting a work order | PARTS_HOLD or wrong state | Resolve parts, retry-parts, then start |
| Task never completes | Work order still OPEN | start → complete (mechanic) |
| Vehicle stuck IN_SHOP | Rejected WO reopened the task, vehicle state not updated | Open + complete a new WO; or set status via fleet manager |
| COMPLIANCE_HOLD persists after PASS | PASS without validUntil or different vehicle | Record PASS with validUntil in the future |
| Forecast creates no task | Plan inactive / category mismatch / beyond horizon | Check plan active flag and interval math |
| Quartz "NOT STARTED" at boot | Normal — starts right after context refresh; or scheduler disabled | Check `app.scheduler.enabled` |
