-- Expense Fraud & Policy Analytics Engine — project schema.
CREATE TABLE expense_claims (
  id             VARCHAR(36)   PRIMARY KEY,
  claim_no       VARCHAR(32)   NOT NULL UNIQUE,
  employee_id    VARCHAR(36)   NOT NULL,
  employee_name  VARCHAR(120)  NOT NULL,
  department     VARCHAR(64)   NOT NULL,
  category       VARCHAR(32)   NOT NULL,
  amount         DECIMAL(12,2) NOT NULL,
  currency       VARCHAR(8)    NOT NULL,
  merchant       VARCHAR(160),
  expense_date   DATE          NOT NULL,
  description    VARCHAR(500),
  receipt_ref    VARCHAR(64),
  status         VARCHAR(24)   NOT NULL,
  risk_score     INT           NOT NULL DEFAULT 0,
  score_version  INT           NOT NULL DEFAULT 0,
  reasons_json   VARCHAR(4000),
  submitted_at   TIMESTAMP     NOT NULL,
  created_at     TIMESTAMP     NOT NULL
);
CREATE INDEX idx_claims_status   ON expense_claims (status);
CREATE INDEX idx_claims_score    ON expense_claims (risk_score);
CREATE INDEX idx_claims_peer     ON expense_claims (department, category);

CREATE TABLE policy_rules (
  id          VARCHAR(36)   PRIMARY KEY,
  code        VARCHAR(40)   NOT NULL UNIQUE,
  category    VARCHAR(32)   NOT NULL,
  comparator  VARCHAR(32)   NOT NULL,
  threshold   DECIMAL(12,2),
  pattern     VARCHAR(120),
  severity    VARCHAR(16)   NOT NULL,
  message     VARCHAR(500)  NOT NULL,
  active      BOOLEAN       NOT NULL DEFAULT TRUE,
  sort_order  INT           NOT NULL DEFAULT 0
);

CREATE TABLE rule_violations (
  id          VARCHAR(36)   PRIMARY KEY,
  claim_id    VARCHAR(36)   NOT NULL,
  rule_code   VARCHAR(40)   NOT NULL,
  rule_message VARCHAR(500) NOT NULL,
  observed    VARCHAR(200),
  expected    VARCHAR(200),
  severity    VARCHAR(16)   NOT NULL,
  points      INT           NOT NULL,
  created_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_violations_claim ON rule_violations (claim_id);

CREATE TABLE duplicate_groups (
  id               VARCHAR(36)   PRIMARY KEY,
  group_key        VARCHAR(64)   NOT NULL UNIQUE,
  claim_ids        VARCHAR(2000) NOT NULL,
  merchant         VARCHAR(160),
  amount           DECIMAL(12,2),
  match_confidence DECIMAL(5,3)  NOT NULL,
  group_size       INT           NOT NULL,
  status           VARCHAR(24)   NOT NULL,
  created_at       TIMESTAMP     NOT NULL,
  resolved_at      TIMESTAMP
);

CREATE TABLE fraud_cases (
  id              VARCHAR(36)   PRIMARY KEY,
  case_no         VARCHAR(32)   NOT NULL UNIQUE,
  claim_id        VARCHAR(36)   NOT NULL,
  risk_score      INT           NOT NULL,
  reasons_json    VARCHAR(4000) NOT NULL,
  evidence_json   VARCHAR(4000),
  status          VARCHAR(24)   NOT NULL,
  opened_by       VARCHAR(64)   NOT NULL,
  opened_at       TIMESTAMP     NOT NULL,
  reviewer_one    VARCHAR(64),
  reviewer_one_note VARCHAR(1000),
  reviewed_at     TIMESTAMP,
  reviewer_two    VARCHAR(64),
  decision        VARCHAR(24),
  decision_note   VARCHAR(1000),
  decided_at      TIMESTAMP
);
CREATE INDEX idx_cases_status ON fraud_cases (status);

CREATE TABLE tips (
  id                VARCHAR(36)  PRIMARY KEY,
  tip_no            VARCHAR(32)  NOT NULL UNIQUE,
  channel           VARCHAR(24)  NOT NULL,
  subject           VARCHAR(200) NOT NULL,
  description       VARCHAR(2000) NOT NULL,
  related_claim_no  VARCHAR(32),
  status            VARCHAR(24)  NOT NULL,
  outcome           VARCHAR(500),
  submitted_at      TIMESTAMP    NOT NULL,
  reviewed_by       VARCHAR(64),
  reviewed_at       TIMESTAMP
);
CREATE INDEX idx_tips_status ON tips (status);

CREATE TABLE peer_baselines (
  id            VARCHAR(36)   PRIMARY KEY,
  department    VARCHAR(64)   NOT NULL,
  category      VARCHAR(32)   NOT NULL,
  mean_amount   DECIMAL(12,2) NOT NULL,
  median_amount DECIMAL(12,2) NOT NULL,
  p90_amount    DECIMAL(12,2) NOT NULL,
  std_dev       DECIMAL(12,2) NOT NULL,
  sample_count  INT           NOT NULL,
  updated_at    TIMESTAMP     NOT NULL,
  CONSTRAINT uq_baseline_dept_cat UNIQUE (department, category)
);
