# JAVA-001 — Operational Runbook

## Daily
- [ ] Watch Grafana: evidence appends, open violations, ingest rate, correlation duration.
- [ ] Check `GET /api/v1/evidence/verify` (auditor) — must be `valid: true`.
- [ ] Review open HIGH violations; acknowledge within SLA.

## Weekly
- [ ] Grant expiry sweep log lines (`Grant expiry sweep: N grants expired`).
- [ ] Export outbox: no PENDING jobs older than 5 min.
- [ ] Policy version review — every activation is an audited, evidenced event.

## Monthly (audit cycle)
- [ ] Run recertification campaign; record KEEP/REVOKE per grant.
- [ ] Create export bundles per scope; verify HMAC; archive off-box with the manifest.
- [ ] Verify the chain from a **second, independent** client (e.g. download bundle + recompute SHA-256 chain).

## Incident: evidence tamper detected
1. Note `brokenSeq` and both hashes from the verify response.
2. Freeze writes (application is read-only in degraded mode via health-based routing).
3. Pull DB snapshot + application logs with correlation ids around `occurredAt` of the broken entry.
4. Restore ledger from the last known-good backup; replay domain events post-restore.
5. File a security incident; the audit trail of who/when is in `audit_log`.

## Retention
- `access_event`: purge after `app.retention.access-events-days` (90) via the scheduled idempotency/event purge (runbook job).
- Export files: delete after `app.retention.export-files-days` (30) — they are reproducible from the ledger.
- `evidence_entry`: **never delete** — archival policy is append-only; archive full-table snapshots to WORM storage monthly.
