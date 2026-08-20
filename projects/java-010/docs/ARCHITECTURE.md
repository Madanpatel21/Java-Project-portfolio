# ARCHITECTURE — Capacity & Shift Rostering Optimizer (JAVA-010)

## Style
Modular monolith (Spring Boot 3.5.3 / Java 21) with an embedded constraint
programming solver (Timefold 1.16). Event bus in-process by default; RabbitMQ via
`app.messaging.enabled=true`.

## Solving model
- `PlannedShift` (@PlanningEntity): one per demand headcount unit; carries shift
  date/type/hours/skill; the planning variable is the assigned `Employee`.
- `RosterSolution` (@PlanningSolution): employees + availabilities as problem
  facts; `PlannedShift` collection; HardSoftScore.
- `RosterConstraintProvider`: 7 hard constraints (skill, availability, one shift
  per day, weekly hours, 11h NIGHT→MORNING rest, max 6 days/week, night cap 4)
  and 2 soft (coverage −10/slot, fairness −hours²).

## Lifecycle
1. Manager posts a demand curve → roster DRAFT + materialized shift rows +
   UNASSIGNED assignment rows.
2. `POST /rosters/{id}/optimize` builds the problem, solves with a per-environment
   time limit (dev 5s, test 3s), persists the assignment set and a constraint
   breakdown JSON on the roster.
3. `GET /rosters/{id}/explain` re-derives the score via `SolutionManager.explain`
   → per-constraint match totals (auditor evidence).
4. Publish requires score present AND zero unassigned slots.
5. Swaps: employee requests → manager decides; approval re-validates skill,
   availability and double-booking, then moves assignments (two-sided exchange
   when the target works the same day).

## Data model
`rosters` → `shifts` → `shift_assignments` (employee nullable until solved);
`employees` (skills CSV, weekly caps), `availabilities`, `roster_rules`
(data-driven rule catalogue mirroring the constraints), `swap_requests`.
Common kit: `local_users`, `audit_log`, `idempotency_record`.

## Determinism & scaling
Solver runs in REPRODUCIBLE mode (fixed seed). For larger fleets, raise the time
limit or move to partitioned per-department solves; persistence stays transactional.
