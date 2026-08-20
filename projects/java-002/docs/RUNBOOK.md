# JAVA-002 — Operational Runbook

## Daily
- [ ] Grafana: invoices ingested vs matched, open exceptions, posting volume.
- [ ] Triage CRITICAL exceptions; escalate supplier disputes with the audit trail.

## Weekly
- [ ] Review waivers — every waiver is an AP_MANAGER decision; sample for abuse.
- [ ] Check the tolerance rules against observed variance distribution (tighten/loosen with ADMIN).

## Monthly
- [ ] Reconcile GL posting totals (debit GRNI == credit AP) from `gl_postings`.
- [ ] Confirm outbox drain: no PENDING records older than the publishing window.

## Incident: duplicate payment suspected
1. Query the invoice by number+supplier — the unique constraint guarantees one record.
2. Check idempotency records for replay attempts.
3. Check `gl_postings` for the invoice — posting is idempotent per invoice.
