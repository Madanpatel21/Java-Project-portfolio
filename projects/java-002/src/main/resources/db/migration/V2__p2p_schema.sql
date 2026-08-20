-- =====================================================================
-- JAVA-002 Procure-to-Pay Reconciliation — domain schema
-- =====================================================================

CREATE TABLE purchase_orders (
  id            VARCHAR(36)  PRIMARY KEY,
  po_number     VARCHAR(40)  NOT NULL UNIQUE,
  supplier_id   VARCHAR(36)  NOT NULL,
  supplier_name VARCHAR(160) NOT NULL,
  currency      VARCHAR(8)   NOT NULL,
  status        VARCHAR(16)  NOT NULL,   -- OPEN | CLOSED
  issued_at     TIMESTAMP    NOT NULL,
  created_at    TIMESTAMP    NOT NULL,
  version       BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE purchase_order_lines (
  id           VARCHAR(36)  PRIMARY KEY,
  po_id        VARCHAR(36)  NOT NULL,
  line_no      INT          NOT NULL,
  item_code    VARCHAR(64)  NOT NULL,
  description  VARCHAR(200) NOT NULL,
  quantity     DECIMAL(14,3) NOT NULL,
  unit_price   DECIMAL(14,4) NOT NULL,
  received_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  invoiced_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  UNIQUE (po_id, line_no)
);

CREATE TABLE goods_receipts (
  id          VARCHAR(36)  PRIMARY KEY,
  gr_number   VARCHAR(40)  NOT NULL UNIQUE,
  po_id       VARCHAR(36)  NOT NULL,
  supplier_id VARCHAR(36)  NOT NULL,
  received_at TIMESTAMP    NOT NULL,
  status      VARCHAR(16)  NOT NULL    -- POSTED
);

CREATE TABLE goods_receipt_lines (
  id                VARCHAR(36)  PRIMARY KEY,
  gr_id             VARCHAR(36)  NOT NULL,
  po_line_id        VARCHAR(36)  NOT NULL,
  quantity_received DECIMAL(14,3) NOT NULL
);

CREATE TABLE invoices (
  id            VARCHAR(36)  PRIMARY KEY,
  invoice_number VARCHAR(40) NOT NULL,
  supplier_id   VARCHAR(36)  NOT NULL,
  supplier_name VARCHAR(160) NOT NULL,
  currency      VARCHAR(8)   NOT NULL,
  total_amount  DECIMAL(14,2) NOT NULL,
  status        VARCHAR(16)  NOT NULL,   -- NEW | MATCHED | EXCEPTION | APPROVED | POSTED | REJECTED
  invoice_date  DATE         NOT NULL,
  due_date      DATE,
  created_at    TIMESTAMP    NOT NULL,
  version       BIGINT       NOT NULL DEFAULT 0,
  UNIQUE (invoice_number, supplier_id)
);

CREATE TABLE invoice_lines (
  id          VARCHAR(36)  PRIMARY KEY,
  invoice_id  VARCHAR(36)  NOT NULL,
  item_code   VARCHAR(64)  NOT NULL,
  quantity    DECIMAL(14,3) NOT NULL,
  unit_price  DECIMAL(14,4) NOT NULL,
  line_total  DECIMAL(14,2) NOT NULL
);

CREATE TABLE tolerance_rules (
  id            VARCHAR(36)  PRIMARY KEY,
  rule_type     VARCHAR(32)  NOT NULL,   -- PRICE_VARIANCE | QUANTITY_VARIANCE | AMOUNT_VARIANCE
  tolerance_pct DECIMAL(8,3) NOT NULL,
  action        VARCHAR(16)  NOT NULL,   -- AUTO_POST | WARN | BLOCK
  active        BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE exceptions (
  id             VARCHAR(36)  PRIMARY KEY,
  invoice_id     VARCHAR(36)  NOT NULL,
  exception_type VARCHAR(32)  NOT NULL,  -- NO_PO_MATCH | MISSING_RECEIPT | PRICE_VARIANCE | QUANTITY_VARIANCE | DUPLICATE_INVOICE | OVER_BILLING
  severity       VARCHAR(16)  NOT NULL,  -- WARNING | CRITICAL
  detail_json    VARCHAR(4000) NOT NULL,
  status         VARCHAR(16)  NOT NULL,  -- OPEN | RESOLVED | WAIVED | REJECTED
  assigned_to    VARCHAR(120),
  created_at     TIMESTAMP    NOT NULL,
  resolved_at    TIMESTAMP,
  resolved_by    VARCHAR(120),
  resolution_note VARCHAR(1000)
);

CREATE TABLE gl_postings (
  id         VARCHAR(36) PRIMARY KEY,
  invoice_id VARCHAR(36) NOT NULL,
  batch_id   VARCHAR(36),
  gl_account VARCHAR(32) NOT NULL,
  debit      DECIMAL(14,2) NOT NULL DEFAULT 0,
  credit     DECIMAL(14,2) NOT NULL DEFAULT 0,
  status     VARCHAR(16) NOT NULL,       -- PENDING | POSTED | FAILED
  created_at TIMESTAMP   NOT NULL,
  posted_at  TIMESTAMP
);

CREATE TABLE batch_runs (
  id                  VARCHAR(36) PRIMARY KEY,
  started_at          TIMESTAMP   NOT NULL,
  completed_at        TIMESTAMP,
  invoices_processed  INT         NOT NULL DEFAULT 0,
  exceptions_created  INT         NOT NULL DEFAULT 0,
  postings_created    INT         NOT NULL DEFAULT 0,
  status              VARCHAR(16) NOT NULL   -- RUNNING | COMPLETED | FAILED
);

CREATE TABLE outbox (
  id         VARCHAR(36)  PRIMARY KEY,
  event_type VARCHAR(64)  NOT NULL,
  payload    VARCHAR(8192) NOT NULL,
  status     VARCHAR(16)  NOT NULL,      -- PENDING | SENT
  created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_invoice_status   ON invoices (status, created_at);
CREATE INDEX idx_invoice_supplier ON invoices (supplier_id, invoice_date);
CREATE INDEX idx_exception_status ON exceptions (status, severity);
CREATE INDEX idx_exception_inv    ON exceptions (invoice_id);
CREATE INDEX idx_po_supplier      ON purchase_orders (supplier_id);
CREATE INDEX idx_gr_po            ON goods_receipts (po_id);
CREATE INDEX idx_posting_status   ON gl_postings (status);
CREATE INDEX idx_outbox_status    ON outbox (status, created_at);
