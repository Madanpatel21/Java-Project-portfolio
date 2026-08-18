# JAVA-211 — Testing Guide

```bash
mvn test                      # unit + integration (H2 pg-mode; Testcontainers PG auto-skips without Docker)
mvn verify                    # same + package
mvn verify -Pstatic-analysis  # adds Checkstyle + SpotBugs (Max effort, Medium threshold)
```

## Suite inventory (30 tests)

| Class | Kind | Proves |
|---|---|---|
| RenalCalculatorTest | unit | Cockcroft-Gault (male/female factor, impairment, missing data) |
| StewardshipRuleEngineTest | unit | all 6 rule families: duration per-indication, IV→PO (oral-form gate), renal threshold, drug-bug R=CRITICAL, de-escalation, redundant coverage, review trigger window |
| UtilizationMetricsTest | unit | calendar-day DOT clipping, stop-day exclusivity, patient-days, per-1000 math |
| AntibiogramServiceTest | unit | S/I/R percentages, 30-isolate gate, 7-day first-isolate dedup |
| InterventionServiceTest | unit | propose, reject-requires-reason, accept-applies-change, double-decision 409, unknown type |
| PrescriptionFlowIT | integration | restricted pre-auth → ACTIVE, idempotent replay, IV→PO accepted → route changed, rejection blocked |
| CultureAlertIT | integration | R-isolate → DRUG_BUG_MISMATCH task; evaluation endpoint findings |
| MetricsIT | integration | DOT/patient-days per ward, ward filter, invalid window, DDD math |
| SecurityIT | security | role matrix, escalation, PHI masking, SQLi, lockout, replay, invalid susceptibility |
| PostgreSQLMigrationIT | Testcontainers | V1+V2 apply on real PostgreSQL 16 (CI) |

### Load/smoke
`jmeter -n -t jmeter/stewardship.jmx -JTOKEN=<bearer>` — 50 threads / 120 s.
