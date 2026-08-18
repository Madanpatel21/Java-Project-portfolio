-- =====================================================================
-- JAVA-001 Workforce Compliance Evidence Platform — initial schema
-- Portable across PostgreSQL 16 and H2 (PostgreSQL compatibility mode):
--   * VARCHAR instead of TEXT, VARCHAR(36) UUIDs, BIGSERIAL sequences
--   * TIMESTAMP columns (Hibernate 6 maps java.time.Instant)
-- =====================================================================

-- ---- Identity -------------------------------------------------------
CREATE TABLE users (
  id              VARCHAR(36) PRIMARY KEY,
  username        VARCHAR(64)  NOT NULL UNIQUE,
  email           VARCHAR(255) NOT NULL,
  org_unit        VARCHAR(64)  NOT NULL,
  cert_expires_at TIMESTAMP,
  status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
  created_at      TIMESTAMP    NOT NULL
);

CREATE TABLE user_roles (
  user_id   VARCHAR(36) NOT NULL,
  role_name VARCHAR(64) NOT NULL,
  PRIMARY KEY (user_id, role_name)
);

-- Local identity provider store (dev/fallback; Argon2id hashes)
CREATE TABLE local_users (
  id             VARCHAR(36)  PRIMARY KEY,
  username       VARCHAR(64)  NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  email          VARCHAR(255) NOT NULL,
  org_unit       VARCHAR(64)  NOT NULL,
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

-- ---- Policy (versioned rules) --------------------------------------
CREATE TABLE policy (
  id                VARCHAR(36)  PRIMARY KEY,
  code              VARCHAR(64)  NOT NULL UNIQUE,
  name              VARCHAR(120) NOT NULL,
  description       VARCHAR(1000),
  active_version_id VARCHAR(36),
  created_at        TIMESTAMP    NOT NULL
);

CREATE TABLE policy_version (
  id             VARCHAR(36)  PRIMARY KEY,
  policy_id      VARCHAR(36)  NOT NULL,
  version_no     INT          NOT NULL,
  rules_json     VARCHAR(8192) NOT NULL,
  status         VARCHAR(16)  NOT NULL,
  effective_from TIMESTAMP    NOT NULL,
  created_by     VARCHAR(120) NOT NULL,
  created_at     TIMESTAMP    NOT NULL,
  UNIQUE (policy_id, version_no)
);

-- ---- Access lifecycle ----------------------------------------------
CREATE TABLE access_request (
  id              VARCHAR(36)  PRIMARY KEY,
  requester_id    VARCHAR(36)  NOT NULL,
  subject_user_id VARCHAR(36)  NOT NULL,
  resource_type   VARCHAR(64)  NOT NULL,
  resource_name   VARCHAR(120) NOT NULL,
  roles_json      VARCHAR(1024) NOT NULL,
  justification   VARCHAR(2000),
  status          VARCHAR(24)  NOT NULL,
  created_at      TIMESTAMP    NOT NULL,
  decided_at      TIMESTAMP,
  decided_by      VARCHAR(120),
  decision_note   VARCHAR(1000)
);

CREATE TABLE approval (
  id               VARCHAR(36)  PRIMARY KEY,
  access_request_id VARCHAR(36) NOT NULL,
  approver_id      VARCHAR(36)  NOT NULL,
  approver_name    VARCHAR(120) NOT NULL,
  decision         VARCHAR(16)  NOT NULL,
  comment          VARCHAR(1000),
  decided_at       TIMESTAMP    NOT NULL
);

CREATE TABLE access_grant (
  id             VARCHAR(36)  PRIMARY KEY,
  user_id        VARCHAR(36)  NOT NULL,
  resource_type  VARCHAR(64)  NOT NULL,
  resource_name  VARCHAR(120) NOT NULL,
  roles_json     VARCHAR(1024) NOT NULL,
  status         VARCHAR(16)  NOT NULL,
  granted_at     TIMESTAMP    NOT NULL,
  expires_at     TIMESTAMP,
  revoked_at     TIMESTAMP,
  revoked_by     VARCHAR(120),
  revoke_reason  VARCHAR(1000),
  recert_due_at  TIMESTAMP,
  recertified_at TIMESTAMP,
  version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE access_event (
  id            VARCHAR(36)  PRIMARY KEY,
  user_id       VARCHAR(36)  NOT NULL,
  resource_name VARCHAR(120) NOT NULL,
  event_type    VARCHAR(32)  NOT NULL,
  ip_address    VARCHAR(64),
  source        VARCHAR(64)  NOT NULL,
  external_id   VARCHAR(128),
  occurred_at   TIMESTAMP    NOT NULL,
  UNIQUE (source, external_id)
);

-- ---- Evidence (hash-chained ledger) ---------------------------------
CREATE TABLE evidence_entry (
  seq            BIGSERIAL PRIMARY KEY,
  aggregate_type VARCHAR(64)  NOT NULL,
  aggregate_id   VARCHAR(64)  NOT NULL,
  event_type     VARCHAR(64)  NOT NULL,
  actor          VARCHAR(120) NOT NULL,
  payload        VARCHAR(8192) NOT NULL,
  prev_hash      VARCHAR(64)  NOT NULL,
  hash           VARCHAR(64)  NOT NULL,
  occurred_at    TIMESTAMP    NOT NULL
);

-- ---- Compliance violations ------------------------------------------
CREATE TABLE violation (
  id               VARCHAR(36)  PRIMARY KEY,
  user_id          VARCHAR(36)  NOT NULL,
  policy_code      VARCHAR(64)  NOT NULL,
  rule_type        VARCHAR(64)  NOT NULL,
  severity         VARCHAR(16)  NOT NULL,
  status           VARCHAR(16)  NOT NULL,
  description      VARCHAR(2000) NOT NULL,
  evidence_seq     BIGINT,
  detected_at      TIMESTAMP    NOT NULL,
  acknowledged_at  TIMESTAMP,
  remediated_at    TIMESTAMP,
  closed_at        TIMESTAMP,
  remediation_note VARCHAR(2000),
  notified_at      TIMESTAMP
);

-- ---- Recertification campaigns --------------------------------------
CREATE TABLE recert_campaign (
  id           VARCHAR(36)  PRIMARY KEY,
  name         VARCHAR(120) NOT NULL,
  window_start TIMESTAMP    NOT NULL,
  window_end   TIMESTAMP    NOT NULL,
  status       VARCHAR(16)  NOT NULL,
  generated_by VARCHAR(120) NOT NULL,
  generated_at TIMESTAMP    NOT NULL
);

CREATE TABLE recert_decision (
  id          VARCHAR(36)  PRIMARY KEY,
  campaign_id VARCHAR(36)  NOT NULL,
  grant_id    VARCHAR(36)  NOT NULL,
  decided_by  VARCHAR(120) NOT NULL,
  decision    VARCHAR(16)  NOT NULL,
  decided_at  TIMESTAMP    NOT NULL
);

-- ---- Audit / exports / idempotency ----------------------------------
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

CREATE TABLE export_job (
  id            VARCHAR(36)  PRIMARY KEY,
  requested_by  VARCHAR(120) NOT NULL,
  scope_user_id VARCHAR(36),
  range_from    TIMESTAMP    NOT NULL,
  range_to      TIMESTAMP    NOT NULL,
  status        VARCHAR(16)  NOT NULL,
  start_seq     BIGINT,
  end_seq       BIGINT,
  file_path     VARCHAR(500),
  hmac          VARCHAR(128),
  error         VARCHAR(2000),
  created_at    TIMESTAMP    NOT NULL,
  completed_at  TIMESTAMP
);

CREATE TABLE idempotency_record (
  id              VARCHAR(36)  PRIMARY KEY,
  idem_key        VARCHAR(200) NOT NULL UNIQUE,
  resource_type   VARCHAR(64)  NOT NULL,
  resource_id     VARCHAR(64),
  response_status INT          NOT NULL,
  created_at      TIMESTAMP    NOT NULL
);

-- ---- Indexes ---------------------------------------------------------
CREATE INDEX idx_evidence_agg   ON evidence_entry (aggregate_type, aggregate_id);
CREATE INDEX idx_violation_user ON violation (user_id, status);
CREATE INDEX idx_violation_stat ON violation (status);
CREATE INDEX idx_grant_user     ON access_grant (user_id, status);
CREATE INDEX idx_grant_recert   ON access_grant (status, recert_due_at);
CREATE INDEX idx_event_user     ON access_event (user_id, occurred_at);
CREATE INDEX idx_audit_time     ON audit_log (occurred_at);
CREATE INDEX idx_request_status ON access_request (status);
CREATE INDEX idx_export_status  ON export_job (status);
CREATE INDEX idx_approval_req   ON approval (access_request_id);
