-- =====================================================================
-- JAVA-003 Contract Lifecycle & Obligation Engine — domain schema
-- =====================================================================

CREATE TABLE contracts (
  id             VARCHAR(36)  PRIMARY KEY,
  contract_no    VARCHAR(40)  NOT NULL UNIQUE,
  title          VARCHAR(200) NOT NULL,
  counterparty   VARCHAR(160) NOT NULL,
  owner_id       VARCHAR(36)  NOT NULL,
  owner_name     VARCHAR(120) NOT NULL,
  status         VARCHAR(16)  NOT NULL,   -- DRAFT | ACTIVE | EXPIRED | TERMINATED
  effective_from DATE,
  effective_to   DATE,
  created_at     TIMESTAMP    NOT NULL,
  version        BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE contract_versions (
  id            VARCHAR(36)  PRIMARY KEY,
  contract_id   VARCHAR(36)  NOT NULL,
  version_no    INT          NOT NULL,
  content_json  VARCHAR(8192) NOT NULL,   -- {"clauses":[{"number":"1.1","title":"...","text":"...","sensitivity":2,"obligationHints":[...]}]}
  created_by    VARCHAR(120) NOT NULL,
  created_at    TIMESTAMP    NOT NULL,
  UNIQUE (contract_id, version_no)
);

CREATE TABLE obligations (
  id                  VARCHAR(36)  PRIMARY KEY,
  contract_id         VARCHAR(36)  NOT NULL,
  source_clause       VARCHAR(32),
  obligation_type     VARCHAR(32)  NOT NULL,   -- RENEWAL | PAYMENT | DELIVERY | EXIT_RIGHT | COMPLIANCE | INSURANCE | OTHER
  title               VARCHAR(200) NOT NULL,
  description         VARCHAR(2000),
  due_at              TIMESTAMP    NOT NULL,
  window_before_days  INT          NOT NULL DEFAULT 30,
  repeat_interval_days INT,
  criticality         VARCHAR(8)   NOT NULL,   -- LOW | MEDIUM | HIGH
  status              VARCHAR(16)  NOT NULL,   -- OPEN | NOTIFIED | ACKNOWLEDGED | COMPLETED | WAIVED | OVERDUE
  assigned_to         VARCHAR(120),
  acknowledged_at     TIMESTAMP,
  completed_at        TIMESTAMP,
  waived_at           TIMESTAMP,
  waived_by           VARCHAR(120),
  waiver_reason       VARCHAR(1000),
  notified_at         TIMESTAMP,
  overdue_at          TIMESTAMP,
  created_at          TIMESTAMP    NOT NULL,
  version             BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE obligation_events (
  id           VARCHAR(36) PRIMARY KEY,
  obligation_id VARCHAR(36) NOT NULL,
  event_type   VARCHAR(32) NOT NULL,          -- CREATED | NOTIFIED | ACKNOWLEDGED | COMPLETED | WAIVED | OVERDUE | RECURRED
  detail       VARCHAR(1000),
  occurred_at  TIMESTAMP   NOT NULL
);

CREATE TABLE approvals (
  id           VARCHAR(36)  PRIMARY KEY,
  target_type  VARCHAR(32)  NOT NULL,         -- CONTRACT_ACTIVATION | OBLIGATION_WAIVER
  target_id    VARCHAR(36)  NOT NULL,
  approver_role VARCHAR(32) NOT NULL,         -- LEGAL_COUNSEL | CONTRACT_MANAGER | ADMIN
  approver_id  VARCHAR(36)  NOT NULL,
  approver_name VARCHAR(120) NOT NULL,
  decision     VARCHAR(16)  NOT NULL,         -- APPROVE | REJECT
  note         VARCHAR(1000),
  decided_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_contract_status   ON contracts (status);
CREATE INDEX idx_version_contract ON contract_versions (contract_id, version_no);
CREATE INDEX idx_obligation_status ON obligations (status, due_at);
CREATE INDEX idx_obligation_due    ON obligations (due_at, status);
CREATE INDEX idx_obligation_cont   ON obligations (contract_id);
CREATE INDEX idx_obevents_ob       ON obligation_events (obligation_id, occurred_at);
