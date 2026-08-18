# Healthcare / Pharma / Life Sciences — Catalog

78 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-193 — Patient Identity & Enterprise Master Index

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Identity
- **Business problem:** Duplicate patient records cause wrong-patient errors; merging identities must be safe and reversible.
- **Core engineering problem:** Patient identity matching with survivorship, merge/unmerge and record linking.
- **Architecture:** Modular monolith; MPI engine; matching pipeline; merge workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (identity events)
- **Security architecture:** RBAC, PHI encryption, merge four-eyes
- **Key advanced concepts:** Probabilistic matching, merge/unmerge, cross-references
- **Why it is industrial:** Enterprise-grade patient identity with reversible merges

## JAVA-194 — Admission, Transfer & Discharge Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Hospital Ops
- **Business problem:** Admissions, transfers and discharges must update bed state, billing and care teams instantly.
- **Core engineering problem:** ATD state machine with bed management, billing triggers and care-team notifications.
- **Architecture:** Modular monolith; ATD service; bed state; billing hooks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (ATD events)
- **Security architecture:** RBAC, ward scoping, PHI masking
- **Key advanced concepts:** ATD state machine, bed conflicts, notifications
- **Why it is industrial:** Hospital-grade patient movement with billing coupling

## JAVA-195 — Appointment Scheduling & Waitlist Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Scheduling
- **Business problem:** Clinic scheduling must respect provider skills, room resources and waitlist fairness.
- **Core engineering problem:** Multi-resource scheduling with waitlist promotion and no-show policies.
- **Architecture:** Modular monolith; scheduling engine; waitlist; no-show rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, patient privacy, audit
- **Key advanced concepts:** Multi-resource booking, waitlist fairness
- **Why it is industrial:** Clinic-grade scheduling with waitlist fairness

## JAVA-196 — Clinical Document Generation & Sign-off

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Clinical Docs
- **Business problem:** Clinical documents need structured templates, versioned sign-offs and tamper evidence.
- **Core engineering problem:** Document composition with templates, co-sign rules and hash-verified versions.
- **Architecture:** Modular monolith; doc engine; template store; signature workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (doc events)
- **Security architecture:** Role-based sign-off, document hashing, PHI controls
- **Key advanced concepts:** Templates, co-sign, version integrity
- **Why it is industrial:** Legally-signed clinical documentation with version integrity

## JAVA-197 — Medication Orders & Dispensing Workflow

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Pharmacy
- **Business problem:** Medication orders must be validated, dispensed and tracked with closed-loop administration.
- **Core engineering problem:** Order-dispense-administer loop with dose checks, inventory and alerts.
- **Architecture:** Modular monolith; order service; dispensing workflow; administration tracking
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (med events)
- **Security architecture:** RBAC, pharmacist verification, controlled-drug tracking
- **Key advanced concepts:** Closed-loop administration, dose checking, inventory
- **Why it is industrial:** Closed-loop medication management with safety checks

## JAVA-198 — Laboratory Order & Results Lifecycle

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Lab
- **Business problem:** Lab orders and results must flow with specimen integrity, priority and critical-result handling.
- **Core engineering problem:** Order-to-result lifecycle with critical result escalation and audit.
- **Architecture:** Modular monolith; lab workflow; specimen tracking; critical alerts
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (lab events)
- **Security architecture:** RBAC, PHI protection, critical-result audit
- **Key advanced concepts:** Specimen states, critical values, escalation
- **Why it is industrial:** Lab-grade result lifecycle with critical-value escalation

## JAVA-199 — Radiology Worklist & Report Distribution

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Radiology
- **Business problem:** Radiology worklists must prioritize studies, track reads and distribute signed reports.
- **Core engineering problem:** Worklist management with priorities, reading workflow and report distribution.
- **Architecture:** Modular monolith; worklist service; reading workflow; distribution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (study events)
- **Security architecture:** RBAC, image access controls, sign-off
- **Key advanced concepts:** Priority worklists, reading states, distribution
- **Why it is industrial:** Radiology-grade worklist and report distribution

## JAVA-200 — Care Plan Authoring & Task Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Care Management
- **Business problem:** Care plans must be authored, versioned and turned into assigned tasks with deadlines.
- **Core engineering problem:** Care plan engine with task generation, assignments and compliance tracking.
- **Architecture:** Modular monolith; plan engine; task service; compliance views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (task events)
- **Security architecture:** RBAC, plan versioning, task audit
- **Key advanced concepts:** Plan versioning, task generation, adherence
- **Why it is industrial:** Care-plan automation with adherence analytics

## JAVA-201 — Clinical Decision Support Rules Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / CDS
- **Business problem:** Clinical decision rules (lab thresholds, scores) must run reliably with versioned evidence.
- **Core engineering problem:** CDS rules service with versioned rule packs, evaluation and alerting.
- **Architecture:** Modular monolith; rules service; rule packs; evaluation API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (CDS events)
- **Security architecture:** RBAC, clinical context authorization, audit
- **Key advanced concepts:** Versioned rules, evaluation, alert fatigue control
- **Why it is industrial:** CDS rule engine with evidence-versioned rule packs

## JAVA-202 — Drug Interaction & Contraindication Checker

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Pharmacy
- **Business problem:** Drug interactions and contraindications must be checked at order time with severity levels.
- **Core engineering problem:** Drug-interaction checker with knowledge base versioning and severity workflow.
- **Architecture:** Modular monolith; interaction engine; KB store; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (alert events)
- **Security architecture:** RBAC, PHI in alerts minimized, audit
- **Key advanced concepts:** Interaction KB, severity levels, override rules
- **Why it is industrial:** Order-time drug safety with override governance

## JAVA-203 — Vaccination Registry & Cold-Chain Ledger

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Immunization
- **Business problem:** Vaccination records need cold-chain integrity, dose schedules and registry reporting.
- **Core engineering problem:** Vaccination registry with cold-chain telemetry and schedule engine.
- **Architecture:** Modular monolith; registry service; cold-chain ingestion; schedule engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (temperature telemetry)
- **Security architecture:** RBAC, PHI protection, excursion alerts
- **Key advanced concepts:** Cold-chain events, dose schedules, registry
- **Why it is industrial:** Immunization registry with cold-chain integrity

## JAVA-204 — Vitals Telemetry Ingestion & Trending

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Telemetry
- **Business problem:** Patient vitals stream from monitors; clinicians need trends, alerts and context.
- **Core engineering problem:** High-volume vitals ingestion with trending, thresholds and nurse-alert routing.
- **Architecture:** Modular monolith; ingestion pipeline; trending; alert routing
- **Java technology stack:** Spring Boot 3, Spring WebFlux, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (vitals streams)
- **Security architecture:** RBAC, ward scoping, alert audit
- **Key advanced concepts:** Stream ingestion, downsampling, threshold alerts
- **Why it is industrial:** Monitor-grade vitals ingestion with alert routing

## JAVA-205 — Hospital Billing & Claims Scrubber

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / RCM
- **Business problem:** Hospital bills must scrub for errors before claim submission to reduce denials.
- **Core engineering problem:** Claims scrubbing with edit rules, coding checks and denial analytics.
- **Architecture:** Modular monolith; billing engine; scrub rules; claim pipeline
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (claim events)
- **Security architecture:** RBAC, PHI masking, four-eyes adjustments
- **Key advanced concepts:** Edit rules, coding checks, denial patterns
- **Why it is industrial:** RCM-grade claim scrubbing with denial analytics

## JAVA-206 — Insurance Eligibility & Pre-Authorization

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / RCM
- **Business problem:** Eligibility and pre-authorization must be verified before services to prevent denials.
- **Core engineering problem:** Eligibility checks with payer adapters, authorization workflows and status caching.
- **Architecture:** Modular monolith; eligibility service; auth workflow; payer adapters
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (auth events)
- **Security architecture:** RBAC, PHI controls, payer isolation
- **Key advanced concepts:** Payer adapters, auth states, cache invalidation
- **Why it is industrial:** Pre-service eligibility with payer resilience

## JAVA-207 — Bed Management & Capacity Dashboard

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Hospital Ops
- **Business problem:** Bed capacity must be visible in real time with predicted discharges and bottlenecks.
- **Core engineering problem:** Bed dashboard with real-time occupancy, discharge prediction and bottleneck alerts.
- **Architecture:** Modular monolith; bed state service; prediction engine; dashboard API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (bed events)
- **Security architecture:** RBAC, ward scoping, audit
- **Key advanced concepts:** Real-time occupancy, discharge prediction
- **Why it is industrial:** Capacity command with predictive discharge insights

## JAVA-208 — Operating Theater Scheduling & Utilization

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / OR
- **Business problem:** Operating theaters must be scheduled for utilization while respecting surgeon blocks and turnover times.
- **Core engineering problem:** OR scheduling with block management, turnover buffers and utilization analytics.
- **Architecture:** Modular monolith; OR scheduler; block service; utilization views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (OR events)
- **Security architecture:** RBAC, surgeon scoping, audit
- **Key advanced concepts:** Block scheduling, turnover buffers, utilization
- **Why it is industrial:** OR-grade scheduling with utilization optimization

## JAVA-209 — Emergency Triage & Patient Flow

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Emergency
- **Business problem:** ED patient flow must track acuity, wait times and boarding with surge response.
- **Core engineering problem:** ED flow board with acuity scoring, wait tracking and surge alerts.
- **Architecture:** Modular monolith; flow service; acuity engine; surge detection
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (ED events)
- **Security architecture:** RBAC, minimal PHI on boards, audit
- **Key advanced concepts:** Acuity scoring, wait times, surge alerts
- **Why it is industrial:** ED flow command with acuity-driven prioritization

## JAVA-210 — Infection Outbreak Surveillance & Alerts

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Epidemiology
- **Business problem:** Infection outbreaks must be detected from lab data with alerting and contact tracing support.
- **Core engineering problem:** Syndromic surveillance with aberration detection and investigation workflows.
- **Architecture:** Modular monolith; surveillance engine; aberration detection; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (lab events)
- **Security architecture:** RBAC, de-identified analytics, audit
- **Key advanced concepts:** Aberration detection, outbreak alerts, tracing
- **Why it is industrial:** Epidemiological surveillance with statistical detection

## JAVA-211 — Antimicrobial Stewardship Tracker

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Pharmacy
- **Business problem:** Antimicrobial use must be tracked against stewardship policies with intervention workflows.
- **Core engineering problem:** Stewardship tracker with prescription review, interventions and resistance reporting.
- **Architecture:** Modular monolith; stewardship workflow; intervention engine; reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (Rx events)
- **Security architecture:** RBAC, pharmacist roles, audit
- **Key advanced concepts:** Stewardship rules, interventions, resistance
- **Why it is industrial:** Stewardship-grade tracking with intervention loops

## JAVA-212 — Blood Bank Inventory & Crossmatch

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Transfusion
- **Business problem:** Blood inventory must match units to patients with crossmatch rules and expiry management.
- **Core engineering problem:** Blood bank inventory with crossmatch validation, expiry and utilization.
- **Architecture:** Modular monolith; inventory engine; crossmatch service; issue workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (issue events)
- **Security architecture:** RBAC, unit traceability, emergency release
- **Key advanced concepts:** Crossmatch rules, expiry, emergency release
- **Why it is industrial:** Transfusion-grade inventory with safety workflows

## JAVA-213 — Organ & Transplant Waitlist Registry

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Transplant
- **Business problem:** Organ waitlists must rank candidates fairly by medical urgency and match rules.
- **Core engineering problem:** Waitlist registry with scoring, matching and status updates.
- **Architecture:** Modular monolith; registry service; matching engine; status workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (match events)
- **Security architecture:** RBAC, high-sensitivity PHI, audit
- **Key advanced concepts:** Urgency scoring, match rules, fairness
- **Why it is industrial:** Transplant-grade matching with fairness constraints

## JAVA-214 — Electronic Prior Authorization Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / RCM
- **Business problem:** Prior authorization requests must be assembled, submitted and tracked against payer rules.
- **Core engineering problem:** PA engine with clinical evidence assembly, payer rules and status tracking.
- **Architecture:** Modular monolith; PA workflow; evidence assembly; payer rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (PA events)
- **Security architecture:** RBAC, PHI controls, deadline audit
- **Key advanced concepts:** Evidence assembly, payer rules, deadlines
- **Why it is industrial:** PA automation with clinical evidence assembly

## JAVA-215 — Referral Management & Consult Workflow

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Referrals
- **Business problem:** Referrals must route patients with clinical context, appointment follow-through and closing the loop.
- **Core engineering problem:** Referral management with consult workflow and loop closure.
- **Architecture:** Modular monolith; referral service; consult workflow; closure tracking
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (referral events)
- **Security architecture:** RBAC, provider scoping, PHI controls
- **Key advanced concepts:** Referral states, consult workflow, closures
- **Why it is industrial:** Referral-grade coordination with loop closure

## JAVA-216 — Remote Patient Monitoring Command

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / RPM
- **Business problem:** Remote patient monitoring must ingest device data, risk-stratify and alert care teams.
- **Core engineering problem:** RPM command with device ingestion, risk rules and escalation.
- **Architecture:** Modular monolith; device ingestion; risk engine; escalation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (device data)
- **Security architecture:** RBAC, device identity, alert audit
- **Key advanced concepts:** Device identity, risk rules, escalation
- **Why it is industrial:** RPM-grade device ingestion with risk escalation

## JAVA-217 — Chronic Disease Registry & Cohort Care

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Population Health
- **Business problem:** Chronic disease cohorts must be identified, risk-stratified and managed with care gaps.
- **Core engineering problem:** Registry analytics with risk stratification and care-gap generation.
- **Architecture:** Modular monolith; registry engine; stratification; care-gap service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (cohort events)
- **Security architecture:** RBAC, cohort-level de-identification, audit
- **Key advanced concepts:** Cohort queries, stratification, care gaps
- **Why it is industrial:** Population-health analytics with care-gap automation

## JAVA-218 — Physician Credentialing & Privileging

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Credentialing
- **Business problem:** Physician credentialing must verify licenses, privileges and expirations continuously.
- **Core engineering problem:** Credentialing workflow with primary-source verification simulation and expirations.
- **Architecture:** Modular monolith; credentialing workflow; verification adapters; expiry alerts
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (credential events)
- **Security architecture:** RBAC, provider PII, audit
- **Key advanced concepts:** Primary-source verification sim, expirations
- **Why it is industrial:** Credentialing-grade verification with expiry monitoring

## JAVA-219 — Nurse Staffing & Acuity-Based Rostering

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Staffing
- **Business problem:** Nurse staffing must match acuity scores to skill mix with labor rules.
- **Core engineering problem:** Acuity-based rostering with skill-mix constraints and fatigue rules.
- **Architecture:** Modular monolith; acuity engine; roster solver; fatigue rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (roster events)
- **Security architecture:** RBAC, staff PII, audit
- **Key advanced concepts:** Acuity-driven staffing, skill mix, fatigue
- **Why it is industrial:** Acuity-based nurse rostering with labor rules

## JAVA-220 — Hospital Inventory & Consignment Pharmacy

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Supply
- **Business problem:** Hospital inventory (esp. consignment) must track expiry, PAR levels and usage per department.
- **Core engineering problem:** Inventory engine with PAR levels, consignment billing and expiry sweeps.
- **Architecture:** Modular monolith; inventory service; consignment billing; expiry jobs
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (stock events)
- **Security architecture:** RBAC, department scoping, audit
- **Key advanced concepts:** PAR levels, consignment, expiry
- **Why it is industrial:** Hospital-grade inventory with consignment billing

## JAVA-221 — Dietary & Nutrition Management System

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Nutrition
- **Business problem:** Dietary services must manage therapeutic diets, allergies and meal production.
- **Core engineering problem:** Dietary management with therapeutic diet rules, allergy screening and production plans.
- **Architecture:** Modular monolith; diet engine; allergy screening; production plans
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (meal events)
- **Security architecture:** RBAC, allergy data protection, audit
- **Key advanced concepts:** Therapeutic diets, allergy screening, production
- **Why it is industrial:** Dietary-grade meal management with allergy safety

## JAVA-222 — Rehabilitation & Physiotherapy Tracker

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Rehab
- **Business problem:** Rehabilitation programs need treatment plans, progress scoring and session tracking.
- **Core engineering problem:** Rehab tracker with plans, progress metrics and session scheduling.
- **Architecture:** Modular monolith; plan engine; progress scoring; session scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (session events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Progress metrics, plans, scheduling
- **Why it is industrial:** Rehab-grade progress tracking with plan adherence

## JAVA-223 — Mental Health Intake & Safety Assessment

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Behavioral Health
- **Business problem:** Mental health intake needs risk assessment, safety planning and escalation protocols.
- **Core engineering problem:** Intake workflow with validated risk scoring, safety plans and escalation.
- **Architecture:** Modular monolith; intake workflow; risk engine; safety plan service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (risk events)
- **Security architecture:** RBAC, special-category data protection, escalation audit
- **Key advanced concepts:** Risk scoring, safety plans, escalation
- **Why it is industrial:** Behavioral-health intake with validated risk protocols

## JAVA-224 — Pathology Specimen Tracking & Barcode

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Pathology
- **Business problem:** Specimens must be tracked from collection to result with chain-of-custody and barcodes.
- **Core engineering problem:** Specimen tracking with barcode lifecycle, custody and result linkage.
- **Architecture:** Modular monolith; specimen service; custody ledger; result linking
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (specimen events)
- **Security architecture:** RBAC, custody audit, PHI controls
- **Key advanced concepts:** Custody chain, barcode lifecycle, linkage
- **Why it is industrial:** Pathology-grade custody tracking with result linkage

## JAVA-225 — Pharmacy Benefit Formulary & Tiering

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / PBM
- **Business problem:** Pharmacy benefit formularies must apply tiering, prior auth and step therapy rules.
- **Core engineering problem:** Formulary engine with tiering, PA rules and step therapy logic.
- **Architecture:** Modular monolith; formulary service; PA rules; step therapy engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (Rx events)
- **Security architecture:** RBAC, member PHI, audit
- **Key advanced concepts:** Tiering, step therapy, PA rules
- **Why it is industrial:** PBM-grade formulary processing with step therapy

## JAVA-226 — Clinical Trial Participant Recruitment

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / Clinical Trials
- **Business problem:** Trial recruitment must match patient criteria to protocols with consent tracking.
- **Core engineering problem:** Recruitment engine with criteria matching, site allocation and consent.
- **Architecture:** Modular monolith; recruitment engine; criteria matching; consent service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (recruit events)
- **Security architecture:** RBAC, de-identification, consent immutability
- **Key advanced concepts:** Criteria matching, site allocation, consent
- **Why it is industrial:** Trial-grade recruitment with protocol matching

## JAVA-227 — Clinical Data Collection & Validation (EDC)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / EDC
- **Business problem:** Clinical data capture needs edit checks, validation and audit of every data point.
- **Core engineering problem:** EDC with edit checks, source verification and complete audit trails.
- **Architecture:** Modular monolith; EDC engine; edit-check rules; audit store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (data events)
- **Security architecture:** RBAC, 21-CFR-style audit, e-signatures
- **Key advanced concepts:** Edit checks, SDV workflow, audit trails
- **Why it is industrial:** EDC-grade data capture with regulatory audit

## JAVA-228 — Trial Safety Reporting & SAE Pipeline

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / Safety
- **Business problem:** Serious adverse events must be reported within regulatory timelines with case narratives.
- **Core engineering problem:** SAE pipeline with intake, causality assessment and regulator reporting.
- **Architecture:** Modular monolith; SAE workflow; causality engine; report generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (SAE events)
- **Security architecture:** RBAC, patient privacy, deadline audit
- **Key advanced concepts:** Causality assessment, timelines, reports
- **Why it is industrial:** SAE-grade reporting with regulatory timelines

## JAVA-229 — Regulatory Submission Publishing (eCTD-style)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / Regulatory
- **Business problem:** Regulatory submissions must be assembled, validated and versioned (eCTD-style structure).
- **Core engineering problem:** Submission publishing with eCTD-style structure, validation and versions.
- **Architecture:** Modular monolith; submission builder; validator; version store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (publish jobs)
- **Security architecture:** RBAC, submission confidentiality, audit
- **Key advanced concepts:** eCTD-style structure, validation, versions
- **Why it is industrial:** Regulatory-grade submission publishing

## JAVA-230 — Drug Safety Pharmacovigilance Case Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / Pharmacovigilance
- **Business problem:** Adverse event cases must be triaged, assessed and reported with deduplication.
- **Core engineering problem:** PV case engine with triage, causality and regulator report generation.
- **Architecture:** Modular monolith; case workflow; triage engine; report service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (AE events)
- **Security architecture:** RBAC, patient privacy, deadline audit
- **Key advanced concepts:** Case triage, causality, deduplication
- **Why it is industrial:** PV-grade case processing with reporting deadlines

## JAVA-231 — Signal Detection over Adverse Events

- **Difficulty:** Expert (Tier 2)
- **Industry:** Pharma / Safety Science
- **Business problem:** Safety signals must be detected statistically over AE data with disproportionality analysis.
- **Core engineering problem:** Signal detection with disproportionality metrics and signal review workflow.
- **Architecture:** Modular monolith; signal engine; statistics; review workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (signal events)
- **Security architecture:** RBAC, data de-identification, audit
- **Key advanced concepts:** PRR/ROR statistics, signal triage
- **Why it is industrial:** Signal-detection analytics with statistical rigor

## JAVA-232 — Medical Coding Workbench (ICD/SNOMED-style)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Coding
- **Business problem:** Medical coders need assisted coding with terminology suggestions and compliance checks.
- **Core engineering problem:** Coding workbench with terminology suggestions, validation and audit queues.
- **Architecture:** Modular monolith; coding workflow; suggestion engine; validation rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (coding events)
- **Security architecture:** RBAC, coder QA sampling, audit
- **Key advanced concepts:** Code suggestions, validation, QA sampling
- **Why it is industrial:** Assisted coding with compliance validation

## JAVA-233 — Data Anonymization & De-Identification Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Privacy
- **Business problem:** Research datasets must be de-identified with measured re-identification risk.
- **Core engineering problem:** Anonymization service with k-anonymity-style checks, masking and risk reports.
- **Architecture:** Modular monolith; anonymization pipeline; risk engine; export service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (export jobs)
- **Security architecture:** RBAC, dual approval for exports, audit
- **Key advanced concepts:** De-identification, risk metrics, exports
- **Why it is industrial:** Privacy-grade de-identification with risk measurement

## JAVA-234 — Genomic Variant Interpretation Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Genomics
- **Business problem:** Genomic variants must be interpreted against knowledge bases with evidence levels.
- **Core engineering problem:** Variant interpretation with annotation, evidence linking and report generation.
- **Architecture:** Modular monolith; variant pipeline; annotation engine; report service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (variant jobs)
- **Security architecture:** RBAC, genomic-data special protection, audit
- **Key advanced concepts:** Variant annotation, evidence levels, reports
- **Why it is industrial:** Genomics-grade interpretation with evidence linkage

## JAVA-235 — Lab Instrument Data Integration Hub

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Lab IT
- **Business problem:** Lab instruments emit heterogeneous formats; a hub must normalize and route them.
- **Core engineering problem:** Instrument integration hub with adapters, normalization and result routing.
- **Architecture:** Modular monolith; adapter framework; normalization; routing rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (instrument messages)
- **Security architecture:** RBAC, instrument authentication, audit
- **Key advanced concepts:** Adapters, normalization, routing
- **Why it is industrial:** Instrument-grade integration with format normalization

## JAVA-236 — Sample Biobank & Consent Management

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Biobanking
- **Business problem:** Biobanks must manage samples with consent linkage and usage tracking.
- **Core engineering problem:** Sample registry with consent binding, storage locations and request workflow.
- **Architecture:** Modular monolith; sample registry; consent binding; request workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (sample events)
- **Security architecture:** RBAC, consent enforcement, chain-of-custody
- **Key advanced concepts:** Consent binding, storage maps, requests
- **Why it is industrial:** Biobank-grade sample governance with consent

## JAVA-237 — Research Cohort Query & Phenotyping

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Research
- **Business problem:** Researchers need cohort queries over de-identified data with phenotype definitions.
- **Core engineering problem:** Cohort query engine with phenotype definitions and export governance.
- **Architecture:** Modular monolith; query engine; phenotype store; export governance
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (export jobs)
- **Security architecture:** RBAC, de-identification, export approvals
- **Key advanced concepts:** Phenotype definitions, cohort queries, governance
- **Why it is industrial:** Cohort-grade querying with export governance

## JAVA-238 — Synthetic Patient Data Generator

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Research
- **Business problem:** Synthetic patients must be statistically realistic without real-PHI leakage.
- **Core engineering problem:** Synthetic data generator with statistical fidelity and leakage tests.
- **Architecture:** Modular monolith; generator engine; fidelity metrics; leakage checker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, leakage testing, audit
- **Key advanced concepts:** Statistical generation, fidelity metrics, leakage tests
- **Why it is industrial:** Synthetic-data generation with privacy verification

## JAVA-239 — Healthcare Interop Gateway (HL7/FHIR-style)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Interop
- **Business problem:** Hospitals exchange HL7/FHIR-style messages; a gateway must translate, validate and route.
- **Core engineering problem:** Interop gateway with HL7/FHIR-style parsing, validation and routing.
- **Architecture:** Modular monolith; message pipeline; parsers; routing rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (message streams)
- **Security architecture:** mTLS, message validation, audit
- **Key advanced concepts:** HL7/FHIR-style parsing, validation, routing
- **Why it is industrial:** Interop-grade message processing with standards validation

## JAVA-240 — FHIR Server & Resource Store

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Interop
- **Business problem:** A FHIR-style server must store, version and search clinical resources.
- **Core engineering problem:** FHIR-style resource store with versioning, search parameters and transactions.
- **Architecture:** Modular monolith; resource store; search engine; transaction bundle
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (resource events)
- **Security architecture:** OAuth2, SMART-style scopes, audit
- **Key advanced concepts:** Resource versioning, search, bundles
- **Why it is industrial:** FHIR-grade resource storage with SMART-style auth

## JAVA-241 — Prescription E-Signature & Audit Chain

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / eRx
- **Business problem:** Prescriptions need electronic signatures with cryptographic audit for legal validity.
- **Core engineering problem:** eRx service with signing workflow, hash chains and pharmacy transmission.
- **Architecture:** Modular monolith; Rx workflow; signing service; transmission
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (Rx events)
- **Security architecture:** e-signatures, prescriber identity, audit chain
- **Key advanced concepts:** Signing, hash chains, transmission
- **Why it is industrial:** Legally-valid e-prescribing with cryptographic audit

## JAVA-242 — Ambulance Dispatch & Crew Allocation

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / EMS
- **Business problem:** Ambulance dispatch must allocate crews by proximity, capability and hospital capacity.
- **Core engineering problem:** Dispatch engine with capability matching and hospital-capacity awareness.
- **Architecture:** Modular monolith; dispatch service; capability matching; capacity feed
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (dispatch events)
- **Security architecture:** RBAC, PHI minimalism, audit
- **Key advanced concepts:** Capability matching, proximity, capacity
- **Why it is industrial:** EMS-grade dispatch with capability constraints

## JAVA-243 — Home Health Visit Scheduling & Routing

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Home Health
- **Business problem:** Home health visits must be routed efficiently with clinician skills and travel windows.
- **Core engineering problem:** Visit routing with skill matching, travel windows and care continuity.
- **Architecture:** Modular monolith; routing solver; skill matching; continuity rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (visit events)
- **Security architecture:** RBAC, patient privacy, audit
- **Key advanced concepts:** Routing, skill matching, continuity
- **Why it is industrial:** Home-health routing with continuity constraints

## JAVA-244 — Telemedicine Consultation & Queue Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Telehealth
- **Business problem:** Telemedicine queues must match patients to available providers with wait management.
- **Core engineering problem:** Consultation queue with provider matching, wait times and escalation.
- **Architecture:** Modular monolith; queue engine; matching; escalation rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (queue events)
- **Security architecture:** RBAC, consultation privacy, audit
- **Key advanced concepts:** Queueing, provider matching, escalation
- **Why it is industrial:** Telehealth-grade queueing with provider matching

## JAVA-245 — Medical Device Registry & Recalls

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Device Safety
- **Business problem:** Medical device recalls must reach patients with affected-serial-number precision.
- **Core engineering problem:** Device registry with recall matching, patient linkage and notification workflow.
- **Architecture:** Modular monolith; device registry; recall engine; notification service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (recall events)
- **Security architecture:** RBAC, patient linkage privacy, audit
- **Key advanced concepts:** Serial matching, recall workflows, notifications
- **Why it is industrial:** Recall-grade device tracking with patient linkage

## JAVA-246 — Device Alerts & Alarm Fatigue Reducer

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Device Safety
- **Business problem:** Alarm floods cause fatigue; alerts must be deduplicated, prioritized and escalated.
- **Core engineering problem:** Alarm pipeline with dedup, priority scoring and escalation policies.
- **Architecture:** Modular monolith; alarm ingestion; dedup engine; escalation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (alarm streams)
- **Security architecture:** RBAC, ward scoping, escalation audit
- **Key advanced concepts:** Dedup, priority scoring, escalation
- **Why it is industrial:** Alarm-flood mitigation with priority escalation

## JAVA-247 — Infusion Pump Programming Guardrails

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Infusion Safety
- **Business problem:** Infusion pump programming must be guarded by drug-library rules with hard/soft limits.
- **Core engineering problem:** Guardrails service with drug libraries, limits and override auditing.
- **Architecture:** Modular monolith; guardrails engine; drug library; override audit
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (pump events)
- **Security architecture:** RBAC, override justification, audit
- **Key advanced concepts:** Hard/soft limits, drug libraries, overrides
- **Why it is industrial:** Infusion-safety guardrails with override governance

## JAVA-248 — Patient Consent & Data Use Ledger

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Privacy
- **Business problem:** Patient consent for data use must be captured, versioned and enforced across systems.
- **Core engineering problem:** Consent ledger with purpose-bound permissions, versioning and enforcement hooks.
- **Architecture:** Modular monolith; consent service; purpose registry; enforcement API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (consent events)
- **Security architecture:** OIDC, consent immutability, propagation
- **Key advanced concepts:** Purpose-bound consent, versions, enforcement
- **Why it is industrial:** Consent-grade data governance with enforcement

## JAVA-249 — Clinical Audit & Compliance Reviewer

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Compliance
- **Business problem:** Clinical audits must sample records, score compliance and track findings to closure.
- **Core engineering problem:** Audit workflow with sampling, scoring and finding remediation.
- **Architecture:** Modular monolith; sampling engine; scoring; finding workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (audit events)
- **Security architecture:** RBAC, auditor scoping, finding audit
- **Key advanced concepts:** Sampling, compliance scoring, remediation
- **Why it is industrial:** Clinical-audit workflows with remediation tracking

## JAVA-250 — Health Scorecard & Quality Measures

- **Difficulty:** Expert (Tier 2)
- **Industry:** Healthcare / Quality
- **Business problem:** Quality measures (readmissions, screening rates) must be computed reproducibly from records.
- **Core engineering problem:** Measure engine with definitions, calculation pipelines and benchmark reports.
- **Architecture:** Modular monolith; measure engine; calculation pipeline; reports
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (measure jobs)
- **Security architecture:** RBAC, aggregate de-identification, audit
- **Key advanced concepts:** Measure definitions, calculation, benchmarks
- **Why it is industrial:** Reproducible quality-measure computation

## JAVA-251 — Wellness Program & Habit Nudging Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Wellness
- **Business problem:** Wellness programs need habit tracking with nudges, streaks and personalized goals.
- **Core engineering problem:** Habit engine with nudging rules, streak logic and goal personalization.
- **Architecture:** Modular monolith; habit service; nudge rules; goal engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (nudge events)
- **Security architecture:** OIDC, wellness-data privacy, audit
- **Key advanced concepts:** Streak logic, nudge rules, goals
- **Why it is industrial:** Engagement-grade habit mechanics with privacy

## JAVA-252 — Insurance Member Portal & Benefits Explainer

- **Difficulty:** Architect (Tier 3)
- **Industry:** Insurance / Member Experience
- **Business problem:** Members need benefit explanations in plain language with cost estimates.
- **Core engineering problem:** Benefits explainer with cost-estimation engine and document explanations.
- **Architecture:** Modular monolith; benefits service; cost estimator; explainer engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (benefit events)
- **Security architecture:** OIDC, member PHI, audit
- **Key advanced concepts:** Cost estimation, plain-language explanation
- **Why it is industrial:** Member-grade benefits explanation with cost math

## JAVA-253 — Underwriting Health Risk Models Service

- **Difficulty:** Architect (Tier 3)
- **Industry:** Insurance / Health Analytics
- **Business problem:** Health underwriting models must be built, versioned and scored with governance.
- **Core engineering problem:** Underwriting model service with versioning, scoring and challenger models.
- **Architecture:** Modular monolith; model service; version registry; challenger pipeline
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scoring events)
- **Security architecture:** RBAC, model governance, audit
- **Key advanced concepts:** Model versioning, challenger tests, scoring
- **Why it is industrial:** Model-governed underwriting with challenger runs

## JAVA-254 — Medical Claim Fraud & Abuse Detection

- **Difficulty:** Architect (Tier 3)
- **Industry:** Insurance / Fraud
- **Business problem:** Medical claim fraud (upcoding, phantom services) needs pattern detection over claims.
- **Core engineering problem:** Fraud analytics with anomaly detection, peer comparison and case generation.
- **Architecture:** Modular monolith; analytics engine; peer groups; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (claim streams)
- **Security architecture:** RBAC, PHI controls, case audit
- **Key advanced concepts:** Anomaly detection, peer comparison, cases
- **Why it is industrial:** Claim-fraud detection with peer benchmarking

## JAVA-255 — Pharmacy Network & Reimbursement Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / PBM
- **Business problem:** Pharmacy network reimbursement must compute per claim with network rules and DIR fees.
- **Core engineering problem:** Reimbursement engine with network contracts, fees and reconciliation.
- **Architecture:** Modular monolith; reimbursement engine; contract rules; reconciliation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (claim events)
- **Security architecture:** RBAC, contract confidentiality, audit
- **Key advanced concepts:** Contract pricing, DIR fees, reconciliation
- **Why it is industrial:** Network-grade reimbursement with contract fidelity

## JAVA-256 — Medication Therapy Management Platform

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Pharmacy
- **Business problem:** Medication therapy management needs regimen reviews, interventions and documentation.
- **Core engineering problem:** MTM platform with regimen reviews, interventions and outcome tracking.
- **Architecture:** Modular monolith; regimen review; intervention workflow; outcomes
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (MTM events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Regimen reviews, interventions, outcomes
- **Why it is industrial:** MTM-grade care with intervention documentation

## JAVA-257 — Clinical Document De-Duplication & Mapper

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Clinical Docs
- **Business problem:** Duplicate clinical documents must be detected and mapped for longitudinal records.
- **Core engineering problem:** Document dedup with similarity matching and concept mapping.
- **Architecture:** Modular monolith; dedup pipeline; similarity engine; mapping service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (doc jobs)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Similarity matching, concept mapping
- **Why it is industrial:** Document deduplication with clinical mapping

## JAVA-258 — Care Team Collaboration & Secure Messaging

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Care Teams
- **Business problem:** Care teams need secure messaging with PHI protection, urgency and audit.
- **Core engineering problem:** Secure messaging with PHI-aware access, urgency routing and audit.
- **Architecture:** Modular monolith; messaging service; urgency routing; audit store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (message events)
- **Security architecture:** RBAC, PHI encryption, message audit
- **Key advanced concepts:** Urgency routing, PHI-aware access, audit
- **Why it is industrial:** Clinical-grade messaging with PHI governance

## JAVA-259 — Hospital Readmission Risk Predictor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Analytics
- **Business problem:** Readmission risk must be predicted at discharge with explainable factors.
- **Core engineering problem:** Risk prediction service with model scoring and factor explanations.
- **Architecture:** Modular monolith; prediction service; model pipeline; explanation engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (discharge events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Risk scoring, factor explanations, feedback
- **Why it is industrial:** Predictive readmission risk with explainability

## JAVA-260 — Discharge Summary & Aftercare Orchestrator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Care Transitions
- **Business problem:** Discharge summaries and aftercare must be orchestrated across providers and follow-ups.
- **Core engineering problem:** Discharge orchestration with task generation, follow-up scheduling and confirmations.
- **Architecture:** Modular monolith; discharge workflow; task engine; follow-up scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (discharge events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Task generation, follow-ups, confirmations
- **Why it is industrial:** Care-transition orchestration with follow-up loops

## JAVA-261 — Emergency Preparedness & Surge Planner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Emergency Mgmt
- **Business problem:** Hospitals must plan surge capacity for disasters with scenario simulation.
- **Core engineering problem:** Surge planning with scenario simulation, resource modeling and activation workflows.
- **Architecture:** Modular monolith; scenario simulator; resource model; activation workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (surge events)
- **Security architecture:** RBAC, plan confidentiality, audit
- **Key advanced concepts:** Scenario simulation, resource modeling
- **Why it is industrial:** Surge-grade planning with scenario simulation

## JAVA-262 — Medical Coding Audit & DRG Grouper

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Coding
- **Business problem:** DRG grouping must be correct, auditable and defended against payer audits.
- **Core engineering problem:** DRG grouper with logic trees, audit trails and denial-response support.
- **Architecture:** Modular monolith; grouper engine; logic trees; audit service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (coding events)
- **Security architecture:** RBAC, audit trails, denial workflows
- **Key advanced concepts:** DRG logic, audit trails, denials
- **Why it is industrial:** DRG-grade grouping with audit defensibility

## JAVA-263 — Clinical Terminology Server & Mapping

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Terminology
- **Business problem:** Clinical terminologies must map concepts across code systems with version governance.
- **Core engineering problem:** Terminology server with code systems, mappings and versioning.
- **Architecture:** Modular monolith; terminology store; mapping engine; version service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (mapping events)
- **Security architecture:** RBAC, version governance, audit
- **Key advanced concepts:** Code systems, mappings, versions
- **Why it is industrial:** Terminology-grade concept mapping with versions

## JAVA-264 — Patient Feedback & Experience Analytics

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Experience
- **Business problem:** Patient feedback must be analyzed for themes, sentiment and service recovery.
- **Core engineering problem:** Feedback analytics with theme extraction, sentiment and recovery workflows.
- **Architecture:** Modular monolith; feedback pipeline; analytics engine; recovery workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (feedback events)
- **Security architecture:** RBAC, de-identification, audit
- **Key advanced concepts:** Theme extraction, sentiment, recovery
- **Why it is industrial:** Experience analytics with service-recovery loops

## JAVA-265 — Dental Practice Imaging & Charting Hub

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Dental
- **Business problem:** Dental practices need imaging-linked charting with treatment plans and billing.
- **Core engineering problem:** Dental charting with imaging linkage, treatment plans and billing.
- **Architecture:** Modular monolith; charting service; imaging store; treatment plans
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (chart events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Imaging linkage, treatment plans, billing
- **Why it is industrial:** Dental-grade charting with imaging integration

## JAVA-266 — Optometry Exam & Prescription Records

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Vision
- **Business problem:** Optometry practices need exam records, prescriptions and lens-order workflows.
- **Core engineering problem:** Exam records with prescriptions, measurements and order workflows.
- **Architecture:** Modular monolith; exam service; prescription engine; order workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (order events)
- **Security architecture:** RBAC, PHI controls, audit
- **Key advanced concepts:** Measurements, prescriptions, orders
- **Why it is industrial:** Optometry-grade records with prescription integrity

## JAVA-267 — Medical Equipment Maintenance & Calibration

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Clinical Engineering
- **Business problem:** Medical equipment maintenance and calibration must be scheduled, evidenced and compliant.
- **Core engineering problem:** Equipment maintenance with calibration schedules, evidence and compliance.
- **Architecture:** Modular monolith; maintenance scheduler; calibration records; compliance
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (maintenance events)
- **Security architecture:** RBAC, evidence immutability, audit
- **Key advanced concepts:** Calibration schedules, evidence, compliance
- **Why it is industrial:** Clinical-engineering maintenance with calibration proof

## JAVA-268 — Clinical Guideline Repository & Versioning

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Clinical Knowledge
- **Business problem:** Clinical guidelines must be versioned, approved and distributed to care settings.
- **Core engineering problem:** Guideline repository with versioning, approval workflows and distribution.
- **Architecture:** Modular monolith; guideline store; approval workflow; distribution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (guideline events)
- **Security architecture:** RBAC, version governance, audit
- **Key advanced concepts:** Versioning, approvals, distribution
- **Why it is industrial:** Guideline-grade knowledge management with approvals

## JAVA-269 — Health Data Lakehouse Ingestion Pipeline

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Data Platform
- **Business problem:** Health data from many sources must land in a lakehouse with governance and lineage.
- **Core engineering problem:** Lakehouse ingestion with schema evolution, lineage and quality checks.
- **Architecture:** Modular monolith; ingestion pipeline; lineage store; quality engine
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO, OpenSearch 2
- **Messaging:** Kafka (data events)
- **Security architecture:** RBAC, de-identification zones, audit
- **Key advanced concepts:** Schema evolution, lineage, quality
- **Why it is industrial:** Lakehouse-grade ingestion with governance

## JAVA-270 — Wearable Data Integration & FHIR Mapping

- **Difficulty:** Architect (Tier 3)
- **Industry:** Healthcare / Wearables
- **Business problem:** Wearable data must be normalized, mapped to FHIR-style resources and shared with consent.
- **Core engineering problem:** Wearable integration with normalization, FHIR-style mapping and consent.
- **Architecture:** Modular monolith; device adapters; normalization; mapping service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (device streams)
- **Security architecture:** OIDC, consent enforcement, audit
- **Key advanced concepts:** Device adapters, FHIR-style mapping, consent
- **Why it is industrial:** Wearable-grade integration with standards mapping
