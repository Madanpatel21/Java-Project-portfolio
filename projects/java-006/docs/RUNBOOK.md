# JAVA-006 — Operational Runbook
## Daily
- [ ] Grafana: pending requests, decisions recorded, escalations.
- [ ] Triage stale PENDING requests (escalated = renewed SLA).
## Weekly
- [ ] Sample decision evidence against the policy version active at request time.
## Incident: contested approval
1. Pull the request's decisions (`GET /requests/{id}/decisions`) — full chain evidence.
2. Verify the policy version id vs the version history — the binding proves which rules applied.
