-- =====================================================================
-- JAVA-004 Enterprise Document Governance Vault — domain schema
-- =====================================================================

CREATE TABLE documents (
  id            VARCHAR(36)  PRIMARY KEY,
  title         VARCHAR(200) NOT NULL,
  file_name     VARCHAR(255) NOT NULL,
  content_type  VARCHAR(64),
  size_bytes    BIGINT       NOT NULL,
  classification VARCHAR(16) NOT NULL,   -- UNCLASSIFIED | PUBLIC | INTERNAL | CONFIDENTIAL | RESTRICTED
  retention_class VARCHAR(8) NOT NULL,   -- R0..R7
  owner_id      VARCHAR(36)  NOT NULL,
  owner_name    VARCHAR(120) NOT NULL,
  content_hash  VARCHAR(64)  NOT NULL,
  extracted_text VARCHAR(8000),
  status        VARCHAR(16)  NOT NULL,   -- ACTIVE | QUARANTINED | DISPOSED
  legal_hold    BOOLEAN      NOT NULL DEFAULT FALSE,
  uploaded_at   TIMESTAMP    NOT NULL,
  disposed_at   TIMESTAMP,
  version       BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE retention_rules (
  id            VARCHAR(36) PRIMARY KEY,
  retention_class VARCHAR(8) NOT NULL UNIQUE,
  retention_days INT        NOT NULL,
  action        VARCHAR(16) NOT NULL,    -- DISPOSE | REVIEW | ARCHIVE
  active        BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE legal_holds (
  id          VARCHAR(36)  PRIMARY KEY,
  name        VARCHAR(120) NOT NULL,
  reason      VARCHAR(1000) NOT NULL,
  applied_by  VARCHAR(120) NOT NULL,
  applied_at  TIMESTAMP    NOT NULL,
  released_at TIMESTAMP,
  status      VARCHAR(16)  NOT NULL       -- ACTIVE | RELEASED
);

CREATE TABLE hold_entries (
  id         VARCHAR(36) PRIMARY KEY,
  hold_id    VARCHAR(36) NOT NULL,
  document_id VARCHAR(36) NOT NULL,
  UNIQUE (hold_id, document_id)
);

CREATE TABLE disposition_proofs (
  id            VARCHAR(36) PRIMARY KEY,
  document_id   VARCHAR(36) NOT NULL,
  title         VARCHAR(200) NOT NULL,
  content_hash  VARCHAR(64) NOT NULL,
  retention_class VARCHAR(8) NOT NULL,
  disposed_at   TIMESTAMP   NOT NULL,
  executor      VARCHAR(120) NOT NULL,
  disposition   VARCHAR(16) NOT NULL        -- DISPOSED | ARCHIVED
);

CREATE INDEX idx_doc_status   ON documents (status, uploaded_at);
CREATE INDEX idx_doc_class    ON documents (classification);
CREATE INDEX idx_doc_hold     ON documents (legal_hold, status);
CREATE INDEX idx_hold_status  ON legal_holds (status);
CREATE INDEX idx_holdentry_h  ON hold_entries (hold_id);
CREATE INDEX idx_holdentry_d  ON hold_entries (document_id);
