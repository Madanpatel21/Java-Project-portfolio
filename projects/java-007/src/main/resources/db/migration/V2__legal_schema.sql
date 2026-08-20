-- =====================================================================
-- JAVA-007 Legal Matter & Conflict Intelligence — domain schema
-- =====================================================================

CREATE TABLE parties (
  id              VARCHAR(36)  PRIMARY KEY,
  name            VARCHAR(200) NOT NULL,
  normalized_name VARCHAR(200) NOT NULL,
  party_type      VARCHAR(16)  NOT NULL,   -- CLIENT | OPPONENT | RELATED
  active          BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMP    NOT NULL
);

CREATE TABLE matters (
  id            VARCHAR(36)  PRIMARY KEY,
  matter_no     VARCHAR(40)  NOT NULL UNIQUE,
  name          VARCHAR(200) NOT NULL,
  status        VARCHAR(16)  NOT NULL,     -- OPEN | CLOSED
  client_party_id VARCHAR(36) NOT NULL,
  practice_area VARCHAR(64),
  opened_at     TIMESTAMP    NOT NULL,
  closed_at     TIMESTAMP,
  version       BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE matter_parties (
  id        VARCHAR(36) PRIMARY KEY,
  matter_id VARCHAR(36) NOT NULL,
  party_id  VARCHAR(36) NOT NULL,
  role      VARCHAR(16) NOT NULL,          -- CLIENT | OPPOSING | ADVERSE | WITNESS
  UNIQUE (matter_id, party_id, role)
);

CREATE TABLE conflict_checks (
  id             VARCHAR(36)  PRIMARY KEY,
  requested_by   VARCHAR(120) NOT NULL,
  checked_at     TIMESTAMP    NOT NULL,
  subject_name   VARCHAR(200) NOT NULL,
  adverse_names  VARCHAR(2000) NOT NULL,
  result         VARCHAR(16)  NOT NULL,    -- CLEAR | POTENTIAL | CONFLICT
  details_json   VARCHAR(4000) NOT NULL
);

CREATE TABLE deadline_rules (
  id          VARCHAR(36)  PRIMARY KEY,
  event_type  VARCHAR(32)  NOT NULL,       -- RESPONSE_DUE | DISCOVERY | APPEAL | HEARING
  jurisdiction VARCHAR(32) NOT NULL,
  days_offset INT          NOT NULL,       -- positive = after trigger, negative = before
  active      BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE matter_deadlines (
  id           VARCHAR(36)  PRIMARY KEY,
  matter_id    VARCHAR(36)  NOT NULL,
  event_type   VARCHAR(32)  NOT NULL,
  jurisdiction VARCHAR(32)  NOT NULL,
  trigger_date DATE         NOT NULL,
  due_at       DATE         NOT NULL,
  status       VARCHAR(16)  NOT NULL,      -- OPEN | COMPLETED | MISSED | WAIVED
  completed_at TIMESTAMP,
  completed_by VARCHAR(120),
  version      BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE ethical_walls (
  id        VARCHAR(36) PRIMARY KEY,
  matter_id VARCHAR(36) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  UNIQUE (matter_id, role_name)
);

CREATE INDEX idx_party_normalized ON parties (normalized_name);
CREATE INDEX idx_mp_matter        ON matter_parties (matter_id);
CREATE INDEX idx_mp_party         ON matter_parties (party_id);
CREATE INDEX idx_deadline_status  ON matter_deadlines (status, due_at);
CREATE INDEX idx_deadline_matter  ON matter_deadlines (matter_id);
