# JAVA-005 — Testing Guide
```bash
mvn test                      # 23 tests
mvn verify -Pstatic-analysis  # + Checkstyle + SpotBugs (Max effort)
```
Suites: WorkflowEngineTest (8) · ExpressionEvaluatorTest (4) · WorkflowFlowIT (4) ·
SecurityIT (6) · PostgreSQLMigrationIT (CI).
