# Manufacturing / Industrial IoT / Robotics — Catalog

75 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-271 — Manufacturing Execution System (MES)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Execution
- **Business problem:** Shop-floor execution must track orders, routes and quality in real time while keeping traceability.
- **Core engineering problem:** MES work-order execution with routing steps, quality gates and traceability.
- **Architecture:** Modular monolith; work-order engine; route execution; quality gates
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (shop-floor events)
- **Security architecture:** RBAC, station authentication, electronic signatures
- **Key advanced concepts:** Route execution, quality gates, genealogy
- **Why it is industrial:** Shop-floor-grade execution with genealogy tracking

## JAVA-272 — Predictive Maintenance Intelligence Platform

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Maintenance
- **Business problem:** Unplanned downtime must be predicted from sensor patterns with actionable maintenance windows.
- **Core engineering problem:** Predictive maintenance with anomaly detection, RUL estimation and work-order generation.
- **Architecture:** Modular monolith; anomaly engine; RUL models; work-order generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (sensor streams)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Anomaly detection, RUL, auto work orders
- **Why it is industrial:** Predictive maintenance with explainable alerts

## JAVA-273 — Digital Twin of a Production Line

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Digital Twin
- **Business problem:** A production line's digital twin must mirror state, run what-ifs and predict bottlenecks.
- **Core engineering problem:** Digital twin with state mirroring, simulation and bottleneck prediction.
- **Architecture:** Modular monolith; twin model; simulator; prediction engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (telemetry)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** State mirroring, what-if simulation, bottleneck detection
- **Why it is industrial:** Digital-twin fidelity with what-if simulation

## JAVA-274 — SCADA Gateway & Historian Replay

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Automation
- **Business problem:** SCADA-style systems need a gateway, historian replay and alarm handling without a real plant.
- **Core engineering problem:** SCADA gateway simulator with historian, replay and alarm processing.
- **Architecture:** Modular monolith; protocol simulator; historian; alarm engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (telemetry, alarms)
- **Security architecture:** RBAC, operator scoping, alarm audit
- **Key advanced concepts:** Historian compression, replay, alarm handling
- **Why it is industrial:** SCADA-grade historian with replay and alarm audit

## JAVA-275 — Batch Recipe Management & Execution

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Process
- **Business problem:** Batch processes must follow recipes with parameters, deviations and electronic signatures.
- **Core engineering problem:** Recipe execution with parameter capture, deviation handling and e-signatures.
- **Architecture:** Modular monolith; recipe engine; execution workflow; deviation handler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (batch events)
- **Security architecture:** RBAC, e-signatures, deviation audit
- **Key advanced concepts:** Recipe versioning, parameters, deviations
- **Why it is industrial:** Batch-grade recipe execution with signature compliance

## JAVA-276 — Overall Equipment Effectiveness (OEE) Analytics

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Analytics
- **Business problem:** OEE must be computed from machine events with correct loss classification.
- **Core engineering problem:** OEE engine with event-based availability, performance and quality computation.
- **Architecture:** Modular monolith; event pipeline; OEE calculator; loss classification
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (machine events)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** OEE math, loss trees, drill-downs
- **Why it is industrial:** OEE-grade analytics with loss-tree drill-downs

## JAVA-277 — Statistical Process Control (SPC) Monitor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** SPC charts must detect process shifts in real time with rule-based alarms.
- **Core engineering problem:** SPC monitor with control charts, Nelson-style rules and alarm workflows.
- **Architecture:** Modular monolith; SPC engine; chart computation; alarm rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (measurement streams)
- **Security architecture:** RBAC, quality scoping, audit
- **Key advanced concepts:** Control charts, run rules, alarms
- **Why it is industrial:** SPC-grade monitoring with statistical run rules

## JAVA-278 — Work Order Lifecycle & Labor Tracking

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Execution
- **Business problem:** Work orders must track labor, materials and progress with real-time visibility.
- **Core engineering problem:** Work-order lifecycle with labor tracking, material consumption and progress views.
- **Architecture:** Modular monolith; work-order service; labor tracker; material consumption
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (progress events)
- **Security architecture:** RBAC, operator identity, audit
- **Key advanced concepts:** Labor tracking, consumption, progress
- **Why it is industrial:** Execution-grade work orders with cost capture

## JAVA-279 — Machine Downtime Tracking & Pareto Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Analytics
- **Business problem:** Downtime events must be classified and Pareto-analyzed to target improvements.
- **Core engineering problem:** Downtime tracking with reason codes, Pareto analysis and improvement workflows.
- **Architecture:** Modular monolith; downtime engine; reason taxonomy; Pareto analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (downtime events)
- **Security architecture:** RBAC, reason-code governance, audit
- **Key advanced concepts:** Reason codes, Pareto, action tracking
- **Why it is industrial:** Downtime analytics with reason-code governance

## JAVA-280 — Tool Life & Cutter Wear Management

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Machining
- **Business problem:** Tool wear must be tracked per cutting tool with life prediction and replacement alerts.
- **Core engineering problem:** Tool-life management with wear tracking, life models and replacement planning.
- **Architecture:** Modular monolith; tool registry; wear tracking; life prediction
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (machine events)
- **Security architecture:** RBAC, tooling scoping, audit
- **Key advanced concepts:** Wear tracking, life prediction, replacements
- **Why it is industrial:** Tooling-grade life management with prediction

## JAVA-281 — CNC Program Versioning & Distribution

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Machining
- **Business problem:** CNC programs must be versioned, validated and distributed to machines securely.
- **Core engineering problem:** CNC program vault with versioning, validation and secure distribution.
- **Architecture:** Modular monolith; program vault; validator; distribution service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (program events)
- **Security architecture:** RBAC, checksum verification, machine auth
- **Key advanced concepts:** Versioning, validation, secure distribution
- **Why it is industrial:** Program-grade vault with machine-safe distribution

## JAVA-282 — Robot Cell Mission Orchestrator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Robotics
- **Business problem:** Robot cell missions must be orchestrated with collision avoidance and exception handling.
- **Core engineering problem:** Mission orchestration with task sequences, interlocks and exception recovery.
- **Architecture:** Modular monolith; mission engine; interlock logic; recovery handler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (robot events)
- **Security architecture:** RBAC, safety interlocks, audit
- **Key advanced concepts:** Mission sequences, interlocks, recovery
- **Why it is industrial:** Robot-grade orchestration with safety interlocks

## JAVA-283 — Quality Inspection Results & AQL Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** Inspection results must be recorded with AQL sampling and accept/reject decisions.
- **Core engineering problem:** Inspection engine with AQL sampling plans, results and disposition workflows.
- **Architecture:** Modular monolith; inspection service; AQL engine; disposition workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (inspection events)
- **Security architecture:** RBAC, inspector identity, audit
- **Key advanced concepts:** AQL plans, sampling, dispositions
- **Why it is industrial:** Inspection-grade sampling with disposition governance

## JAVA-284 — Non-Conformance & CAPA Workflow

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** Non-conformances must flow to CAPA with containment and effectiveness checks.
- **Core engineering problem:** NC-to-CAPA workflow with containment, root cause and effectiveness verification.
- **Architecture:** Modular monolith; NC workflow; CAPA engine; effectiveness checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (quality events)
- **Security architecture:** RBAC, electronic sign-off, audit
- **Key advanced concepts:** 8D-style flow, effectiveness, trends
- **Why it is industrial:** Closed-loop NC/CAPA with effectiveness verification

## JAVA-285 — First Article Inspection (FAI) Package

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** First article inspection packages must be assembled, reviewed and dispositioned.
- **Core engineering problem:** FAI package engine with ballooned drawing data, measurements and dispositions.
- **Architecture:** Modular monolith; FAI workflow; measurement capture; disposition engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (FAI events)
- **Security architecture:** RBAC, signature chain, audit
- **Key advanced concepts:** FAI packages, measurements, dispositions
- **Why it is industrial:** FAI-grade package assembly with signature chains

## JAVA-286 — Materials Traceability & Genealogy Ledger

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Traceability
- **Business problem:** Materials must be traced from receipt to finished goods with full genealogy.
- **Core engineering problem:** Lot genealogy with consumption records, queries and recall readiness.
- **Architecture:** Modular monolith; material ledger; genealogy engine; recall queries
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (material events)
- **Security architecture:** RBAC, immutable consumption records, audit
- **Key advanced concepts:** Genealogy, consumption records, recalls
- **Why it is industrial:** Genealogy-grade traceability with recall queries

## JAVA-287 — Bill of Materials & Engineering Change Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Engineering
- **Business problem:** BOMs and engineering changes must propagate with effectivity dates and impact analysis.
- **Core engineering problem:** BOM management with ECN workflows, effectivity and impact analysis.
- **Architecture:** Modular monolith; BOM engine; ECN workflow; impact analyzer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (ECN events)
- **Security architecture:** RBAC, change approvals, audit
- **Key advanced concepts:** BOM versions, effectivity, ECNs
- **Why it is industrial:** Engineering-grade BOM control with effectivity

## JAVA-288 — Configuration Management & Variant Explosion

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Configuration
- **Business problem:** Configurable products need variant generation with constraint validation.
- **Core engineering problem:** Variant explosion with feature constraints, rules and validation.
- **Architecture:** Modular monolith; configuration engine; rule validation; variant generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (variant events)
- **Security architecture:** RBAC, product-line scoping, audit
- **Key advanced concepts:** Feature models, constraint validation, variants
- **Why it is industrial:** Configuration-grade variant management with rules

## JAVA-289 — Shelf-Life & Expiry-Aware Inventory

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Inventory
- **Business problem:** Shelf-life and expiry-aware inventory must respect FEFO and quality holds.
- **Core engineering problem:** Expiry-aware inventory with FEFO allocation and hold management.
- **Architecture:** Modular monolith; inventory engine; FEFO allocation; hold service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (stock events)
- **Security architecture:** RBAC, hold governance, audit
- **Key advanced concepts:** FEFO logic, expiry, holds
- **Why it is industrial:** Inventory-grade expiry control with FEFO allocation

## JAVA-290 — Kanban Replenishment & eKanban Boards

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Lean
- **Business problem:** Kanban replenishment must signal, track and optimize supermarket loops.
- **Core engineering problem:** eKanban boards with signal tracking, replenishment and lead-time analytics.
- **Architecture:** Modular monolith; kanban engine; board service; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (kanban events)
- **Security architecture:** RBAC, cell scoping, audit
- **Key advanced concepts:** Kanban signals, replenishment, lead times
- **Why it is industrial:** Lean-grade kanban management with analytics

## JAVA-291 — Just-in-Time Sequencing & Line Feeding

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Sequencing
- **Business problem:** JIT line feeding must sequence parts to stations exactly in build order.
- **Core engineering problem:** Sequencing engine with build-order computation and feeder validation.
- **Architecture:** Modular monolith; sequence engine; line-feed logic; validation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (sequence events)
- **Security architecture:** RBAC, line scoping, audit
- **Key advanced concepts:** Build-order sequencing, feeder validation
- **Why it is industrial:** JIT-grade sequencing with line-feed validation

## JAVA-292 — Yard Management & Dock Scheduling

- **Difficulty:** Architect (Tier 3)
- **Industry:** Logistics / Yard
- **Business problem:** Yard management must schedule docks, track trailers and manage gates.
- **Core engineering problem:** Yard engine with dock scheduling, trailer tracking and gate automation simulation.
- **Architecture:** Modular monolith; yard service; dock scheduler; gate simulator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (yard events)
- **Security architecture:** RBAC, gate auth, audit
- **Key advanced concepts:** Dock scheduling, trailer tracking, gates
- **Why it is industrial:** Yard-grade operations with gate automation

## JAVA-293 — Returns & Refurbishment Line Planning

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Refurbishment
- **Business problem:** Returned units must be triaged, refurbished and re-certified with full history.
- **Core engineering problem:** Refurbishment line planning with triage, work steps and re-certification.
- **Architecture:** Modular monolith; triage engine; refurb workflow; re-certification
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (unit events)
- **Security architecture:** RBAC, unit history integrity, audit
- **Key advanced concepts:** Triage, refurb steps, re-certification
- **Why it is industrial:** Refurbishment-grade triage with certification

## JAVA-294 — Label Printing & Serialization Station

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Serialization
- **Business problem:** Labels must be generated, serialized and verified with anti-counterfeit codes.
- **Core engineering problem:** Label station with serialization, checksum codes and verification.
- **Architecture:** Modular monolith; label engine; serialization; verification service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (label events)
- **Security architecture:** RBAC, code integrity, audit
- **Key advanced concepts:** Serialization, checksums, verification
- **Why it is industrial:** Serialization-grade labeling with verification

## JAVA-295 — Palletizing & Pack Pattern Optimizer

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Packing
- **Business problem:** Pallet patterns must be optimized for stability and transport efficiency.
- **Core engineering problem:** Palletization engine with pattern search, stability checks and load planning.
- **Architecture:** Modular monolith; pattern solver; stability checker; load planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (packing events)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Pattern optimization, stability, loads
- **Why it is industrial:** Palletization-grade pattern optimization

## JAVA-296 — Energy & Compressed-Air Consumption Analytics

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Energy
- **Business problem:** Compressed-air and energy consumption must be analyzed for waste reduction.
- **Core engineering problem:** Energy analytics with consumption baselines, waste detection and alerts.
- **Architecture:** Modular monolith; ingestion pipeline; baseline engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (meter streams)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Baselines, waste detection, alerts
- **Why it is industrial:** Energy-grade analytics with waste detection

## JAVA-297 — Emissions Monitoring & ESG Reporting

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / ESG
- **Business problem:** Emissions monitoring must track sources, compute totals and support ESG audits.
- **Core engineering problem:** Emissions engine with source tracking, calculation and audit exports.
- **Architecture:** Modular monolith; source registry; calculation engine; export service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (emission events)
- **Security architecture:** RBAC, factor versioning, audit
- **Key advanced concepts:** Source tracking, calculations, exports
- **Why it is industrial:** ESG-grade emissions accounting with audit exports

## JAVA-298 — Hazardous Material Handling & Storage

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / EHS
- **Business problem:** Hazardous materials must be stored, used and documented per safety regulations.
- **Core engineering problem:** Hazmat management with storage rules, usage tracking and documentation.
- **Architecture:** Modular monolith; hazmat registry; storage rules; usage ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (usage events)
- **Security architecture:** RBAC, safety sign-offs, audit
- **Key advanced concepts:** Storage rules, usage tracking, SDS docs
- **Why it is industrial:** Hazmat-grade compliance with usage ledger

## JAVA-299 — Permit to Work & LOTO Safety System

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Safety
- **Business problem:** Permits to work and LOTO must gate hazardous work with approvals and verification.
- **Core engineering problem:** PTW/LOTO workflow with permit types, isolations and verification steps.
- **Architecture:** Modular monolith; permit workflow; LOTO engine; verification
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (permit events)
- **Security architecture:** RBAC, four-eyes, isolation audit
- **Key advanced concepts:** Permit states, isolations, verification
- **Why it is industrial:** Safety-grade permit control with LOTO verification

## JAVA-300 — Shift Handover & Operator Logbook

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Ops
- **Business problem:** Shift handovers must transfer context, risks and tasks between crews without loss.
- **Core engineering problem:** Handover logbook with context capture, risk flags and task transfer.
- **Architecture:** Modular monolith; logbook service; handover workflow; task transfer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (handover events)
- **Security architecture:** RBAC, crew scoping, audit
- **Key advanced concepts:** Context capture, risk flags, transfers
- **Why it is industrial:** Ops-grade handover with context integrity

## JAVA-301 — Operator Skills Matrix & Certification Ledger

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / HR
- **Business problem:** Operator skills and certifications must gate work assignments with expiry monitoring.
- **Core engineering problem:** Skills matrix with certification tracking, expiry and work gating.
- **Architecture:** Modular monolith; skills registry; certification tracking; gating service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (cert events)
- **Security architecture:** RBAC, evidence immutability, audit
- **Key advanced concepts:** Certifications, expiry, work gating
- **Why it is industrial:** Skills-grade gating with certification evidence

## JAVA-302 — Standard Work Instructions & Digital Workbench

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Digital Workbench
- **Business problem:** Standard work instructions must be versioned, displayed and confirmed at stations.
- **Core engineering problem:** Digital workbench with SWI versions, step confirmation and deviation capture.
- **Architecture:** Modular monolith; SWI store; step engine; confirmation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (step events)
- **Security architecture:** RBAC, electronic confirmation, audit
- **Key advanced concepts:** SWI versions, step confirmation, deviations
- **Why it is industrial:** Workbench-grade SWI with confirmed execution

## JAVA-303 — Andon Escalation & Help Chain

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Andon
- **Business problem:** Andon escalations must route by help chain with timers and automatic escalation.
- **Core engineering problem:** Andon engine with help-chain routing, timers and escalation policies.
- **Architecture:** Modular monolith; andon service; help-chain engine; escalation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (andon events)
- **Security architecture:** RBAC, escalation audit, station scoping
- **Key advanced concepts:** Help chains, timers, escalations
- **Why it is industrial:** Andon-grade escalation with timer-driven routing

## JAVA-304 — Error-Proofing (Poka-Yoke) Station Validation

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** Poka-yoke stations must validate operations before release with sensor confirmation.
- **Core engineering problem:** Error-proofing validation with sensor checks, sequences and release gates.
- **Architecture:** Modular monolith; validation engine; sensor checks; release gates
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (station events)
- **Security architecture:** RBAC, release authorization, audit
- **Key advanced concepts:** Sensor confirmation, sequences, gates
- **Why it is industrial:** Poka-yoke-grade validation with release gates

## JAVA-305 — Defect Root-Cause Analysis Workspace

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** Defect root causes must be analyzed with structured tools and shared learnings.
- **Core engineering problem:** Root-cause workspace with 5-why/ishikawa tools and learning repository.
- **Architecture:** Modular monolith; RCA workflow; analysis tools; learning store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (RCA events)
- **Security architecture:** RBAC, team scoping, audit
- **Key advanced concepts:** 5-why, fishbone, learning search
- **Why it is industrial:** RCA-grade analysis with organizational learning

## JAVA-306 — FMEA & Risk Register Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Risk
- **Business problem:** FMEA must be maintained with risk scoring, actions and re-evaluation cycles.
- **Core engineering problem:** FMEA engine with RPN scoring, action tracking and re-evaluation.
- **Architecture:** Modular monolith; FMEA workflow; RPN engine; action tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (FMEA events)
- **Security architecture:** RBAC, team review gates, audit
- **Key advanced concepts:** RPN scoring, actions, re-evaluation
- **Why it is industrial:** FMEA-grade risk management with action loops

## JAVA-307 — 8D Problem-Solving Workflow

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Quality
- **Business problem:** 8D reports must be structured, timed and verified for closure.
- **Core engineering problem:** 8D workflow with D-stage tracking, timing and closure verification.
- **Architecture:** Modular monolith; 8D workflow; stage tracker; closure checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (8D events)
- **Security architecture:** RBAC, four-eyes closure, audit
- **Key advanced concepts:** D-stage tracking, timing, verification
- **Why it is industrial:** 8D-grade problem solving with verified closure

## JAVA-308 — Supplier Incoming Inspection & Skip-Lot

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Incoming Quality
- **Business problem:** Supplier incoming inspection must adapt sampling based on supplier history.
- **Core engineering problem:** Skip-lot engine with supplier ratings, sampling plans and holds.
- **Architecture:** Modular monolith; inspection scheduler; skip-lot rules; hold service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (lot events)
- **Security architecture:** RBAC, supplier scoping, audit
- **Key advanced concepts:** Skip-lot logic, ratings, holds
- **Why it is industrial:** Incoming-grade inspection with adaptive sampling

## JAVA-309 — Test Stand Orchestration & Result Broker

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Test
- **Business problem:** Test stands must orchestrate test sequences, capture results and broker pass/fail.
- **Core engineering problem:** Test orchestration with sequences, result capture and disposition routing.
- **Architecture:** Modular monolith; test orchestrator; sequence engine; result broker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (test events)
- **Security architecture:** RBAC, test-result integrity, audit
- **Key advanced concepts:** Test sequences, results, dispositions
- **Why it is industrial:** Test-grade orchestration with result integrity

## JAVA-310 — Calibration Management & Recall Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Metrology
- **Business problem:** Calibration schedules must be managed with due-date prediction and recall of gages.
- **Core engineering problem:** Calibration engine with schedules, due prediction and gage recall.
- **Architecture:** Modular monolith; calibration scheduler; due engine; recall service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (cal events)
- **Security architecture:** RBAC, calibration evidence, audit
- **Key advanced concepts:** Due prediction, schedules, recalls
- **Why it is industrial:** Metrology-grade calibration with recall automation

## JAVA-311 — Gauge R&R Study Automation

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Metrology
- **Business problem:** Gauge R&R studies must be computed, reported and gated for gage approval.
- **Core engineering problem:** GR&R engine with variance decomposition and gage approval gates.
- **Architecture:** Modular monolith; GR&R calculator; report engine; approval gates
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (study events)
- **Security architecture:** RBAC, study sign-off, audit
- **Key advanced concepts:** Variance decomposition, reports, gates
- **Why it is industrial:** Metrology-grade GR&R with approval gates

## JAVA-312 — CMM & Metrology Data Management

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Metrology
- **Business problem:** CMM and metrology data must be stored, analyzed and linked to inspection plans.
- **Core engineering problem:** Metrology data management with plan linkage and tolerance analytics.
- **Architecture:** Modular monolith; data store; plan linkage; tolerance engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (measurement events)
- **Security architecture:** RBAC, data integrity, audit
- **Key advanced concepts:** Plan linkage, tolerance checks, analytics
- **Why it is industrial:** Metrology-grade data with tolerance analytics

## JAVA-313 — Shop-Floor Edge Gateway & Protocol Router

- **Difficulty:** Architect (Tier 3)
- **Industry:** Industrial IoT / Edge
- **Business problem:** Edge devices must route protocols (MQTT/Modbus-style) to the cloud with buffering.
- **Core engineering problem:** Edge gateway with protocol adapters, buffering and offline queueing.
- **Architecture:** Modular monolith; protocol adapters; buffer store; forwarding
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Integration, Flyway
- **Data layer:** PostgreSQL 16, SQLite (edge mode)
- **Messaging:** MQTT broker (Mosquitto)
- **Security architecture:** Device auth, TLS, message integrity
- **Key advanced concepts:** Protocol adapters, offline buffering, backpressure
- **Why it is industrial:** Edge-grade protocol routing with offline resilience

## JAVA-314 — Wireless Sensor Network Health Monitor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Industrial IoT / Networks
- **Business problem:** Wireless sensor network health must be monitored with battery and link analytics.
- **Core engineering problem:** WSN health monitor with battery prediction, link quality and topologies.
- **Architecture:** Modular monolith; telemetry ingestion; battery models; topology views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, device identity, audit
- **Key advanced concepts:** Battery prediction, link quality, topologies
- **Why it is industrial:** WSN-grade monitoring with battery prediction

## JAVA-315 — Environmental Chamber Profile Control

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Test
- **Business problem:** Environmental chambers must follow temperature/humidity profiles with tolerance control.
- **Core engineering problem:** Chamber profile engine with setpoint control simulation and deviation alerts.
- **Architecture:** Modular monolith; profile engine; setpoint simulator; deviation alerts
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, profile integrity, audit
- **Key advanced concepts:** Profiles, setpoints, deviations
- **Why it is industrial:** Chamber-grade profile control with deviation handling

## JAVA-316 — Manufacturing Costing & Variances Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Finance
- **Business problem:** Manufacturing costs and variances must be computed from actual consumption and standards.
- **Core engineering problem:** Costing engine with standard costs, actuals and variance analysis.
- **Architecture:** Modular monolith; costing service; variance engine; period close
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (cost events)
- **Security architecture:** RBAC, cost confidentiality, audit
- **Key advanced concepts:** Standard vs actual, variances, closes
- **Why it is industrial:** Costing-grade variance analysis with period integrity

## JAVA-317 — Capacity Rough-Cut Planning (RCCP)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Planning
- **Business problem:** Rough-cut capacity planning must check feasibility before detailed scheduling.
- **Core engineering problem:** RCCP engine with capacity buckets, load profiles and feasibility checks.
- **Architecture:** Modular monolith; capacity model; load profiler; feasibility engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (plan events)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Capacity buckets, loads, feasibility
- **Why it is industrial:** Planning-grade RCCP with feasibility gates

## JAVA-318 — Sequencing Solver for Mixed-Model Lines

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Sequencing
- **Business problem:** Mixed-model line sequencing must balance workload with changeover minimization.
- **Core engineering problem:** Sequencing solver with workload balancing and changeover constraints.
- **Architecture:** Modular monolith; solver engine; workload model; changeover rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (sequence events)
- **Security architecture:** RBAC, planner approval, audit
- **Key advanced concepts:** Constraint solving, balancing, changeovers
- **Why it is industrial:** Sequencing-grade optimization with changeover rules

## JAVA-319 — Industrial Weighbridge & Catchweight

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Weighbridge
- **Business problem:** Weighbridge operations must capture weights with fraud controls and invoicing.
- **Core engineering problem:** Weighbridge system with weight capture, fraud checks and invoicing.
- **Architecture:** Modular monolith; weighbridge service; fraud rules; invoice engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (weigh events)
- **Security architecture:** RBAC, weight tamper detection, audit
- **Key advanced concepts:** Catchweight capture, fraud checks, invoicing
- **Why it is industrial:** Weighbridge-grade capture with fraud controls

## JAVA-320 — Process Historian with Compression

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Historian
- **Business problem:** Process data must be stored with compression for years while remaining queryable.
- **Core engineering problem:** Historian with swing-door compression, tiered storage and queries.
- **Architecture:** Modular monolith; ingestion pipeline; compression engine; query API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (process data)
- **Security architecture:** RBAC, data integrity, audit
- **Key advanced concepts:** Compression algorithms, tiering, queries
- **Why it is industrial:** Historian-grade compression with query performance

## JAVA-321 — Alarm Rationalization & Flood Filtering

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Alarm Mgmt
- **Business problem:** Alarm rationalization must reduce floods, prioritize and track alarm performance.
- **Core engineering problem:** Alarm engine with rationalization workflows, prioritization and KPIs.
- **Architecture:** Modular monolith; alarm registry; rationalization workflow; KPI engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (alarm events)
- **Security architecture:** RBAC, rationalization approvals, audit
- **Key advanced concepts:** Rationalization, prioritization, alarm KPIs
- **Why it is industrial:** Alarm-management-grade rationalization with KPIs

## JAVA-322 — Cobot Safety Zone & Speed Supervision

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Robotics
- **Business problem:** Cobot safety zones must be monitored with speed and distance supervision.
- **Core engineering problem:** Cobot safety supervision with zone monitoring and stop logic.
- **Architecture:** Modular monolith; zone engine; speed supervision; stop logic
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, safety stop audit, zone integrity
- **Key advanced concepts:** Zone geometry, speed limits, stops
- **Why it is industrial:** Safety-grade cobot supervision with stop integrity

## JAVA-323 — AGV Fleet Traffic Management

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Intralogistics
- **Business problem:** AGV fleets must route and avoid deadlocks with traffic management.
- **Core engineering problem:** AGV traffic management with routing, reservation and deadlock prevention.
- **Architecture:** Modular monolith; traffic engine; routing solver; reservation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (AGV events)
- **Security architecture:** RBAC, fleet scoping, audit
- **Key advanced concepts:** Routing, reservations, deadlock prevention
- **Why it is industrial:** Fleet-grade traffic management with deadlock prevention

## JAVA-324 — Additive Manufacturing Job Queue

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Additive
- **Business problem:** Additive manufacturing jobs must be queued, sliced-parameterized and tracked.
- **Core engineering problem:** AM job queue with build parameters, nesting and print tracking.
- **Architecture:** Modular monolith; job queue; parameter store; print tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (print events)
- **Security architecture:** RBAC, IP protection, audit
- **Key advanced concepts:** Job queueing, parameters, tracking
- **Why it is industrial:** AM-grade job management with build parameters

## JAVA-325 — Surface Treatment & Plating Recipe Control

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Finishing
- **Business problem:** Surface treatment recipes must control baths, cycles and quality checks.
- **Core engineering problem:** Plating recipe control with bath tracking, cycles and quality gates.
- **Architecture:** Modular monolith; recipe engine; bath tracker; quality gates
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (process events)
- **Security architecture:** RBAC, recipe integrity, audit
- **Key advanced concepts:** Bath tracking, cycles, quality gates
- **Why it is industrial:** Finishing-grade process control with bath chemistry

## JAVA-326 — Packaging Line Changeover Optimizer

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Packing
- **Business problem:** Packaging line changeovers must be minimized with sequence optimization.
- **Core engineering problem:** Changeover optimizer with product clustering and sequence planning.
- **Architecture:** Modular monolith; optimizer engine; product clustering; sequence plan
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (line events)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Clustering, changeover math, sequences
- **Why it is industrial:** Packing-grade changeover optimization

## JAVA-327 — Serialization & Track-and-Trace (pharma grade)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Pharma / Serialization
- **Business problem:** Pharma serialization must track units through the supply chain with DSCSA-style integrity.
- **Core engineering problem:** Track-and-trace with serial numbers, aggregation and verification.
- **Architecture:** Modular monolith; serialization ledger; aggregation; verification API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scan events)
- **Security architecture:** RBAC, tamper-evident serials, audit
- **Key advanced concepts:** Serialization, aggregation, verification
- **Why it is industrial:** Pharma-grade track-and-trace with aggregation

## JAVA-328 — Warranty Claims & Field Quality Analytics

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Warranty
- **Business problem:** Warranty claims must be adjudicated with failure analytics feeding quality.
- **Core engineering problem:** Warranty engine with claim adjudication, failure analytics and supplier chargeback.
- **Architecture:** Modular monolith; claim workflow; adjudication rules; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (claim events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Adjudication, failure analytics, chargebacks
- **Why it is industrial:** Warranty-grade adjudication with supplier recovery

## JAVA-329 — Spare Parts Criticality & Reorder Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / MRO
- **Business problem:** Spare parts criticality must drive reorder policies with downtime-cost awareness.
- **Core engineering problem:** Parts criticality engine with reorder policies and downtime-cost models.
- **Architecture:** Modular monolith; criticality engine; reorder service; cost models
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (stock events)
- **Security architecture:** RBAC, criticality governance, audit
- **Key advanced concepts:** Criticality scoring, reorder policies, cost models
- **Why it is industrial:** MRO-grade parts management with cost awareness

## JAVA-330 — RCM: Reliability-Centered Maintenance

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Reliability
- **Business problem:** Reliability-centered maintenance must analyze failure modes to choose strategies.
- **Core engineering problem:** RCM engine with failure-mode analysis and strategy selection.
- **Architecture:** Modular monolith; RCM workflow; FMEA linkage; strategy engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (RCM events)
- **Security architecture:** RBAC, analysis sign-off, audit
- **Key advanced concepts:** Failure modes, strategies, intervals
- **Why it is industrial:** RCM-grade analysis with strategy optimization

## JAVA-331 — Maintenance Backlog & Shutdown Planner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Turnarounds
- **Business problem:** Shutdown maintenance must be planned with backlog prioritization and resource allocation.
- **Core engineering problem:** Shutdown planner with backlog, resource leveling and critical-path views.
- **Architecture:** Modular monolith; backlog service; leveling engine; schedule views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (shutdown events)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Backlog prioritization, leveling, critical path
- **Why it is industrial:** Turnaround-grade planning with resource leveling

## JAVA-332 — Work Permit Integration & Gas Testing

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Safety
- **Business problem:** Confined-space work needs permits with gas testing and standby verification.
- **Core engineering problem:** Permit engine with gas-testing records, standby checks and expirations.
- **Architecture:** Modular monolith; permit workflow; gas-test records; standby checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (permit events)
- **Security architecture:** RBAC, four-eyes, time-bound permits
- **Key advanced concepts:** Gas tests, standby, expirations
- **Why it is industrial:** Safety-grade permit control with gas-test evidence

## JAVA-333 — Digital Work Order for Contractors

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Contractor Mgmt
- **Business problem:** Contractor work orders must flow digitally with certifications and time capture.
- **Core engineering problem:** Contractor work orders with certification checks, time capture and approvals.
- **Architecture:** Modular monolith; work-order service; certification check; time capture
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (WO events)
- **Security architecture:** RBAC, contractor scoping, audit
- **Key advanced concepts:** Certification gates, time capture, approvals
- **Why it is industrial:** Contractor-grade work orders with certification gates

## JAVA-334 — Batch Genealogy & Recall Readiness

- **Difficulty:** Architect (Tier 3)
- **Industry:** Pharma / Batch Records
- **Business problem:** Batch genealogy must support instant recall readiness with full production history.
- **Core engineering problem:** Batch genealogy with production history, quality data and recall queries.
- **Architecture:** Modular monolith; batch ledger; history service; recall engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (batch events)
- **Security architecture:** RBAC, record immutability, audit
- **Key advanced concepts:** Genealogy, quality linkage, recalls
- **Why it is industrial:** Batch-grade genealogy with recall readiness

## JAVA-335 — Production Line Reconfiguration Simulator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Simulation
- **Business problem:** Line reconfiguration must be simulated before committing capital.
- **Core engineering problem:** Line simulation with discrete-event models, what-ifs and throughput prediction.
- **Architecture:** Modular monolith; DES engine; line models; what-if service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (sim events)
- **Security architecture:** RBAC, scenario scoping, audit
- **Key advanced concepts:** Discrete-event simulation, what-ifs, throughput
- **Why it is industrial:** Simulation-grade line modeling with what-if analysis

## JAVA-336 — In-Plant Logistics Milk-Run Planner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Intralogistics
- **Business problem:** Milk-run routes must be planned for internal material delivery with time windows.
- **Core engineering problem:** Milk-run planner with route optimization, time windows and cart capacity.
- **Architecture:** Modular monolith; route solver; time-window engine; cart tracking
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (delivery events)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Route optimization, time windows, capacity
- **Why it is industrial:** Intralogistics-grade routing with time windows

## JAVA-337 — Smart Container & Bin Tracking

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Intralogistics
- **Business problem:** Smart containers and bins must be tracked with fill levels and location.
- **Core engineering problem:** Container tracking with fill-level telemetry, location and cycle counting.
- **Architecture:** Modular monolith; container registry; telemetry ingestion; cycle count
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, asset integrity, audit
- **Key advanced concepts:** Fill levels, location, cycle counting
- **Why it is industrial:** Container-grade tracking with cycle-count integration

## JAVA-338 — Material Flow Simulation Sandbox

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Simulation
- **Business problem:** Material flow must be simulated to find bottlenecks before layout changes.
- **Core engineering problem:** Material-flow sandbox with queue models, bottlenecks and layout what-ifs.
- **Architecture:** Modular monolith; flow simulator; queue models; bottleneck detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (sim events)
- **Security architecture:** RBAC, scenario scoping, audit
- **Key advanced concepts:** Queue models, bottlenecks, layouts
- **Why it is industrial:** Simulation-grade flow modeling with bottleneck detection

## JAVA-339 — Electronic Batch Record (EBR) Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Pharma / Compliance
- **Business problem:** Electronic batch records must be assembled with signatures and regulatory compliance.
- **Core engineering problem:** EBR engine with step records, signatures and exception handling.
- **Architecture:** Modular monolith; EBR workflow; signature service; exception handler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (EBR events)
- **Security architecture:** RBAC, e-signatures, audit trail
- **Key advanced concepts:** Step records, signatures, exceptions
- **Why it is industrial:** EBR-grade record assembly with signature integrity

## JAVA-340 — Set-Up Time Reduction (SMED) Planner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Lean
- **Business problem:** Setup times must be reduced with SMED analysis and standardized changeover plans.
- **Core engineering problem:** SMED planner with internal/external task analysis and changeover plans.
- **Architecture:** Modular monolith; SMED workflow; task analyzer; plan service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (changeover events)
- **Security architecture:** RBAC, team scoping, audit
- **Key advanced concepts:** Internal/external tasks, plans, timing
- **Why it is industrial:** Lean-grade SMED planning with task analysis

## JAVA-341 — Tool Crib & Vending Machine Inventory

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Tooling
- **Business problem:** Tool cribs must manage issue/return with vending automation and accountability.
- **Core engineering problem:** Tool crib with vending simulation, issue/return and accountability reports.
- **Architecture:** Modular monolith; crib service; vending simulator; accountability engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (tool events)
- **Security architecture:** RBAC, operator identity, audit
- **Key advanced concepts:** Issue/return, vending, accountability
- **Why it is industrial:** Tooling-grade crib management with accountability

## JAVA-342 — Thermal Process Profiling (reflow-style)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Thermal
- **Business problem:** Thermal processes (reflow-style) must follow profiles with zone control and traceability.
- **Core engineering problem:** Thermal profiling with zone setpoints, profile adherence and traceability.
- **Architecture:** Modular monolith; profile engine; zone simulator; traceability store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, profile integrity, audit
- **Key advanced concepts:** Zone control, profile adherence, traceability
- **Why it is industrial:** Thermal-grade profiling with zone-level traceability

## JAVA-343 — Scrap & Yield Reconciliation Ledger

- **Difficulty:** Architect (Tier 3)
- **Industry:** Manufacturing / Yield
- **Business problem:** Scrap and yield must be reconciled per batch to cost and quality accounts.
- **Core engineering problem:** Scrap/yield ledger with reconciliation, root-cause tags and cost allocation.
- **Architecture:** Modular monolith; yield ledger; reconciliation engine; cost allocation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scrap events)
- **Security architecture:** RBAC, ledger integrity, audit
- **Key advanced concepts:** Yield math, reconciliation, cost tags
- **Why it is industrial:** Yield-grade reconciliation with cost allocation

## JAVA-344 — Regulatory Audit Trail for Food Safety

- **Difficulty:** Architect (Tier 3)
- **Industry:** Food / Safety
- **Business problem:** Food safety programs need regulatory audit trails from intake to shipping.
- **Core engineering problem:** Food-safety audit trail with HACCP-style checkpoints and regulatory exports.
- **Architecture:** Modular monolith; checkpoint engine; audit store; regulatory exports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (checkpoint events)
- **Security architecture:** RBAC, sign-offs, immutable records
- **Key advanced concepts:** Checkpoints, audit trails, exports
- **Why it is industrial:** Food-grade safety with regulator-ready audit trails

## JAVA-345 — Food Safety HACCP Monitoring Platform

- **Difficulty:** Architect (Tier 3)
- **Industry:** Food / Safety
- **Business problem:** HACCP monitoring must track CCPs with limits, deviations and corrective actions.
- **Core engineering problem:** HACCP platform with CCP monitoring, limit checks and corrective actions.
- **Architecture:** Modular monolith; CCP registry; monitor service; CA workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, deviation audit, sign-offs
- **Key advanced concepts:** CCP limits, deviations, corrections
- **Why it is industrial:** HACCP-grade monitoring with corrective-action loops
