-- =====================================================================
-- JAVA-005 Dynamic Workflow Orchestration Platform — domain schema
-- =====================================================================

CREATE TABLE workflow_definitions (
  id              VARCHAR(36)  PRIMARY KEY,
  definition_key  VARCHAR(64)  NOT NULL,
  name            VARCHAR(160) NOT NULL,
  version_no      INT          NOT NULL,
  definition_json VARCHAR(8192) NOT NULL,
  status          VARCHAR(16)  NOT NULL,   -- ACTIVE | DEPRECATED
  created_by      VARCHAR(120) NOT NULL,
  created_at      TIMESTAMP    NOT NULL,
  UNIQUE (definition_key, version_no)
);

CREATE TABLE workflow_instances (
  id               VARCHAR(36)  PRIMARY KEY,
  definition_id    VARCHAR(36)  NOT NULL,
  business_key     VARCHAR(120) NOT NULL,
  status           VARCHAR(24)  NOT NULL,  -- RUNNING | WAITING_TASK | WAITING_TIMER | COMPLETED | FAILED | CANCELLED
  variables_json   VARCHAR(8192) NOT NULL,
  current_node_id  VARCHAR(64),
  resume_at        TIMESTAMP,
  started_at       TIMESTAMP    NOT NULL,
  completed_at     TIMESTAMP,
  version          BIGINT       NOT NULL DEFAULT 0,
  UNIQUE (definition_id, business_key)
);

CREATE TABLE workflow_tasks (
  id            VARCHAR(36)  PRIMARY KEY,
  instance_id   VARCHAR(36)  NOT NULL,
  node_id       VARCHAR(64)  NOT NULL,
  task_type     VARCHAR(16)  NOT NULL,    -- APPROVAL | AUTOMATED | COMPENSATION
  assignee_role VARCHAR(32),
  status        VARCHAR(16)  NOT NULL,    -- PENDING | COMPLETED | SKIPPED
  result_json   VARCHAR(2000),
  due_at        TIMESTAMP,
  created_at    TIMESTAMP    NOT NULL,
  completed_at  TIMESTAMP,
  completed_by  VARCHAR(120),
  version       BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE workflow_steps (
  id          VARCHAR(36) PRIMARY KEY,
  instance_id VARCHAR(36) NOT NULL,
  node_id     VARCHAR(64) NOT NULL,
  step_type   VARCHAR(16) NOT NULL,       -- AUTOMATED | APPROVAL | GATEWAY
  result_json VARCHAR(2000),
  occurred_at TIMESTAMP   NOT NULL
);

CREATE INDEX idx_instance_status ON workflow_instances (status);
CREATE INDEX idx_instance_bkey   ON workflow_instances (business_key);
CREATE INDEX idx_task_worklist   ON workflow_tasks (status, assignee_role, due_at);
CREATE INDEX idx_task_instance   ON workflow_tasks (instance_id);
CREATE INDEX idx_step_instance   ON workflow_steps (instance_id, occurred_at);
CREATE INDEX idx_instance_resume ON workflow_instances (status, resume_at);
