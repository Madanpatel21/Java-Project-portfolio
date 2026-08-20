-- Fleet Maintenance Planning System — project schema.
CREATE TABLE vehicles (
  id                 VARCHAR(36)   PRIMARY KEY,
  vin                VARCHAR(40)   NOT NULL UNIQUE,
  plate              VARCHAR(20)   NOT NULL UNIQUE,
  make               VARCHAR(60)   NOT NULL,
  model              VARCHAR(60)   NOT NULL,
  model_year         INT           NOT NULL,
  category           VARCHAR(24)   NOT NULL,
  status             VARCHAR(24)   NOT NULL,
  current_odometer   INT           NOT NULL DEFAULT 0,
  odometer_updated_at TIMESTAMP,
  service_anchor_odometer INT,
  last_service_date  DATE,
  department         VARCHAR(64),
  driver_name        VARCHAR(120),
  purchase_date      DATE,
  created_at         TIMESTAMP     NOT NULL
);
CREATE INDEX idx_vehicles_status ON vehicles (status);

CREATE TABLE maintenance_plans (
  id                  VARCHAR(36)   PRIMARY KEY,
  code                VARCHAR(40)   NOT NULL UNIQUE,
  name                VARCHAR(120)  NOT NULL,
  applies_to_category VARCHAR(24)   NOT NULL,
  interval_type       VARCHAR(24)   NOT NULL,
  interval_value      INT           NOT NULL,
  compliance_required BOOLEAN       NOT NULL DEFAULT FALSE,
  active              BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at          TIMESTAMP     NOT NULL
);

CREATE TABLE plan_items (
  id             VARCHAR(36)   PRIMARY KEY,
  plan_id        VARCHAR(36)   NOT NULL,
  part_code      VARCHAR(40)   NOT NULL,
  part_name      VARCHAR(120)  NOT NULL,
  quantity       INT           NOT NULL,
  estimated_cost DECIMAL(12,2) NOT NULL
);
CREATE INDEX idx_plan_items_plan ON plan_items (plan_id);

CREATE TABLE maintenance_tasks (
  id                 VARCHAR(36)   PRIMARY KEY,
  task_no            VARCHAR(32)   NOT NULL UNIQUE,
  vehicle_id         VARCHAR(36)   NOT NULL,
  plan_id            VARCHAR(36)   NOT NULL,
  due_type           VARCHAR(16)   NOT NULL,
  due_date           DATE,
  due_odometer       INT,
  status             VARCHAR(24)   NOT NULL,
  priority           VARCHAR(24)   NOT NULL,
  forecast_at        TIMESTAMP,
  work_order_id      VARCHAR(36),
  completed_at       TIMESTAMP,
  created_at         TIMESTAMP     NOT NULL
);
CREATE INDEX idx_tasks_status ON maintenance_tasks (status);
CREATE INDEX idx_tasks_vehicle ON maintenance_tasks (vehicle_id);

CREATE TABLE work_orders (
  id                  VARCHAR(36)   PRIMARY KEY,
  wo_no               VARCHAR(32)   NOT NULL UNIQUE,
  task_id             VARCHAR(36)   NOT NULL,
  vehicle_id          VARCHAR(36)   NOT NULL,
  status              VARCHAR(24)   NOT NULL,
  opened_by           VARCHAR(64)   NOT NULL,
  mechanic            VARCHAR(120),
  labor_hours         DECIMAL(6,2),
  labor_cost          DECIMAL(12,2),
  parts_cost          DECIMAL(12,2),
  notes               VARCHAR(1000),
  shortfall_reason    VARCHAR(500),
  odometer_at_service INT,
  opened_at           TIMESTAMP     NOT NULL,
  completed_at        TIMESTAMP
);
CREATE INDEX idx_wo_status ON work_orders (status);

CREATE TABLE parts (
  id               VARCHAR(36)   PRIMARY KEY,
  part_code        VARCHAR(40)   NOT NULL UNIQUE,
  name             VARCHAR(120)  NOT NULL,
  quantity_on_hand INT           NOT NULL DEFAULT 0,
  reserved_qty     INT           NOT NULL DEFAULT 0,
  reorder_point    INT           NOT NULL DEFAULT 0,
  unit_cost        DECIMAL(12,2) NOT NULL
);

CREATE TABLE part_reservations (
  id              VARCHAR(36)  PRIMARY KEY,
  reservation_no  VARCHAR(32)  NOT NULL UNIQUE,
  work_order_id   VARCHAR(36)  NOT NULL,
  part_code       VARCHAR(40)  NOT NULL,
  quantity        INT          NOT NULL,
  status          VARCHAR(16)  NOT NULL,
  created_at      TIMESTAMP    NOT NULL
);
CREATE INDEX idx_reservations_wo ON part_reservations (work_order_id);

CREATE TABLE inspections (
  id            VARCHAR(36)   PRIMARY KEY,
  inspection_no VARCHAR(32)   NOT NULL UNIQUE,
  vehicle_id    VARCHAR(36)   NOT NULL,
  inspection_type VARCHAR(24) NOT NULL,
  inspector     VARCHAR(120)  NOT NULL,
  result        VARCHAR(16)   NOT NULL,
  notes         VARCHAR(500),
  valid_until   DATE,
  performed_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_inspections_vehicle ON inspections (vehicle_id);

CREATE TABLE odometer_entries (
  id         VARCHAR(36)  PRIMARY KEY,
  vehicle_id VARCHAR(36)  NOT NULL,
  reading    INT          NOT NULL,
  source     VARCHAR(16)  NOT NULL,
  flag       VARCHAR(24),
  recorded_by VARCHAR(64) NOT NULL,
  recorded_at TIMESTAMP   NOT NULL
);
CREATE INDEX idx_odo_vehicle ON odometer_entries (vehicle_id, recorded_at);
