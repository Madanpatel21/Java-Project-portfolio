# JAVA-004 — Operational Runbook

## Daily
- [ ] Grafana: quarantined count, dispositions, holds applied.
- [ ] Triage REVIEW-class documents reaching retention.

## Weekly
- [ ] Sample disposition proofs vs content hashes (independent re-verification).

## Incident: document disposed prematurely
1. The disposition proof records the hash + executor + class; the content was deleted.
2. Restore from the backup tier of the content store (outside the vault).
3. Re-upload as a corrected record; the original proof remains (append-only).
