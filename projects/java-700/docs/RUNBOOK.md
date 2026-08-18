# JAVA-700 — Operational Runbook

## Daily
- [ ] Grafana: ledger appends, open registrations, dedup candidates.
- [ ] Triage OPEN dedup candidates (admin) — these are potential identity fraud.

## Weekly
- [ ] Verify the global chain from a second, independent client.
- [ ] Review REJECTED registrations for procedural issues.
- [ ] Confirm certificate revocation list against the ledger.

## Incident: ledger tamper detected
1. Note `brokenSeq` + both hashes from `/api/v1/ledger/verify`.
2. Freeze writes; the health indicator already reports DOWN.
3. Restore from the last known-good backup; replay domain events post-restore.
4. File a security incident — the audit log has who/when with correlation ids.

## Retention
- `life_events`: **never delete** — this is the legal record. Archive snapshots to WORM storage.
- Idempotency records: auto-purged daily.
- Certificates: revoked certificates are retained permanently (verification must show REVOKED).
