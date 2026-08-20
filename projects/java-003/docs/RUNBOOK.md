# JAVA-003 — Operational Runbook

## Daily
- [ ] Grafana: open obligations, reminders sent, waivers.
- [ ] Triage OVERDUE obligations — every overdue HIGH obligation is an escalation.

## Weekly
- [ ] Review waivers (LEGAL decisions) for pattern abuse.
- [ ] Verify obligation events history for the week (append-only).

## Monthly
- [ ] Sample contract versions vs the clause diff endpoint for legal review.
- [ ] Confirm recurring obligations spawned correctly.

## Incident: obligation missed
1. Check the obligation's event history (`obligation_events`).
2. Determine whether the scan ran (scheduler logs + manual `POST /obligations/scan`).
3. Escalate per the waiver workflow — never silently close.
