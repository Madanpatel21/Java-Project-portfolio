-- =====================================================================
-- JAVA-700 Digital ID & Civil Registry (CRVS) — initial schema
-- Portable across PostgreSQL 16 and H2 (PostgreSQL compatibility mode).
-- =====================================================================

-- ---- Registry offices (district offices that capture registrations) ---
CREATE TABLE offices (
  id     VARCHAR(36)  PRIMARY KEY,
  code   VARCHAR(16)  NOT NULL UNIQUE,
  name   VARCHAR(120) NOT NULL,
  region VARCHAR(64)  NOT NULL
);

-- ---- Persons (lifetime identity records) --------------------------------
CREATE TABLE persons (
  id              VARCHAR(36)  PRIMARY KEY,
  national_id     VARCHAR(16)  NOT NULL UNIQUE,
  full_name       VARCHAR(160) NOT NULL,
  dob             DATE         NOT NULL,
  sex             VARCHAR(8)   NOT NULL,
  place_of_birth  VARCHAR(120) NOT NULL,
  parent_names    VARCHAR(300),
  region          VARCHAR(64)  NOT NULL,
  status          VARCHAR(16)  NOT NULL,     -- ACTIVE | DECEASED
  registered_at   TIMESTAMP    NOT NULL,
  deceased_at     TIMESTAMP
);

-- ---- Registrations (four-eyes life-event capture) ------------------------
CREATE TABLE registrations (
  id              VARCHAR(36)  PRIMARY KEY,
  type            VARCHAR(16)  NOT NULL,     -- BIRTH | MARRIAGE | DEATH | CORRECTION
  person_id       VARCHAR(36),
  spouse_person_id VARCHAR(36),
  payload_json    VARCHAR(4096) NOT NULL,
  status          VARCHAR(16)  NOT NULL,     -- PENDING | APPROVED | REJECTED
  office_id       VARCHAR(36)  NOT NULL,
  registrar_id    VARCHAR(36)  NOT NULL,
  registrar_name  VARCHAR(120) NOT NULL,
  supervisor_id   VARCHAR(36),
  supervisor_name VARCHAR(120),
  decided_at      TIMESTAMP,
  decision_note   VARCHAR(1000),
  created_at      TIMESTAMP    NOT NULL
);

-- ---- Dual-chained life-event ledger --------------------------------------
-- Every approved life event appends ONE entry that links BOTH the global
-- registry chain and the per-person chain.
CREATE TABLE life_events (
  global_seq       BIGSERIAL PRIMARY KEY,
  person_id        VARCHAR(36)  NOT NULL,
  event_type       VARCHAR(32)  NOT NULL,    -- BIRTH | MARRIAGE | DEATH | AMENDMENT | CERTIFICATE_ISSUED | CERTIFICATE_REVOKED
  payload          VARCHAR(8192) NOT NULL,
  actor            VARCHAR(120) NOT NULL,
  occurred_at      TIMESTAMP    NOT NULL,
  prev_global_hash VARCHAR(64)  NOT NULL,
  global_hash      VARCHAR(64)  NOT NULL,
  chain_seq        BIGINT       NOT NULL,    -- per-person sequence
  prev_chain_hash  VARCHAR(64)  NOT NULL,
  chain_hash       VARCHAR(64)  NOT NULL
);

-- ---- Certificates ----------------------------------------------------------
CREATE TABLE certificates (
  id           VARCHAR(36)  PRIMARY KEY,
  person_id    VARCHAR(36)  NOT NULL,
  type         VARCHAR(16)  NOT NULL,        -- BIRTH | MARRIAGE | DEATH
  token        VARCHAR(32)  NOT NULL UNIQUE, -- verification token (QR-ready)
  content_hash VARCHAR(64)  NOT NULL,
  status       VARCHAR(16)  NOT NULL,        -- VALID | REVOKED
  issued_at    TIMESTAMP    NOT NULL,
  issued_by    VARCHAR(120) NOT NULL,
  revoked_at   TIMESTAMP,
  revoked_by   VARCHAR(120),
  revoke_reason VARCHAR(500)
);

-- ---- Duplicate-identity candidates ------------------------------------------
CREATE TABLE dedup_candidates (
  id           VARCHAR(36)  PRIMARY KEY,
  person_a_id  VARCHAR(36)  NOT NULL,
  person_b_id  VARCHAR(36)  NOT NULL,
  score        DECIMAL(5,4) NOT NULL,
  status       VARCHAR(16)  NOT NULL,        -- OPEN | CONFIRMED | DISMISSED
  created_at   TIMESTAMP    NOT NULL,
  decided_by   VARCHAR(120),
  decided_at   TIMESTAMP
);

-- ---- Local identity provider ------------------------------------------------
CREATE TABLE local_users (
  id             VARCHAR(36)  PRIMARY KEY,
  username       VARCHAR(64)  NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  email          VARCHAR(255) NOT NULL,
  office_id      VARCHAR(36),
  failed_attempts INT         NOT NULL DEFAULT 0,
  locked_until   TIMESTAMP,
  enabled        BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at     TIMESTAMP    NOT NULL
);

CREATE TABLE local_user_roles (
  user_id   VARCHAR(36) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role_name)
);

-- ---- Audit / idempotency ------------------------------------------------------
CREATE TABLE audit_log (
  id             VARCHAR(36)  PRIMARY KEY,
  occurred_at    TIMESTAMP    NOT NULL,
  principal      VARCHAR(120),
  action         VARCHAR(64)  NOT NULL,
  target_type    VARCHAR(64),
  target_id      VARCHAR(64),
  detail         VARCHAR(2000),
  correlation_id VARCHAR(64)
);

CREATE TABLE idempotency_record (
  id              VARCHAR(36)  PRIMARY KEY,
  idem_key        VARCHAR(200) NOT NULL UNIQUE,
  resource_type   VARCHAR(64)  NOT NULL,
  resource_id     VARCHAR(64),
  response_status INT          NOT NULL,
  created_at      TIMESTAMP    NOT NULL
);

-- ---- Indexes -------------------------------------------------------------------
CREATE INDEX idx_person_national  ON persons (national_id);
CREATE INDEX idx_person_name      ON persons (full_name);
CREATE INDEX idx_person_status    ON persons (status, region);
CREATE INDEX idx_registration_st  ON registrations (status, office_id);
CREATE INDEX idx_event_person     ON life_events (person_id, chain_seq);
CREATE INDEX idx_event_type       ON life_events (event_type, occurred_at);
CREATE INDEX idx_cert_token       ON certificates (token);
CREATE INDEX idx_dedup_status     ON dedup_candidates (status);
CREATE INDEX idx_audit_time       ON audit_log (occurred_at);
