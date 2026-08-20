-- Kit common schema (local IdP, audit, idempotency). Project-specific tables go in V2+.
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
CREATE INDEX idx_audit_time ON audit_log (occurred_at);
