# JAVA-211 — Operational Runbook

## Daily
- [ ] Grafana: open review tasks, drug-bug mismatch alert counter, intervention acceptance.
- [ ] Triage OPEN `DRUG_BUG_MISMATCH` and `TIME_BASED` tasks — these are clinical safety items.

## Weekly
- [ ] Restricted-authorization expiry sweep log (`Restricted authorization expired — therapy stopped`).
- [ ] Review `RENAL_ADJUSTMENT_NEEDED` findings vs latest lab results.
- [ ] Check guideline versions (every activation is audited).

## Monthly
- [ ] Antibiogram review: confirm rows ≥ 30 isolates before external publication.
- [ ] DOT/1000 per ward trend — investigate wards drifting above benchmark.

## Incident: therapy continued on resistant organism
1. The system auto-creates the CRITICAL task + audit entry; acknowledge and escalate to ID.
2. If the task is missing, check `CultureReported` event delivery (broker/DLQ in local profile).
3. Stop the prescription (`POST /prescriptions/{id}/stop`) — tasks cancel automatically.

## Retention
- Audit log: append-only; archive monthly to WORM storage.
- Idempotency records: auto-purged daily (3:17 UTC).
