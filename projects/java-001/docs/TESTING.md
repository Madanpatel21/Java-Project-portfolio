# JAVA-001 — Testing Guide

```bash
mvn test                      # unit + integration (H2 pg-mode; Testcontainers-PG test auto-skips without Docker)
mvn verify                    # same + package
mvn verify -Pstatic-analysis  # adds Checkstyle + SpotBugs (Max effort, Medium threshold)
mvn verify -Psecurity-scan    # adds OWASP dependency-check (CI; needs NVD feed)
jmeter -n -t jmeter/workforce-compliance.jmx -JTOKEN=<bearer>   # smoke/load (50 threads, 120s)
```

## Suite inventory (43 tests)

| Class | Kind | What it proves |
|---|---|---|
| HashChainTest | unit | determinism, clean-chain verify, payload tamper, link tamper, genesis |
| RuleEvaluatorsTest | unit | each of the 5 rule evaluators (positive + negative cases) |
| MaskingSerializerTest | unit | PII masking at serialization |
| LoginAttemptServiceTest | unit | lockout after 5, reset on success, window expiry |
| EventIngestServiceTest | unit (mock) | publish, idempotent replay, source-dedup 409 |
| ViolationServiceTest | unit (mock) | evidence linkage, dedup, controlled lifecycle transitions |
| EvidenceChainIT | integration | appends, clean verify, tampered payload → DOWN health, sequence gap |
| ComplianceCorrelationIT | integration | all 5 rule families detected, rerun dedup, remediation flow |
| AccessFlowIT | integration | full dual-control flow, SoD blocks, replay safety, RFC 7807 validation, revoke |
| SecurityIT | security | authN matrix, forged token, escalation per role, auditor read-only, SQLi attempts, lockout, replay, policy gate |
| ExportServiceTest | integration | signed bundle, tamper detection, idempotency, invalid range |
| PostgreSQLMigrationIT | integration (Testcontainers) | Flyway V1+V2 apply on real PostgreSQL 16 (skipped without Docker; runs in CI) |
