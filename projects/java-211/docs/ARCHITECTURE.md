# JAVA-211 — Architecture Overview

## 1. Style and rationale

**Modular monolith** — one deployable, package-isolated bounded contexts. The stewardship loop
(prescription → review → intervention → metrics) is one strongly-consistent clinical unit;
splitting it across services would force distributed transactions across therapy changes and
audit records for zero scaling benefit. The event bus abstraction is a real seam (RabbitMQ in the
local profile) that allows extracting the analytics consumers later.

## 2. Module map

```
patients        registry, admissions (metric denominator), lab values (renal dosing)
catalog         antimicrobial formulary (WHO DDD, spectrum, coverage tags, costs)
guidelines      versioned guideline sets + RenalCalculator (Cockcroft-Gault) + rule engine
prescriptions   order lifecycle (PENDING_AUTHORIZATION → ACTIVE → STOPPED/EXPIRED/COMPLETED)
restricted      time-boxed ID-physician pre-authorization (72h default)
reviews         review tasks (TIME_BASED | CULTURE_RESULT | DRUG_BUG_MISMATCH | DE_ESCALATION_CANDIDATE | REDUNDANT_COVERAGE)
interventions   pharmacist proposals; prescriber accept (applies change) / reject (reason required)
microbiology    cultures, isolates, CLSI-style S/I/R susceptibility
antibiogram     first-isolate dedup (7d), S/I/R percentages, 30-isolate reporting gate
metrics         DOT / patient-days / DOT-per-1000 / DDD / acceptance rates
messaging       bus abstraction (Direct | RabbitMQ+DLX), typed handlers, dispatcher
common          correlation ids, RFC 7807, idempotency, PHI masking, audit log, rate limiting
```

## 3. Key flows

### Prescription → restricted pre-authorization
Restricted drug order → `PENDING_AUTHORIZATION` + authorization request → ID physician approves
(time-boxed 72h) → `RestrictedAuthApproved` event activates the prescription → expiry sweep stops
therapy and cancels open review tasks.

### Culture → drug-bug mismatch alert
Culture reported → `CultureReported` event → for every ACTIVE prescription of the patient the
rule engine re-evaluates against the new susceptibility rows → an **R** result against the current
drug creates a CRITICAL `DRUG_BUG_MISMATCH` review task (deduplicated per trigger) and a
`DRUG_BUG_MISMATCH_ALERT` audit entry. A narrower-spectrum **S** agent yields a
`DE_ESCALATION_CANDIDATE` task.

### Intervention lifecycle
Pharmacist evaluates (`GET /stewardship/evaluate/{rxId}`) → proposes (idempotent) → prescriber
accepts → the therapy change is applied **in the same transaction** as the decision record;
rejection requires a clinical reason. Stale proposals expire after 2 days.

## 4. Clinical calculation correctness

| Computation | Rule |
|---|---|
| DOT | distinct calendar days per prescription inside [startAt, stopAt) ∩ window (UTC dates) |
| Patient-days | distinct calendar days of admission overlap with the window |
| DOT/1000 | DOT × 1000 ÷ patient-days, per ward, rounded 0.1 |
| DDD | doseMg × (24/freqH) ÷ 1000 ÷ drug.dddGrams × therapy days |
| CrCl | Cockcroft-Gault: ((140−age)×weight)/(72×serumCr), ×0.85 female |
| Antibiogram | first isolate per patient+organism within rolling 7 days; %S = S/(S+I+R); rows < 30 isolates not reportable |

## 5. Concurrency and failure handling

| Concern | Mechanism |
|---|---|
| Duplicate orders/interventions | Idempotency-Key records (REQUIRES_NEW claim, unique constraint) |
| Duplicate review tasks | dedup on (prescription, OPEN, trigger) |
| Prescription races | `@Version` optimistic locking; state checks inside transactions |
| Broker down (local profile) | resilience4j retry on publish; dev profile unaffected |
| Culture re-report | 409 conflict — reports are final |
| Scheduler failures | cron jobs log and retry next tick; scans are idempotent |
| Task cancellation | stopping a prescription cancels its OPEN tasks via event |
