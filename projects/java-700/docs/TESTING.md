# JAVA-700 — Testing Guide

```bash
mvn test                      # unit + integration (H2 pg-mode; Testcontainers PG auto-skips without Docker)
mvn verify                    # same + package
mvn verify -Pstatic-analysis  # adds Checkstyle + SpotBugs (Max effort, Medium threshold)
jmeter -n -t jmeter/crvs.jmx -JTOKEN=<bearer>
```

## Suite inventory (37 tests)

| Class | Kind | Proves |
|---|---|---|
| NationalIdGeneratorTest | unit | 10-digit format, checksum validation, tamper detection, sequencing |
| DedupEngineTest | unit | Jaro-Winkler scoring, blocking keys, parent overlap, threshold filtering, normalization |
| HashChainTest | unit | global + per-person chains, payload tamper, sequence gaps, canonical JSON |
| VitalStatisticsTest | unit | per-region aggregation, natural increase, window filtering |
| LifeEventFlowIT | integration | four-eyes birth (SoD 403 + dual-role 409), idempotent replay, national ID, chain verification, death propagation, marriage rules (duplicate + deceased blocked), certificate issue/verify/revoke, correction amendments preserving originals, dedup candidate raising |
| SecurityIT | security | role matrix, escalation, masking, SQLi, lockout, invalid national ID |
| PostgreSQLMigrationIT | Testcontainers | V1 applies on real PostgreSQL 16 (CI) |
