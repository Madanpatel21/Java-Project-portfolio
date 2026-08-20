# JAVA-006 — Troubleshooting
| Symptom | Cause | Fix |
|---|---|---|
| 409 on decision | dual control or already decided | a different approver of the same role must decide |
| 409 on create policy | duplicate policy code or in-flight key | use a new code/key |
| 400 on chain | malformed steps JSON | steps must be numbered 1..N, approversRequired >= 1 |
| Request stuck PENDING | step not satisfied | check the required role/approver count |
