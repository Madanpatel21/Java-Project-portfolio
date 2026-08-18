-- =====================================================================
-- JAVA-211 Antimicrobial Stewardship Tracker — initial schema
-- Portable across PostgreSQL 16 and H2 (PostgreSQL compatibility mode)
-- =====================================================================

-- ---- Patients & admissions -----------------------------------------
CREATE TABLE patients (
  id        VARCHAR(36) PRIMARY KEY,
  mrn       VARCHAR(32)  NOT NULL UNIQUE,
  name      VARCHAR(120) NOT NULL,
  dob       DATE         NOT NULL,
  sex       VARCHAR(8)   NOT NULL,
  weight_kg DECIMAL(6,2) NOT NULL
);

CREATE TABLE admissions (
  id           VARCHAR(36) PRIMARY KEY,
  patient_id   VARCHAR(36) NOT NULL,
  ward         VARCHAR(64) NOT NULL,
  admitted_at  TIMESTAMP   NOT NULL,
  discharged_at TIMESTAMP
);

CREATE TABLE lab_values (
  id          VARCHAR(36) PRIMARY KEY,
  patient_id  VARCHAR(36) NOT NULL,
  type        VARCHAR(32) NOT NULL,   -- CREATININE, CRP, WBC, PROCALCITONIN ...
  reading     DECIMAL(12,3) NOT NULL, -- "value" is an H2 reserved word; "reading" is portable
  unit        VARCHAR(16) NOT NULL,
  measured_at TIMESTAMP   NOT NULL
);

-- ---- Antimicrobial catalog ------------------------------------------
CREATE TABLE antimicrobial_drugs (
  id             VARCHAR(36)  PRIMARY KEY,
  code           VARCHAR(32)  NOT NULL UNIQUE,
  name           VARCHAR(120) NOT NULL,
  drug_class     VARCHAR(64)  NOT NULL,
  spectrum       VARCHAR(16)  NOT NULL,     -- NARROW | MEDIUM | BROAD
  ddd_grams      DECIMAL(10,3) NOT NULL,
  iv_available   BOOLEAN      NOT NULL,
  po_available   BOOLEAN      NOT NULL,
  restricted     BOOLEAN      NOT NULL,
  coverage_tags  VARCHAR(200) NOT NULL,      -- comma list: GRAM_NEG,ANAEROBE,MRSA,PSEUDOMONAS,ESBL,FUNGAL ...
  iv_cost_per_day DECIMAL(10,2),
  po_cost_per_day DECIMAL(10,2)
);

-- ---- Stewardship guidelines (versioned) ------------------------------
CREATE TABLE stewardship_guidelines (
  id             VARCHAR(36)  PRIMARY KEY,
  name           VARCHAR(120) NOT NULL,
  version_no     INT          NOT NULL,
  status         VARCHAR(16)  NOT NULL,     -- ACTIVE | SUPERSEDED
  effective_from TIMESTAMP    NOT NULL,
  created_by     VARCHAR(120) NOT NULL,
  rules_json     VARCHAR(8192) NOT NULL
);

-- ---- Prescriptions ---------------------------------------------------
CREATE TABLE prescriptions (
  id                   VARCHAR(36)  PRIMARY KEY,
  patient_id           VARCHAR(36)  NOT NULL,
  admission_id         VARCHAR(36)  NOT NULL,
  drug_id              VARCHAR(36)  NOT NULL,
  indication           VARCHAR(200) NOT NULL,
  route                VARCHAR(8)   NOT NULL,   -- IV | PO
  dose_mg              DECIMAL(10,2) NOT NULL,
  frequency_hours      INT          NOT NULL,   -- e.g. 8 = Q8H
  start_at             TIMESTAMP    NOT NULL,
  stop_at              TIMESTAMP,
  status               VARCHAR(24)  NOT NULL,   -- PENDING_AUTHORIZATION | ACTIVE | STOPPED | COMPLETED | EXPIRED
  empiric              BOOLEAN      NOT NULL,
  prescribed_by        VARCHAR(120) NOT NULL,
  guideline_version_id VARCHAR(36),
  restricted_auth_id   VARCHAR(36),
  created_at           TIMESTAMP    NOT NULL,
  version              BIGINT       NOT NULL DEFAULT 0
);

-- ---- Restricted-drug pre-authorization -------------------------------
CREATE TABLE restricted_authorizations (
  id             VARCHAR(36)  PRIMARY KEY,
  prescription_id VARCHAR(36) NOT NULL,
  requested_by   VARCHAR(120) NOT NULL,
  requested_at   TIMESTAMP    NOT NULL,
  approved_by    VARCHAR(120),
  approved_at    TIMESTAMP,
  expires_at     TIMESTAMP,
  status         VARCHAR(24)  NOT NULL,    -- PENDING | APPROVED | REJECTED | EXPIRED
  reason         VARCHAR(1000)
);

-- ---- Review tasks -----------------------------------------------------
CREATE TABLE review_tasks (
  id             VARCHAR(36)  PRIMARY KEY,
  prescription_id VARCHAR(36) NOT NULL,
  trigger_reason VARCHAR(64)  NOT NULL,    -- TIME_BASED | CULTURE_RESULT | DRUG_BUG_MISMATCH | REDUNDANT_COVERAGE | RENAL_CHANGE
  due_at         TIMESTAMP    NOT NULL,
  status         VARCHAR(16)  NOT NULL,    -- OPEN | COMPLETED | CANCELLED
  assigned_to    VARCHAR(120),
  created_by     VARCHAR(120) NOT NULL,
  created_at     TIMESTAMP    NOT NULL,
  completed_at   TIMESTAMP
);

-- ---- Interventions -----------------------------------------------------
CREATE TABLE interventions (
  id              VARCHAR(36)  PRIMARY KEY,
  prescription_id VARCHAR(36)  NOT NULL,
  review_task_id  VARCHAR(36),
  type            VARCHAR(32)  NOT NULL,   -- STOP | IV_TO_PO | DOSE_CHANGE | DE_ESCALATE | DURATION_CHANGE | RENAL_ADJUST | ADD_THERAPY
  detail_json     VARCHAR(2000) NOT NULL,
  reason          VARCHAR(2000) NOT NULL,
  status          VARCHAR(16)  NOT NULL,   -- PROPOSED | ACCEPTED | REJECTED | IGNORED | EXPIRED
  proposed_by     VARCHAR(120) NOT NULL,
  proposed_at     TIMESTAMP    NOT NULL,
  decided_by      VARCHAR(120),
  decided_at      TIMESTAMP,
  prescriber_response VARCHAR(1000)
);

-- ---- Microbiology ------------------------------------------------------
CREATE TABLE cultures (
  id            VARCHAR(36)  PRIMARY KEY,
  patient_id    VARCHAR(36)  NOT NULL,
  specimen_type VARCHAR(32)  NOT NULL,     -- BLOOD | URINE | SPUTUM | WOUND | CSF
  collected_at  TIMESTAMP    NOT NULL,
  reported_at   TIMESTAMP
);

CREATE TABLE isolates (
  id          VARCHAR(36)  PRIMARY KEY,
  culture_id  VARCHAR(36)  NOT NULL,
  organism    VARCHAR(120) NOT NULL,
  collected_at TIMESTAMP   NOT NULL
);

CREATE TABLE susceptibility_results (
  id         VARCHAR(36)  PRIMARY KEY,
  isolate_id VARCHAR(36)  NOT NULL,
  drug_id    VARCHAR(36)  NOT NULL,
  result     VARCHAR(4)   NOT NULL,        -- S | I | R
  mic_value  DECIMAL(12,4)
);

-- ---- Local identity provider (dev/fallback) ------------------------------
CREATE TABLE local_users (
  id             VARCHAR(36)  PRIMARY KEY,
  username       VARCHAR(64)  NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  email          VARCHAR(255) NOT NULL,
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

-- ---- Audit / idempotency ------------------------------------------------
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

-- ---- Indexes -------------------------------------------------------------
CREATE INDEX idx_prescription_patient  ON prescriptions (patient_id, status);
CREATE INDEX idx_prescription_status   ON prescriptions (status, start_at);
CREATE INDEX idx_review_open           ON review_tasks (status, due_at);
CREATE INDEX idx_intervention_rx       ON interventions (prescription_id, status);
CREATE INDEX idx_isolate_culture       ON isolates (culture_id);
CREATE INDEX idx_susc_isolate          ON susceptibility_results (isolate_id);
CREATE INDEX idx_lab_patient_type       ON lab_values (patient_id, type, measured_at);
CREATE INDEX idx_admission_ward         ON admissions (ward, admitted_at);
CREATE INDEX idx_audit_time             ON audit_log (occurred_at);
