# Energy / Utilities / Grid — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-496 — Smart Grid SCADA Event Processor

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Grid
- **Business problem:** Grid SCADA-style events must be processed with state tracking and operator actions.
- **Core engineering problem:** Grid event processor with device state machines, event correlation and operator workflows.
- **Architecture:** Modular monolith; event pipeline; state engine; operator console
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (grid events)
- **Security architecture:** RBAC, operator four-eyes, audit
- **Key advanced concepts:** Device states, correlation, operator actions
- **Why it is industrial:** Grid-grade event processing with state integrity

## JAVA-497 — Advanced Metering Infrastructure Headend

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Metering
- **Business problem:** Smart meters must be managed with registration, reads and firmware campaigns.
- **Core engineering problem:** AMI headend with meter registry, read collection and firmware management.
- **Architecture:** Modular monolith; meter registry; read pipeline; firmware campaign
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (meter data)
- **Security architecture:** RBAC, device auth, audit
- **Key advanced concepts:** Meter lifecycle, reads, firmware
- **Why it is industrial:** AMI-grade headend with firmware campaigns

## JAVA-498 — Load Forecasting & Dispatch Planning

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Forecasting
- **Business problem:** Load must be forecast for dispatch with weather and calendar features.
- **Core engineering problem:** Load forecasting with models, weather features and error tracking.
- **Architecture:** Modular monolith; forecast pipeline; model service; error tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (load data)
- **Security architecture:** RBAC, forecast versioning, audit
- **Key advanced concepts:** Forecast models, weather features, errors
- **Why it is industrial:** Forecasting-grade accuracy with error tracking

## JAVA-499 — Virtual Power Plant Aggregator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / VPP
- **Business problem:** Distributed resources must be aggregated into a virtual power plant with dispatch.
- **Core engineering problem:** VPP aggregator with resource registry, dispatch and settlement.
- **Architecture:** Modular monolith; aggregator engine; dispatch service; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (VPP events)
- **Security architecture:** RBAC, resource auth, dispatch audit
- **Key advanced concepts:** Aggregation, dispatch, settlement
- **Why it is industrial:** VPP-grade aggregation with dispatch integrity

## JAVA-500 — Distributed Energy Resource Registry

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / DER
- **Business problem:** Distributed energy resources must be registered with capabilities and grid codes.
- **Core engineering problem:** DER registry with capability models, grid codes and interconnection workflows.
- **Architecture:** Modular monolith; DER registry; capability store; interconnection workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (DER events)
- **Security architecture:** RBAC, interconnection approvals, audit
- **Key advanced concepts:** Capabilities, grid codes, interconnections
- **Why it is industrial:** DER-grade registration with grid-code compliance

## JAVA-501 — Microgrid Control & Islanding Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Microgrids
- **Business problem:** Microgrids must manage islanding, load shedding and reconnection safely.
- **Core engineering problem:** Microgrid controller with island detection, shedding logic and reconnection.
- **Architecture:** Modular monolith; control engine; island logic; reconnection service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (microgrid events)
- **Security architecture:** RBAC, safety interlocks, audit
- **Key advanced concepts:** Islanding, load shedding, reconnection
- **Why it is industrial:** Microgrid-grade control with safety interlocks

## JAVA-502 — Battery Storage Optimization (BESS)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Storage
- **Business problem:** Battery storage must be optimized for arbitrage, degradation and grid services.
- **Core engineering problem:** BESS optimizer with cycling models, degradation and market signals.
- **Architecture:** Modular monolith; optimizer engine; battery models; market adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (BESS events)
- **Security architecture:** RBAC, dispatch audit, safety limits
- **Key advanced concepts:** Cycling optimization, degradation, arbitrage
- **Why it is industrial:** Storage-grade optimization with degradation models

## JAVA-503 — Electric Vehicle Charging Network Ops

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / EV Charging
- **Business problem:** EV charging networks must manage stations, sessions and roaming payments.
- **Core engineering problem:** Charging network OPS with station registry, sessions and payment settlement.
- **Architecture:** Modular monolith; station registry; session service; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (charge events)
- **Security architecture:** RBAC, station auth, payment audit
- **Key advanced concepts:** Sessions, roaming, settlement
- **Why it is industrial:** Charging-grade operations with roaming settlement

## JAVA-504 — Demand Response Event Orchestrator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / DR
- **Business problem:** Demand response events must be orchestrated with enrollment and performance verification.
- **Core engineering problem:** DR orchestrator with event dispatch, participation and baseline verification.
- **Architecture:** Modular monolith; event engine; enrollment service; verification
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (DR events)
- **Security architecture:** RBAC, participant scoping, audit
- **Key advanced concepts:** Event dispatch, baselines, verification
- **Why it is industrial:** DR-grade orchestration with baseline verification

## JAVA-505 — Time-of-Use Tariff & Billing Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Tariffs
- **Business problem:** Time-of-use tariffs must be modeled, versioned and applied to billing.
- **Core engineering problem:** Tariff engine with TOU periods, versioning and bill calculation.
- **Architecture:** Modular monolith; tariff store; TOU engine; bill calculator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (tariff events)
- **Security architecture:** RBAC, tariff versioning, audit
- **Key advanced concepts:** TOU periods, versions, billing
- **Why it is industrial:** Tariff-grade modeling with version control

## JAVA-506 — Net Metering & Solar Credit Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Solar
- **Business problem:** Net metering must track solar credits with true-up calculations.
- **Core engineering problem:** Solar credit ledger with net metering rules, true-ups and exports.
- **Architecture:** Modular monolith; credit ledger; true-up engine; export service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (meter events)
- **Security architecture:** RBAC, ledger integrity, audit
- **Key advanced concepts:** Net metering, true-ups, credits
- **Why it is industrial:** Solar-grade crediting with true-up integrity

## JAVA-507 — Power Market Bidding & Settlement

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Markets
- **Business problem:** Power market bidding must be simulated with clearing and settlement.
- **Core engineering problem:** Market simulator with bid matching, clearing prices and settlements.
- **Architecture:** Modular monolith; market engine; clearing service; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (bid events)
- **Security architecture:** RBAC, bid confidentiality, audit
- **Key advanced concepts:** Bid matching, clearing, settlement
- **Why it is industrial:** Market-grade simulation with clearing integrity

## JAVA-508 — Renewable Production Forecasting (wind/solar)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Renewables
- **Business problem:** Wind and solar production must be forecast with weather models and errors.
- **Core engineering problem:** Renewable production forecasting with weather ingestion and error metrics.
- **Architecture:** Modular monolith; forecast pipeline; weather adapter; error service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (weather data)
- **Security architecture:** RBAC, forecast versioning, audit
- **Key advanced concepts:** Weather models, forecast errors, ramp alerts
- **Why it is industrial:** Renewable-grade forecasting with ramp alerts

## JAVA-509 — Wind Turbine Condition Monitoring

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Wind
- **Business problem:** Wind turbines must be monitored for condition with SCADA-style signals.
- **Core engineering problem:** Turbine condition monitor with vibration analysis, alerts and work orders.
- **Architecture:** Modular monolith; telemetry pipeline; vibration analytics; work-order hook
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (turbine telemetry)
- **Security architecture:** RBAC, farm scoping, audit
- **Key advanced concepts:** Vibration analysis, alerts, work orders
- **Why it is industrial:** Wind-grade condition monitoring with work orders

## JAVA-510 — Solar Plant Performance Analytics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Solar
- **Business problem:** Solar plant performance must be compared to expected output with loss analysis.
- **Core engineering problem:** Solar performance analytics with PR computation and loss classification.
- **Architecture:** Modular monolith; performance engine; PR calculator; loss classifier
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (plant telemetry)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** PR computation, loss classification
- **Why it is industrial:** Solar-grade analytics with performance-ratio math

## JAVA-511 — Grid Outage Management System (OMS)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / OMS
- **Business problem:** Outage management must predict scope, dispatch crews and restore service.
- **Core engineering problem:** OMS with outage prediction, crew dispatch and restoration tracking.
- **Architecture:** Modular monolith; outage engine; prediction service; crew dispatch
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (outage events)
- **Security architecture:** RBAC, dispatch scoping, audit
- **Key advanced concepts:** Outage prediction, dispatch, restoration
- **Why it is industrial:** OMS-grade operations with prediction

## JAVA-512 — Distribution Automation & Switch Control

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Distribution
- **Business problem:** Distribution switches must be controlled with safety interlocks and authorization.
- **Core engineering problem:** Distribution automation with switch control, interlocks and switching orders.
- **Architecture:** Modular monolith; control engine; interlock logic; order workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (DA events)
- **Security architecture:** RBAC, switching four-eyes, audit
- **Key advanced concepts:** Switch control, interlocks, orders
- **Why it is industrial:** Distribution-grade automation with interlock safety

## JAVA-513 — Protection Relay Event Correlation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Protection
- **Business problem:** Protection relay events must be correlated for fault analysis.
- **Core engineering problem:** Relay event correlation with fault records, sequence analysis and reports.
- **Architecture:** Modular monolith; relay pipeline; correlation engine; fault reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (relay events)
- **Security architecture:** RBAC, protection scoping, audit
- **Key advanced concepts:** Event correlation, fault analysis, reports
- **Why it is industrial:** Protection-grade correlation with fault records

## JAVA-514 — Transformer Load & Health Analytics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Assets
- **Business problem:** Transformer loads must be monitored with health and thermal rating analytics.
- **Core engineering problem:** Transformer health with load monitoring, thermal models and alerts.
- **Architecture:** Modular monolith; load pipeline; thermal models; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (load data)
- **Security architecture:** RBAC, asset scoping, audit
- **Key advanced concepts:** Thermal rating, load trends, alerts
- **Why it is industrial:** Asset-grade health monitoring with thermal models

## JAVA-515 — Substation Condition & Thermal Rating

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Substations
- **Business problem:** Substation condition must be monitored with thermal and environmental data.
- **Core engineering problem:** Substation condition monitor with environmental telemetry and rating alerts.
- **Architecture:** Modular monolith; telemetry ingestion; rating engine; alerts
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (substation data)
- **Security architecture:** RBAC, substation scoping, audit
- **Key advanced concepts:** Thermal rating, environmental telemetry
- **Why it is industrial:** Substation-grade monitoring with rating alerts

## JAVA-516 — Energy Trading Desk & Position Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Trading
- **Business problem:** Energy trading desks need positions, curves and P&L across commodities.
- **Core engineering problem:** Trading position engine with curves, mark-to-market and P&L.
- **Architecture:** Modular monolith; position engine; curve service; P&L calculator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (trade events)
- **Security architecture:** RBAC, desk scoping, audit
- **Key advanced concepts:** Curves, MTM, P&L attribution
- **Why it is industrial:** Trading-grade position management with curves

## JAVA-517 — Gas Pipeline Flow & Nominations

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Gas
- **Business problem:** Gas pipeline flows must be nominated, scheduled and balanced.
- **Core engineering problem:** Gas nominations with flow scheduling, balancing and settlement.
- **Architecture:** Modular monolith; nomination engine; flow scheduler; balancing
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (flow data)
- **Security architecture:** RBAC, shipper scoping, audit
- **Key advanced concepts:** Nominations, balancing, settlement
- **Why it is industrial:** Gas-grade scheduling with balancing integrity

## JAVA-518 — Pipeline Leak Detection & Pressure Analytics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Pipelines
- **Business problem:** Pipeline leaks must be detected from pressure and flow anomalies.
- **Core engineering problem:** Leak detection with pressure analytics, anomaly scoring and alerts.
- **Architecture:** Modular monolith; pressure pipeline; anomaly engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (pressure data)
- **Security architecture:** RBAC, safety alerts, audit
- **Key advanced concepts:** Anomaly scoring, leak signatures, alerts
- **Why it is industrial:** Pipeline-grade leak detection with safety alerts

## JAVA-519 — LNG Terminal Berth & Regas Scheduling

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / LNG
- **Business problem:** LNG terminals must schedule berths, regasification and storage.
- **Core engineering problem:** LNG terminal scheduler with berth windows, regas plans and storage.
- **Architecture:** Modular monolith; berth scheduler; regas planner; storage model
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (LNG events)
- **Security architecture:** RBAC, four-eyes schedules, audit
- **Key advanced concepts:** Berth windows, regas plans, storage
- **Why it is industrial:** LNG-grade scheduling with storage modeling

## JAVA-520 — Oilfield Production Allocation Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Upstream
- **Business problem:** Oilfield production must be allocated per well with measurement and losses.
- **Core engineering problem:** Production allocation ledger with well tests, measurement and loss factors.
- **Architecture:** Modular monolith; allocation engine; well registry; measurement service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (well data)
- **Security architecture:** RBAC, allocation integrity, audit
- **Key advanced concepts:** Well tests, allocation math, losses
- **Why it is industrial:** Upstream-grade allocation with loss accounting

## JAVA-521 — Well Telemetry & Artificial Lift Monitor

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Upstream
- **Business problem:** Well telemetry must be monitored for artificial lift performance.
- **Core engineering problem:** Well telemetry monitor with lift analytics, alarms and workover triggers.
- **Architecture:** Modular monolith; telemetry pipeline; lift analytics; alarm service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (well telemetry)
- **Security architecture:** RBAC, field scoping, audit
- **Key advanced concepts:** Lift analytics, alarms, workovers
- **Why it is industrial:** Upstream-grade monitoring with workover triggers

## JAVA-522 — Tank Farm Inventory & Gauging Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Downstream
- **Business problem:** Tank farms must track inventory with gauging and transfer reconciliation.
- **Core engineering problem:** Tank inventory with gauging records, transfers and reconciliation.
- **Architecture:** Modular monolith; tank registry; gauging service; reconciliation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (tank events)
- **Security architecture:** RBAC, four-eyes transfers, audit
- **Key advanced concepts:** Gauging, transfers, reconciliation
- **Why it is industrial:** Downstream-grade inventory with reconciliation

## JAVA-523 — Refinery Blend Optimization & Lab Control

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Refining
- **Business problem:** Refinery blends must be optimized with lab constraints and specifications.
- **Core engineering problem:** Blend optimizer with component models, lab results and spec constraints.
- **Architecture:** Modular monolith; blend engine; lab integration; spec checker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (blend events)
- **Security architecture:** RBAC, blend approvals, audit
- **Key advanced concepts:** Blend optimization, lab constraints, specs
- **Why it is industrial:** Refining-grade blending with spec constraints

## JAVA-524 — Field Operator Rounds & Inspection App

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Field Ops
- **Business problem:** Field operators must complete rounds and inspections with digital evidence.
- **Core engineering problem:** Rounds app with inspection routes, checklists and evidence capture.
- **Architecture:** Modular monolith; route service; checklist engine; evidence store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (round events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Routes, checklists, evidence
- **Why it is industrial:** Field-grade inspections with evidence capture

## JAVA-525 — Workover Rig Scheduling & Logistics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Well Services
- **Business problem:** Workover rigs must be scheduled with logistics, crews and permits.
- **Core engineering problem:** Rig scheduler with logistics constraints, crew planning and permits.
- **Architecture:** Modular monolith; rig scheduler; logistics service; permit checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (rig events)
- **Security architecture:** RBAC, permit enforcement, audit
- **Key advanced concepts:** Scheduling, logistics, permits
- **Why it is industrial:** Well-service-grade scheduling with permit gates

## JAVA-526 — Power Plant Operations Logbook

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Power Plants
- **Business problem:** Power plant operations must be logged with shift handovers and events.
- **Core engineering problem:** Operations logbook with shift logs, events and compliance records.
- **Architecture:** Modular monolith; logbook service; shift workflow; compliance store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (log events)
- **Security architecture:** RBAC, log immutability, audit
- **Key advanced concepts:** Shift logs, events, compliance
- **Why it is industrial:** Plant-grade logging with compliance records

## JAVA-527 — Outage Permit & Switching Order Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Safety
- **Business problem:** Switching orders must be issued, executed and confirmed with isolation verification.
- **Core engineering problem:** Switching order engine with step execution, isolation checks and confirmation.
- **Architecture:** Modular monolith; order workflow; step engine; isolation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (switching events)
- **Security architecture:** RBAC, four-eyes execution, audit
- **Key advanced concepts:** Step execution, isolations, confirmation
- **Why it is industrial:** Safety-grade switching with isolation verification

## JAVA-528 — Generator Dispatch & Unit Commitment

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Dispatch
- **Business problem:** Generator dispatch must respect unit commitment, ramp rates and reserves.
- **Core engineering problem:** Dispatch engine with unit commitment, ramps and reserve margins.
- **Architecture:** Modular monolith; dispatch optimizer; ramp models; reserve service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (dispatch events)
- **Security architecture:** RBAC, dispatch audit, four-eyes
- **Key advanced concepts:** Unit commitment, ramps, reserves
- **Why it is industrial:** Dispatch-grade optimization with reserve margins

## JAVA-529 — Emissions Allowance & Carbon Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Carbon
- **Business problem:** Carbon emissions must be tracked with allowance ledgers and offsets.
- **Core engineering problem:** Carbon ledger with allowance tracking, offsets and compliance reports.
- **Architecture:** Modular monolith; allowance ledger; offset registry; report engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (carbon events)
- **Security architecture:** RBAC, ledger integrity, audit
- **Key advanced concepts:** Allowances, offsets, compliance
- **Why it is industrial:** Carbon-grade accounting with compliance reports

## JAVA-530 — Utility Customer Service & Move-In Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Retail
- **Business problem:** Utility customers must move in/out with service orders and billing transitions.
- **Core engineering problem:** Move-in/move-out engine with service orders, reads and billing transitions.
- **Architecture:** Modular monolith; MIMO workflow; service orders; billing hook
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (MIMO events)
- **Security architecture:** RBAC, customer PII, audit
- **Key advanced concepts:** Service orders, reads, transitions
- **Why it is industrial:** Retail-grade MIMO with billing transitions

## JAVA-531 — Meter Data Validation, Estimation & Editing

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Metering
- **Business problem:** Meter data must be validated, estimated and edited (VEE) before billing.
- **Core engineering problem:** VEE engine with validation rules, estimation and editing workflows.
- **Architecture:** Modular monolith; VEE pipeline; estimation engine; edit workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (meter data)
- **Security architecture:** RBAC, edit approvals, audit
- **Key advanced concepts:** Validation, estimation, editing
- **Why it is industrial:** Metering-grade VEE with edit governance

## JAVA-532 — Grid Event Playback & Forensic Replay

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Forensics
- **Business problem:** Grid events must be replayable for post-mortem analysis and training.
- **Core engineering problem:** Event replay platform with time-travel queries, replay and forensics.
- **Architecture:** Modular monolith; event store; replay engine; forensics API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (grid events)
- **Security architecture:** RBAC, replay governance, audit
- **Key advanced concepts:** Event sourcing, replay, forensics
- **Why it is industrial:** Forensics-grade replay with time-travel queries

## JAVA-533 — Fault Location, Isolation & Service Restoration

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Distribution
- **Business problem:** Fault location, isolation and service restoration must be automated safely.
- **Core engineering problem:** FLISR engine with fault location, isolation logic and restoration plans.
- **Architecture:** Modular monolith; FLISR engine; isolation service; restoration planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (fault events)
- **Security architecture:** RBAC, restoration approvals, audit
- **Key advanced concepts:** Fault location, isolation, restoration
- **Why it is industrial:** Distribution-grade FLISR with safety approvals

## JAVA-534 — Crew Management & Storm Response

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Storm Ops
- **Business problem:** Storm response must manage crews, mutual aid and priority restoration.
- **Core engineering problem:** Crew management with mutual-aid coordination, priorities and logistics.
- **Architecture:** Modular monolith; crew registry; priority engine; logistics service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (storm events)
- **Security architecture:** RBAC, mutual-aid scoping, audit
- **Key advanced concepts:** Crew allocation, priorities, logistics
- **Why it is industrial:** Storm-grade response with mutual-aid coordination

## JAVA-535 — Vegetation Management & Clearance Tracking

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Vegetation
- **Business problem:** Vegetation management must track clearances, risks and work cycles.
- **Core engineering problem:** Vegetation tracker with risk scoring, work cycles and compliance.
- **Architecture:** Modular monolith; vegetation registry; risk engine; work planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (work events)
- **Security architecture:** RBAC, compliance scoping, audit
- **Key advanced concepts:** Risk scoring, work cycles, compliance
- **Why it is industrial:** Vegetation-grade management with risk scoring

## JAVA-536 — Grid Asset Health & Reinvestment Planning

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Investment
- **Business problem:** Grid asset health must drive reinvestment planning with risk-based priorities.
- **Core engineering problem:** Asset health scoring with reinvestment planning and budget scenarios.
- **Architecture:** Modular monolith; health engine; investment planner; scenarios
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (asset events)
- **Security architecture:** RBAC, budget scoping, audit
- **Key advanced concepts:** Health scoring, planning, scenarios
- **Why it is industrial:** Investment-grade planning with risk-based priorities

## JAVA-537 — Substation Commissioning Checklist Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Commissioning
- **Business problem:** Substation commissioning must run checklists with evidence and sign-offs.
- **Core engineering problem:** Commissioning engine with checklists, evidence capture and sign-offs.
- **Architecture:** Modular monolith; checklist engine; evidence store; sign-off workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (commissioning events)
- **Security architecture:** RBAC, sign-off integrity, audit
- **Key advanced concepts:** Checklists, evidence, sign-offs
- **Why it is industrial:** Commissioning-grade checklists with evidence

## JAVA-538 — Energy Efficiency Audits & Retrofit Pipeline

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Efficiency
- **Business problem:** Energy audits must be turned into retrofit projects with savings verification.
- **Core engineering problem:** Audit-to-retrofit pipeline with savings models, projects and verification.
- **Architecture:** Modular monolith; audit workflow; savings model; project service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (audit events)
- **Security architecture:** RBAC, auditor scoping, audit
- **Key advanced concepts:** Savings models, projects, verification
- **Why it is industrial:** Efficiency-grade pipelines with savings verification

## JAVA-539 — Utility Revenue Protection & Theft Analytics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Revenue Protection
- **Business problem:** Energy theft must be detected from consumption anomalies and tamper events.
- **Core engineering problem:** Theft analytics with anomaly detection, tamper correlation and cases.
- **Architecture:** Modular monolith; anomaly engine; tamper correlation; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (meter data)
- **Security architecture:** RBAC, case confidentiality, audit
- **Key advanced concepts:** Anomaly detection, tamper correlation, cases
- **Why it is industrial:** Revenue-protection-grade analytics with cases

## JAVA-540 — Smart City Lighting Control

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Smart City / Lighting
- **Business problem:** City lighting must be controlled with schedules, sensors and energy optimization.
- **Core engineering problem:** Lighting control with schedules, motion sensing and energy analytics.
- **Architecture:** Modular monolith; control engine; sensor ingestion; energy analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, device auth, audit
- **Key advanced concepts:** Schedules, sensors, energy analytics
- **Why it is industrial:** Smart-city-grade lighting with energy optimization

## JAVA-541 — District Heating Network Optimization

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / District Heating
- **Business problem:** District heating networks must be optimized for flow, temperature and demand.
- **Core engineering problem:** Heating optimizer with network models, demand forecasting and dispatch.
- **Architecture:** Modular monolith; network model; forecast engine; dispatch service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (heat data)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Network models, forecasting, dispatch
- **Why it is industrial:** Heating-grade optimization with network models

## JAVA-542 — Hydro Plant Unit Efficiency Curves

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Hydro
- **Business problem:** Hydro units must track efficiency curves and dispatch against water constraints.
- **Core engineering problem:** Hydro efficiency engine with unit curves, water constraints and dispatch.
- **Architecture:** Modular monolith; efficiency engine; water model; dispatch service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (hydro events)
- **Security architecture:** RBAC, dispatch audit, four-eyes
- **Key advanced concepts:** Unit curves, water constraints, dispatch
- **Why it is industrial:** Hydro-grade efficiency tracking with dispatch

## JAVA-543 — Nuclear Outage Work Package Manager

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Nuclear
- **Business problem:** Nuclear outage work packages must be planned with safety and regulatory rigor.
- **Core engineering problem:** Outage work package manager with scheduling, permits and regulatory records.
- **Architecture:** Modular monolith; work package engine; permit service; regulatory store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (outage events)
- **Security architecture:** RBAC, strict permit controls, audit
- **Key advanced concepts:** Work packages, permits, regulatory records
- **Why it is industrial:** Nuclear-grade planning with regulatory rigor

## JAVA-544 — Carbon Intensity Real-Time Display

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / Sustainability
- **Business problem:** Real-time carbon intensity must be displayed with source mix and forecasts.
- **Core engineering problem:** Carbon intensity service with source mix, intensity computation and forecasts.
- **Architecture:** Modular monolith; source mix pipeline; intensity engine; forecast service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (generation data)
- **Security architecture:** RBAC, public/private APIs, audit
- **Key advanced concepts:** Source mix, intensity math, forecasts
- **Why it is industrial:** Sustainability-grade intensity with public APIs

## JAVA-545 — Peer-to-Peer Local Energy Trading

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Energy / P2P
- **Business problem:** Local energy trading must settle peer-to-peer with grid constraints.
- **Core engineering problem:** P2P energy exchange with order matching, grid checks and settlement.
- **Architecture:** Modular monolith; matching engine; grid check service; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (trade events)
- **Security architecture:** RBAC, settlement integrity, audit
- **Key advanced concepts:** Order matching, grid checks, settlement
- **Why it is industrial:** P2P-grade trading with grid-aware settlement
