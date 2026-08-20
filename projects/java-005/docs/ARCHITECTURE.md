# JAVA-005 — Architecture Overview

**Modular monolith** with a pure-engine core. Definition versioning, instance advancement and task
lifecycle are one transactional unit — BPM integrity requires it. The bus abstraction (RabbitMQ in
`local`) is the seam for downstream consumers (notification, ERP callbacks).

## Modules
```
engine     WorkflowModel (definition parser/validator), WorkflowEngine (pure interpreter →
           action plans), ExpressionEvaluator (gateway conditions)
domain     WorkflowDefinition (versioned), WorkflowInstance (pinned to version),
           WorkflowTask (SLA), WorkflowStep (append-only execution trace)
service    WorkflowService — applies plans transactionally; cancel+compensation; timers; escalation
api        REST controllers (RBAC + idempotency)
messaging  bus abstraction (Direct | RabbitMQ + DLX)
```

## Key flows
- **Definition** → model validated at creation; new version deprecates the previous.
- **Start** → instance pinned to definition id; engine advances from START.
- **Task completion** → result may carry variable updates; engine routes via GATEWAY conditions.
- **Timer** → instance suspends (WAITING_TIMER); scheduler resumes.
- **Escalation** → overdue PENDING tasks get a renewed SLA + `TaskEscalated` event.
- **Cancel** → skip pending tasks, then create reverse-order compensation tasks.

## Concurrency and failure handling
| Concern | Mechanism |
|---|---|
| Duplicate starts/completions | Idempotency-Key; unique (definition, businessKey) |
| Instance races | @Version optimistic locking |
| Task double-completion | status guard + idempotency replay |
| Timer re-fires | status transition guarded (only WAITING_TIMER resumes) |
| Broker down | resilience4j retry; dev profile unaffected |
