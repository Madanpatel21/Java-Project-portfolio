# JAVA-005 — Operational Runbook

## Daily
- [ ] Grafana: instances started/completed, pending tasks, escalations.
- [ ] Triage escalated tasks — repeated escalation indicates a broken assignment.

## Weekly
- [ ] Review definition versions; confirm running instances pin to correct versions.

## Incident: instance stuck in WAITING_TASK
1. Check the pending tasks and their assignee roles.
2. Complete or cancel; cancellation creates compensation tasks (audited).
