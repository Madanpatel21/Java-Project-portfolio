-- =====================================================================
-- JAVA-006 Audit-Grade Approval & Policy Chain Engine — domain schema
-- =====================================================================

CREATE TABLE policies (
  id                VARCHAR(36)  PRIMARY KEY,
  policy_code       VARCHAR(64)  NOT NULL UNIQUE,
  name              VARCHAR(160) NOT NULL,
  description       VARCHAR(1000),
  active_version_id VARCHAR(36),
  created_at        TIMESTAMP    NOT NULL
);

CREATE TABLE policy_versions (
  id             VARCHAR(36)  PRIMARY KEY,
  policy_id      VARCHAR(36)  NOT NULL,
  version_no     INT          NOT NULL,
  rules_json     VARCHAR(8192) NOT NULL,
  status         VARCHAR(16)  NOT NULL,   -- ACTIVE | SUPERSEDED
  effective_from TIMESTAMP    NOT NULL,
  created_by     VARCHAR(120) NOT NULL,
  created_at     TIMESTAMP    NOT NULL,
  UNIQUE (policy_id, version_no)
);

CREATE TABLE approval_chains (
  id        VARCHAR(36)  PRIMARY KEY,
  chain_code VARCHAR(64) NOT NULL UNIQUE,
  name      VARCHAR(160) NOT NULL,
  steps_json VARCHAR(4096) NOT NULL,       -- [{"step":1,"role":"MANAGER","approversRequired":1},...]
  created_at TIMESTAMP   NOT NULL
);

CREATE TABLE approval_requests (
  id                VARCHAR(36)  PRIMARY KEY,
  chain_id          VARCHAR(36)  NOT NULL,
  policy_version_id VARCHAR(36)  NOT NULL,
  subject_type      VARCHAR(64)  NOT NULL,
  subject_id        VARCHAR(64)  NOT NULL,
  payload_json      VARCHAR(8192) NOT NULL,
  status            VARCHAR(16)  NOT NULL, -- PENDING | APPROVED | REJECTED | CANCELLED
  current_step      INT          NOT NULL DEFAULT 1,
  requested_by_id   VARCHAR(36)  NOT NULL,
  requested_by_name VARCHAR(120) NOT NULL,
  due_at            TIMESTAMP    NOT NULL,
  created_at        TIMESTAMP    NOT NULL,
  decided_at        TIMESTAMP,
  version           BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE approval_decisions (
  id           VARCHAR(36)  PRIMARY KEY,
  request_id   VARCHAR(36)  NOT NULL,
  step_no      INT          NOT NULL,
  approver_id  VARCHAR(36)  NOT NULL,
  approver_name VARCHAR(120) NOT NULL,
  decision     VARCHAR(16)  NOT NULL,     -- APPROVE | REJECT
  note         VARCHAR(1000),
  decided_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_request_status ON approval_requests (status, due_at);
CREATE INDEX idx_request_chain  ON approval_requests (chain_id);
CREATE INDEX idx_decision_req   ON approval_decisions (request_id, step_no);
CREATE INDEX idx_policy_active  ON policy_versions (policy_id, status);
