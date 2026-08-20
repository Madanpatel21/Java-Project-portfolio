-- Capacity & Shift Rostering Optimizer — project schema.
CREATE TABLE employees (
  id                VARCHAR(36)  PRIMARY KEY,
  user_id           VARCHAR(36),
  emp_no            VARCHAR(20)  NOT NULL UNIQUE,
  name              VARCHAR(120) NOT NULL,
  department        VARCHAR(64)  NOT NULL,
  skills            VARCHAR(500) NOT NULL,
  employment_type   VARCHAR(16)  NOT NULL,
  max_weekly_hours  INT          NOT NULL DEFAULT 40,
  active            BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at        TIMESTAMP    NOT NULL
);

CREATE TABLE availabilities (
  id           VARCHAR(36) PRIMARY KEY,
  employee_id  VARCHAR(36) NOT NULL,
  avail_date   DATE        NOT NULL,
  reason       VARCHAR(120),
  created_at   TIMESTAMP   NOT NULL
);
CREATE INDEX idx_avail_employee ON availabilities (employee_id, avail_date);

CREATE TABLE rosters (
  id          VARCHAR(36)  PRIMARY KEY,
  name        VARCHAR(120) NOT NULL,
  department  VARCHAR(64)  NOT NULL,
  start_date  DATE         NOT NULL,
  end_date    DATE         NOT NULL,
  status      VARCHAR(16)  NOT NULL,
  score_json  VARCHAR(4000),
  published_at TIMESTAMP,
  created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE shifts (
  id               VARCHAR(36) PRIMARY KEY,
  roster_id        VARCHAR(36) NOT NULL,
  shift_date       DATE        NOT NULL,
  shift_type       VARCHAR(16) NOT NULL,
  start_hour       INT         NOT NULL,
  duration_hours   INT         NOT NULL,
  required_skill   VARCHAR(40) NOT NULL,
  required_headcount INT       NOT NULL
);
CREATE INDEX idx_shifts_roster ON shifts (roster_id, shift_date);

CREATE TABLE shift_assignments (
  id           VARCHAR(36) PRIMARY KEY,
  roster_id    VARCHAR(36) NOT NULL,
  shift_id     VARCHAR(36) NOT NULL,
  employee_id  VARCHAR(36),
  status       VARCHAR(20) NOT NULL,
  assigned_at  TIMESTAMP
);
CREATE INDEX idx_assign_roster ON shift_assignments (roster_id, employee_id);

CREATE TABLE roster_rules (
  id          VARCHAR(36)  PRIMARY KEY,
  code        VARCHAR(40)  NOT NULL UNIQUE,
  name        VARCHAR(160) NOT NULL,
  level       VARCHAR(16)  NOT NULL,
  threshold   INT          NOT NULL,
  weight      INT          NOT NULL,
  active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE swap_requests (
  id                VARCHAR(36)  PRIMARY KEY,
  swap_no           VARCHAR(32)  NOT NULL UNIQUE,
  assignment_id     VARCHAR(36)  NOT NULL,
  requested_by      VARCHAR(36)  NOT NULL,
  target_employee_id VARCHAR(36) NOT NULL,
  reason            VARCHAR(300),
  status            VARCHAR(16)  NOT NULL,
  reviewed_by       VARCHAR(64),
  reviewed_at       TIMESTAMP,
  created_at        TIMESTAMP    NOT NULL
);
CREATE INDEX idx_swaps_status ON swap_requests (status);
