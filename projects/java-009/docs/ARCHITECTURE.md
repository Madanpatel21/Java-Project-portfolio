# ARCHITECTURE — Fleet Maintenance Planning System (JAVA-009)

## Style
Modular monolith on Spring Boot 3.5.3 / Java 21 (virtual threads) with a Quartz
scheduler (RAM job store, single node) driving the forecast engine. Event bus is
in-process by default and upgrades to RabbitMQ with `app.messaging.enabled=true`.

## Core loop
1. **Signals** — drivers/shops submit odometer readings; the calendar advances.
2. **Forecast** — `DueForecastJob` (Quartz, every 30 min or on demand) walks
   ACTIVE vehicles × active plans and computes the next due point:
   - ODOMETER plans: anchor (last completed service or registration anchor) + interval
     vs current odometer → OVERDUE (≤ 0), DUE (≤ 1,500 km), SCHEDULED (≤ 3,000 km).
   - CALENDAR plans: anchor date (last service / purchase) + interval days vs today →
     OVERDUE, DUE (≤ 7 d), SCHEDULED (≤ 14 d).
   - One open task per vehicle+plan; repeated runs update in place (idempotent).
3. **Execution** — a due task becomes a work order; the plan's parts kit is reserved
   from inventory (shortfall → PARTS_HOLD with reasons); start → IN_PROGRESS;
   complete issues parts, records labor/parts costs, advances the vehicle odometer
   and closes the task; reject releases reservations and reopens the task.
4. **Compliance** — inspections PASS/FAIL drive COMPLIANCE_HOLD; the compliance
   report joins vehicles × compliance plans × latest inspection validity.

## Data model
`vehicles` → `maintenance_tasks` (1:N, one open per plan) → `work_orders` (1:1) →
`part_reservations` (1:N) → `parts`; `maintenance_plans` → `plan_items` (kits);
`inspections`, `odometer_entries` (ledgers). Common kit: `local_users`,
`audit_log`, `idempotency_record`.

## Scaling notes
- Single node: RAM job store; forecasts are cheap table scans over ACTIVE vehicles.
- Horizontal: swap RAM store for JDBC-backed Quartz clustering and move forecast
  runs behind ServiceDue events; inventory operations stay transactional per node.
