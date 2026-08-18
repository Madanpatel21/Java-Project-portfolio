# Automotive / Aerospace / Transportation — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-546 — Automotive ECU Diagnostics Gateway (DoIP-style)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Automotive / Diagnostics
- **Business problem:** Vehicle ECUs must be diagnosed remotely with DoIP-style sessions and DTC handling.
- **Core engineering problem:** Diagnostics gateway with session management, DTC parsing and test sequences.
- **Architecture:** Modular monolith; diagnostics engine; DTC store; session service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (DTC events)
- **Security architecture:** mTLS vehicle auth, RBAC, audit
- **Key advanced concepts:** DoIP-style sessions, DTCs, test sequences
- **Why it is industrial:** Diagnostics-grade gateway with secure sessions

## JAVA-547 — Vehicle Telematics & Trip Analytics

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Automotive / Telematics
- **Business problem:** Vehicle telematics must be ingested at fleet scale with trip analytics.
- **Core engineering problem:** Telematics platform with trip segmentation, driver scoring and alerts.
- **Architecture:** Modular monolith; ingestion pipeline; trip engine; scoring service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (telemetry streams)
- **Security architecture:** Device identity, PII controls, audit
- **Key advanced concepts:** Trip segmentation, scoring, geofencing
- **Why it is industrial:** Telematics-grade ingestion with trip intelligence

## JAVA-548 — Connected Car OTA Update Campaigns

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Automotive / OTA
- **Business problem:** Connected cars need OTA update campaigns with staged rollouts and rollback.
- **Core engineering problem:** OTA campaign engine with targeting, staging, eligibility and rollback.
- **Architecture:** Modular monolith; campaign service; targeting engine; rollout control
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (OTA events)
- **Security architecture:** Signed firmware, vehicle auth, audit
- **Key advanced concepts:** Staged rollouts, eligibility, rollback
- **Why it is industrial:** OTA-grade campaigns with signed firmware

## JAVA-549 — Autonomous Vehicle Log Mining Lab

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Automotive / Autonomous
- **Business problem:** AV fleets generate logs that must be mined for scenarios and edge cases.
- **Core engineering problem:** AV log mining with scenario extraction, indexing and replay.
- **Architecture:** Modular monolith; log pipeline; scenario extractor; replay service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2, MinIO
- **Messaging:** Kafka (log streams)
- **Security architecture:** RBAC, data isolation, audit
- **Key advanced concepts:** Scenario extraction, indexing, replay
- **Why it is industrial:** AV-grade log mining with scenario search

## JAVA-550 — ADAS Scenario Replay & Annotation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Automotive / ADAS
- **Business problem:** ADAS scenarios must be replayed and annotated for validation.
- **Core engineering problem:** Scenario replay with annotation tools, diffing and coverage analytics.
- **Architecture:** Modular monolith; replay engine; annotation service; coverage analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (scenario events)
- **Security architecture:** RBAC, project scoping, audit
- **Key advanced concepts:** Replay, annotation, coverage
- **Why it is industrial:** ADAS-grade validation tooling with coverage

## JAVA-551 — Fleet Driver Safety Scoring

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / Safety
- **Business problem:** Fleet drivers must be scored for safety with coaching interventions.
- **Core engineering problem:** Driver safety scoring with event weighting, coaching and improvement tracking.
- **Architecture:** Modular monolith; scoring engine; event pipeline; coaching workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (safety events)
- **Security architecture:** RBAC, driver privacy, audit
- **Key advanced concepts:** Event weighting, coaching, trends
- **Why it is industrial:** Safety-grade scoring with coaching loops

## JAVA-552 — Vehicle Recall & Campaign Manager

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / Recalls
- **Business problem:** Vehicle recalls must reach owners with VIN precision and repair tracking.
- **Core engineering problem:** Recall campaign engine with VIN matching, notifications and repair tracking.
- **Architecture:** Modular monolith; campaign engine; VIN matcher; repair tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (recall events)
- **Security architecture:** RBAC, owner PII, audit
- **Key advanced concepts:** VIN matching, notifications, repair rates
- **Why it is industrial:** Recall-grade campaigns with repair tracking

## JAVA-553 — Dealer Service Lane & Appointment Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / Dealership
- **Business problem:** Dealer service lanes must schedule, upsell and track vehicles efficiently.
- **Core engineering problem:** Service lane engine with appointment scheduling, inspections and approvals.
- **Architecture:** Modular monolith; scheduling engine; inspection workflow; approval service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (lane events)
- **Security architecture:** RBAC, dealer scoping, audit
- **Key advanced concepts:** Scheduling, inspections, approvals
- **Why it is industrial:** Dealer-grade operations with inspection workflows

## JAVA-554 — Warranty Analytics & Claim Adjudication

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / Warranty
- **Business problem:** Warranty claims must be adjudicated against coverage with fraud detection.
- **Core engineering problem:** Warranty adjudication with coverage rules, fraud scoring and supplier recovery.
- **Architecture:** Modular monolith; claim workflow; coverage engine; fraud scorer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (claim events)
- **Security architecture:** RBAC, four-eyes, audit
- **Key advanced concepts:** Coverage rules, fraud scoring, recovery
- **Why it is industrial:** Warranty-grade adjudication with fraud scoring

## JAVA-555 — Tire Pressure & Health Monitoring Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / TPMS
- **Business problem:** Tire health must be monitored from pressure data with proactive alerts.
- **Core engineering problem:** TPMS analytics with pressure trends, leak detection and maintenance alerts.
- **Architecture:** Modular monolith; pressure pipeline; leak detector; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (TPMS data)
- **Security architecture:** RBAC, fleet scoping, audit
- **Key advanced concepts:** Leak detection, trends, alerts
- **Why it is industrial:** TPMS-grade monitoring with leak detection

## JAVA-556 — EV Battery Telemetry & Degradation Analytics

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / EV
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** EV batteries must be monitored for degradation and range prediction.
- **Core engineering problem:** Battery telemetry with degradation models, range prediction and alerts.
- **Architecture:** Modular monolith; telemetry pipeline; degradation models; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (battery data)
- **Security architecture:** RBAC, battery data protection, audit
- **Key advanced concepts:** Degradation modeling, range prediction, alerts
- **Why it is industrial:** EV-grade battery analytics with degradation models

## JAVA-557 — Battery Swap Station Orchestrator

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / Battery Swap
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Battery swap stations must orchestrate inventory, charging and reservations.
- **Core engineering problem:** Swap station orchestrator with battery inventory, charging plans and reservations.
- **Architecture:** Modular monolith; station service; inventory engine; reservation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (swap events)
- **Security architecture:** RBAC, battery auth, audit
- **Key advanced concepts:** Inventory, charging plans, reservations
- **Why it is industrial:** Swap-grade orchestration with charging plans

## JAVA-558 — Vehicle-to-Grid Session Ledger

- **Difficulty:** Omega (Tier 5)
- **Industry:** Automotive / V2G
- **Business problem:** Vehicle-to-grid sessions must be metered, settled and grid-constrained.
- **Core engineering problem:** V2G ledger with session metering, grid checks and settlement.
- **Architecture:** Modular monolith; session ledger; grid check service; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (V2G events)
- **Security architecture:** RBAC, ledger integrity, audit
- **Key advanced concepts:** Session metering, grid checks, settlement
- **Why it is industrial:** V2G-grade ledgering with grid constraints

## JAVA-559 — Ride-Hailing Dispatch & Matching Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Ride-Hailing
- **Business problem:** Ride requests must be matched to drivers with ETA prediction and pricing.
- **Core engineering problem:** Dispatch engine with matching optimization, ETA models and dynamic pricing.
- **Architecture:** Modular monolith; matching engine; ETA service; pricing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext), Redis 7
- **Messaging:** Kafka (ride events)
- **Security architecture:** OIDC, device verification, audit
- **Key advanced concepts:** Matching, ETA prediction, pricing
- **Why it is industrial:** Ride-hailing-grade dispatch with matching quality

## JAVA-560 — Dynamic Surge Pricing & Incentive Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Pricing
- **Business problem:** Surge pricing must balance supply and demand with fairness and caps.
- **Core engineering problem:** Dynamic pricing engine with demand sensing, surge logic and caps.
- **Architecture:** Modular monolith; demand engine; pricing rules; cap service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (demand events)
- **Security architecture:** RBAC, price-change audit, fairness rules
- **Key advanced concepts:** Demand sensing, surge, caps
- **Why it is industrial:** Pricing-grade surge control with fairness caps

## JAVA-561 — Carpool Matching & Trust Profiles

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Carpooling
- **Business problem:** Carpool matching must verify identities and trust profiles for safety.
- **Core engineering problem:** Carpool engine with route matching, trust profiles and verification.
- **Architecture:** Modular monolith; matching engine; trust service; verification workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (carpool events)
- **Security architecture:** OIDC, identity verification, audit
- **Key advanced concepts:** Route matching, trust, verification
- **Why it is industrial:** Carpool-grade matching with trust verification

## JAVA-562 — Bus Network Planning & Headway Analysis

- **Difficulty:** Omega (Tier 5)
- **Industry:** Public Transit / Planning
- **Business problem:** Bus networks must be planned with headway analysis and crowding data.
- **Core engineering problem:** Network planner with headway optimization, crowding analytics and schedules.
- **Architecture:** Modular monolith; schedule engine; crowding pipeline; headway optimizer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (AVL events)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Headway optimization, crowding, schedules
- **Why it is industrial:** Transit-grade planning with headway optimization

## JAVA-563 — Real-Time Transit Passenger Information

- **Difficulty:** Omega (Tier 5)
- **Industry:** Public Transit / Info
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Real-time passenger information must predict arrivals and disruptions.
- **Core engineering problem:** Passenger info service with arrival prediction and disruption alerts.
- **Architecture:** Modular monolith; AVL pipeline; prediction engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (AVL events)
- **Security architecture:** RBAC, public APIs, audit
- **Key advanced concepts:** Arrival prediction, disruptions, alerts
- **Why it is industrial:** Transit-grade information with arrival prediction

## JAVA-564 — Fare Collection & Capping Ledger

- **Difficulty:** Omega (Tier 5)
- **Industry:** Public Transit / Fares
- **Business problem:** Fare collection must cap daily/weekly spend and settle across operators.
- **Core engineering problem:** Fare engine with capping logic, transfers and multi-operator settlement.
- **Architecture:** Modular monolith; fare service; capping engine; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (tap events)
- **Security architecture:** RBAC, ledger integrity, audit
- **Key advanced concepts:** Capping, transfers, settlement
- **Why it is industrial:** Fare-grade collection with capping integrity

## JAVA-565 — Railway Signalling Simulation Workbench

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Signalling
- **Business problem:** Railway signalling must be simulated with route locking and safety logic.
- **Core engineering problem:** Signalling simulation with interlocking logic, routes and safety checks.
- **Architecture:** Modular monolith; interlocking engine; route service; safety checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (signal events)
- **Security architecture:** RBAC, safety-critical audit, four-eyes
- **Key advanced concepts:** Interlocking logic, routes, safety
- **Why it is industrial:** Signalling-grade simulation with interlocking

## JAVA-566 — Rail Timetable Conflict Detection

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Planning
- **Business problem:** Timetables must be checked for conflicts across infrastructure and rolling stock.
- **Core engineering problem:** Timetable conflict detector with resource graphs and conflict resolution.
- **Architecture:** Modular monolith; timetable engine; conflict detector; resolution workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (timetable events)
- **Security architecture:** RBAC, planner approvals, audit
- **Key advanced concepts:** Conflict detection, resolution, resources
- **Why it is industrial:** Rail-grade timetable validation with conflicts

## JAVA-567 — Rail Asset Condition & Track Geometry

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Assets
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Track geometry must be monitored with condition analytics and maintenance planning.
- **Core engineering problem:** Track condition monitor with geometry data, thresholds and maintenance plans.
- **Architecture:** Modular monolith; geometry pipeline; condition engine; planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (geometry data)
- **Security architecture:** RBAC, network scoping, audit
- **Key advanced concepts:** Geometry thresholds, condition, planning
- **Why it is industrial:** Rail-grade condition monitoring with planning

## JAVA-568 — Rail Crew Duty & Fatigue Rules Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Crew
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Train crew duty must respect fatigue rules, routes and certifications.
- **Core engineering problem:** Crew duty engine with fatigue rules, route knowledge and rostering.
- **Architecture:** Modular monolith; duty engine; fatigue rules; roster service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (crew events)
- **Security architecture:** RBAC, crew PII, audit
- **Key advanced concepts:** Fatigue rules, route knowledge, rostering
- **Why it is industrial:** Rail-grade crew management with fatigue compliance

## JAVA-569 — Rolling Stock Maintenance Planner

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Maintenance
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Rolling stock maintenance must be planned from mileage, condition and parts.
- **Core engineering problem:** Maintenance planner with mileage triggers, condition data and parts logistics.
- **Architecture:** Modular monolith; maintenance scheduler; condition feed; parts service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (fleet events)
- **Security architecture:** RBAC, depot scoping, audit
- **Key advanced concepts:** Mileage triggers, condition, parts
- **Why it is industrial:** Rolling-stock-grade planning with parts logistics

## JAVA-570 — Train Dispatching & Delay Attribution

- **Difficulty:** Omega (Tier 5)
- **Industry:** Rail / Operations
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Train dispatching must handle conflicts and attribute delays to causes.
- **Core engineering problem:** Dispatch console with conflict resolution, delay attribution and analytics.
- **Architecture:** Modular monolith; dispatch engine; attribution service; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (train events)
- **Security architecture:** RBAC, dispatcher scoping, audit
- **Key advanced concepts:** Conflict resolution, delay attribution
- **Why it is industrial:** Rail-grade dispatching with delay attribution

## JAVA-571 — Airline Revenue Management & Pricing

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Revenue
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Airline pricing must optimize revenue with demand forecasting and fare classes.
- **Core engineering problem:** Revenue management with demand models, fare classes and overbooking.
- **Architecture:** Modular monolith; pricing engine; demand models; overbooking service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (booking events)
- **Security architecture:** RBAC, pricing confidentiality, audit
- **Key advanced concepts:** Demand models, fare classes, overbooking
- **Why it is industrial:** Revenue-grade management with demand modeling

## JAVA-572 — Airline Schedule & Slot Coordination

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Scheduling
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Airline schedules must respect slots, rotations and maintenance windows.
- **Core engineering problem:** Schedule engine with slot constraints, rotations and maintenance windows.
- **Architecture:** Modular monolith; schedule engine; rotation service; maintenance windows
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (schedule events)
- **Security architecture:** RBAC, planner approvals, audit
- **Key advanced concepts:** Slot constraints, rotations, windows
- **Why it is industrial:** Aviation-grade scheduling with slot compliance

## JAVA-573 — Aircraft Turnaround Orchestration

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Ground Ops
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Aircraft turnaround must be orchestrated with service timing and delays.
- **Core engineering problem:** Turnaround orchestrator with service tasks, timing and delay alerts.
- **Architecture:** Modular monolith; turnaround workflow; task service; delay engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (turnaround events)
- **Security architecture:** RBAC, ramp safety, audit
- **Key advanced concepts:** Service tasks, timing, delays
- **Why it is industrial:** Aviation-grade turnaround with timing optimization

## JAVA-574 — Aircraft Maintenance Logbook & Deferrals

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / MRO
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Aircraft maintenance logbooks must track defects, deferrals and releases.
- **Core engineering problem:** Tech log with defect tracking, MEL/CDL deferrals and release workflows.
- **Architecture:** Modular monolith; logbook service; deferral engine; release workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (logbook events)
- **Security architecture:** RBAC, e-signatures, audit
- **Key advanced concepts:** Defects, deferrals, releases
- **Why it is industrial:** MRO-grade logbooks with deferral governance

## JAVA-575 — MEL/CDL Configuration & Release

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / MEL
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** MEL/CDL configurations must be managed per aircraft with dispatch constraints.
- **Core engineering problem:** MEL/CDL configuration service with dispatch rules and validity windows.
- **Architecture:** Modular monolith; configuration store; dispatch rules; validity service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (config events)
- **Security architecture:** RBAC, dispatch safety, audit
- **Key advanced concepts:** Dispatch rules, validity windows, configurations
- **Why it is industrial:** Aviation-grade MEL management with dispatch safety

## JAVA-576 — Crew Pairing & Rostering Optimizer

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Crew
- **Business problem:** Crew pairings must be optimized with legality, fatigue and cost constraints.
- **Core engineering problem:** Crew pairing optimizer with legality rules, rest requirements and costs.
- **Architecture:** Modular monolith; pairing solver; legality engine; cost model
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (crew events)
- **Security architecture:** RBAC, crew PII, audit
- **Key advanced concepts:** Pairing optimization, legality, fatigue
- **Why it is industrial:** Aviation-grade pairing with legality constraints

## JAVA-577 — Airport Slot & Gate Allocation

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Airport
- **Business problem:** Airport slots and gates must be allocated with constraints and preferences.
- **Core engineering problem:** Slot/gate allocation engine with constraints, preferences and changes.
- **Architecture:** Modular monolith; allocation engine; constraint service; change workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (allocation events)
- **Security architecture:** RBAC, airline scoping, audit
- **Key advanced concepts:** Slot allocation, constraints, preferences
- **Why it is industrial:** Airport-grade allocation with constraint handling

## JAVA-578 — Baggage Reconciliation & Tracking

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Baggage
- **Business problem:** Baggage must be tracked and reconciled with passenger flights.
- **Core engineering problem:** Baggage reconciliation with scan events, exception handling and tracing.
- **Architecture:** Modular monolith; scan pipeline; reconciliation engine; exception workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (scan events)
- **Security architecture:** RBAC, baggage data, audit
- **Key advanced concepts:** Scan events, reconciliation, exceptions
- **Why it is industrial:** Aviation-grade baggage with reconciliation integrity

## JAVA-579 — Flight Data Monitoring & Exceedance Detector

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / Safety
- **Business problem:** Flight data must be monitored for exceedances with safety analytics.
- **Core engineering problem:** Flight data monitoring with exceedance detection, events and analytics.
- **Architecture:** Modular monolith; FDM pipeline; exceedance engine; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (flight data)
- **Security architecture:** RBAC, flight-data confidentiality, audit
- **Key advanced concepts:** Exceedance detection, events, analytics
- **Why it is industrial:** Safety-grade monitoring with exceedance detection

## JAVA-580 — Air Traffic Flow Simulation (tower-lab)

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / ATM
- **Business problem:** Air traffic flow must be simulated with sector capacity and conflict detection.
- **Core engineering problem:** ATM flow simulator with sector models, conflicts and capacity limits.
- **Architecture:** Modular monolith; simulator engine; sector models; conflict detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (traffic events)
- **Security architecture:** RBAC, scenario scoping, audit
- **Key advanced concepts:** Sector capacity, conflicts, simulation
- **Why it is industrial:** ATM-grade simulation with sector capacity

## JAVA-581 — Maritime Vessel Tracking & AIS Processing

- **Difficulty:** Omega (Tier 5)
- **Industry:** Maritime / Tracking
- **Business problem:** Vessel tracking must process AIS streams with geofencing and port events.
- **Core engineering problem:** AIS processing with position streaming, geofences and port-call detection.
- **Architecture:** Modular monolith; AIS pipeline; geofence engine; port-call detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (AIS streams)
- **Security architecture:** RBAC, data scoping, audit
- **Key advanced concepts:** Position streaming, geofences, port calls
- **Why it is industrial:** Maritime-grade tracking with port-call detection

## JAVA-582 — Port Container Terminal Operations

- **Difficulty:** Omega (Tier 5)
- **Industry:** Maritime / Ports
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Container terminal operations must be planned with cranes, yard and trucks.
- **Core engineering problem:** Terminal operations with crane scheduling, yard planning and truck windows.
- **Architecture:** Modular monolith; crane scheduler; yard planner; truck windows
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (terminal events)
- **Security architecture:** RBAC, terminal scoping, audit
- **Key advanced concepts:** Crane scheduling, yard planning, windows
- **Why it is industrial:** Terminal-grade operations with crane optimization

## JAVA-583 — Ship Stowage & Stability Calculator

- **Difficulty:** Omega (Tier 5)
- **Industry:** Maritime / Stowage
- **Business problem:** Ship stowage must be planned with stability, hazardous segregation and port rotations.
- **Core engineering problem:** Stowage planner with stability computation, DG rules and port sequences.
- **Architecture:** Modular monolith; stowage engine; stability calculator; DG rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (stowage events)
- **Security architecture:** RBAC, four-eyes plans, audit
- **Key advanced concepts:** Stability math, DG segregation, rotations
- **Why it is industrial:** Stowage-grade planning with stability safety

## JAVA-584 — Maritime Compliance & Port State Ledger

- **Difficulty:** Omega (Tier 5)
- **Industry:** Maritime / Compliance
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Port state inspections and certificates must be tracked with expiry management.
- **Core engineering problem:** Maritime compliance with certificate tracking, inspections and expiries.
- **Architecture:** Modular monolith; certificate registry; inspection workflow; expiry alerts
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (compliance events)
- **Security architecture:** RBAC, certificate integrity, audit
- **Key advanced concepts:** Certificates, inspections, expiries
- **Why it is industrial:** Maritime-grade compliance with certificate integrity

## JAVA-585 — Autonomous Underwater Inspection Missions

- **Difficulty:** Omega (Tier 5)
- **Industry:** Maritime / Robotics
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Underwater inspection missions must be planned with autonomy and data capture.
- **Core engineering problem:** AUV mission planner with waypoints, obstacle avoidance and data logs.
- **Architecture:** Modular monolith; mission planner; waypoint engine; data store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext), MinIO
- **Messaging:** Kafka (mission events)
- **Security architecture:** RBAC, mission safety, audit
- **Key advanced concepts:** Waypoints, obstacles, data logs
- **Why it is industrial:** Maritime-grade mission planning with autonomy

## JAVA-586 — Traffic Signal Coordination & Green Waves

- **Difficulty:** Omega (Tier 5)
- **Industry:** Smart City / Traffic
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Traffic signals must coordinate for green waves with adaptive timing.
- **Core engineering problem:** Signal coordination with corridor logic, green waves and adaptive timing.
- **Architecture:** Modular monolith; signal engine; corridor service; adaptive timing
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (signal events)
- **Security architecture:** RBAC, control audit, safety interlocks
- **Key advanced concepts:** Green waves, adaptive timing, corridors
- **Why it is industrial:** Traffic-grade coordination with adaptive timing

## JAVA-587 — Road Congestion & Incident Detection

- **Difficulty:** Omega (Tier 5)
- **Industry:** Smart City / Traffic
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Road congestion and incidents must be detected from vehicle data.
- **Core engineering problem:** Congestion detector with speed analytics, incident detection and alerts.
- **Architecture:** Modular monolith; speed pipeline; incident engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (traffic data)
- **Security architecture:** RBAC, city scoping, audit
- **Key advanced concepts:** Speed analytics, incidents, alerts
- **Why it is industrial:** Traffic-grade detection with incident alerts

## JAVA-588 — Electronic Toll Collection & Enforcement

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Tolling
- **Business problem:** Electronic toll collection must handle violations, enforcement and settlement.
- **Core engineering problem:** Toll engine with transaction processing, violation workflow and settlement.
- **Architecture:** Modular monolith; transaction pipeline; violation engine; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (toll events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Transactions, violations, settlement
- **Why it is industrial:** Tolling-grade collection with enforcement

## JAVA-589 — Smart Parking Guidance Network

- **Difficulty:** Omega (Tier 5)
- **Industry:** Smart City / Parking
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Smart parking must guide drivers with occupancy and pricing.
- **Core engineering problem:** Parking guidance with occupancy sensors, pricing rules and guidance APIs.
- **Architecture:** Modular monolith; occupancy pipeline; pricing engine; guidance API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** MQTT broker
- **Security architecture:** RBAC, public APIs, audit
- **Key advanced concepts:** Occupancy, pricing, guidance
- **Why it is industrial:** Parking-grade guidance with occupancy intelligence

## JAVA-590 — Bicycle & Micro-Mobility Fleet Ops

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Micro-Mobility
- **Business problem:** Bike/scooter fleets must manage charging, parking zones and maintenance.
- **Core engineering problem:** Micro-mobility ops with zone enforcement, charging logistics and maintenance.
- **Architecture:** Modular monolith; fleet service; zone engine; maintenance planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (fleet events)
- **Security architecture:** RBAC, zone rules, audit
- **Key advanced concepts:** Zone enforcement, charging, maintenance
- **Why it is industrial:** Micro-mobility-grade operations with zone rules

## JAVA-591 — Hyperloop/Pod Scheduling Simulator

- **Difficulty:** Omega (Tier 5)
- **Industry:** Mobility / Futuristic
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Pod-based transit must be scheduled with demand-responsive routing.
- **Core engineering problem:** Pod scheduling simulator with demand routing, platooning and safety margins.
- **Architecture:** Modular monolith; scheduler; routing engine; safety model
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (pod events)
- **Security architecture:** RBAC, safety interlocks, audit
- **Key advanced concepts:** Demand routing, platooning, safety
- **Why it is industrial:** Futuristic-grade scheduling with safety models

## JAVA-592 — Spacecraft Telemetry Ground Station

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aerospace / Ground Stations
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Spacecraft telemetry must be received, decoded and archived at ground stations.
- **Core engineering problem:** Ground station with telemetry decoding, archival and pass scheduling.
- **Architecture:** Modular monolith; telemetry decoder; archive service; pass scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (telemetry)
- **Security architecture:** RBAC, mission confidentiality, audit
- **Key advanced concepts:** Telemetry decoding, passes, archival
- **Why it is industrial:** Aerospace-grade telemetry with pass scheduling

## JAVA-593 — Space Mission Planning & Constraint Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aerospace / Mission Planning
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Space missions must be planned with resource, window and constraint reasoning.
- **Core engineering problem:** Mission planner with constraint solving, resource windows and schedules.
- **Architecture:** Modular monolith; constraint solver; resource model; schedule service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (mission events)
- **Security architecture:** RBAC, mission confidentiality, audit
- **Key advanced concepts:** Constraint solving, windows, resources
- **Why it is industrial:** Aerospace-grade planning with constraint reasoning

## JAVA-594 — Aircraft Spares Pooling & AOG Support

- **Difficulty:** Omega (Tier 5)
- **Industry:** Aviation / AOG
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Aircraft-on-ground support needs spares pooling with urgent logistics.
- **Core engineering problem:** AOG spares service with pooling, urgent routing and loan tracking.
- **Architecture:** Modular monolith; pool registry; urgent routing; loan ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (AOG events)
- **Security architecture:** RBAC, loan integrity, audit
- **Key advanced concepts:** Spares pooling, urgent routing, loans
- **Why it is industrial:** AOG-grade support with pooling and loans

## JAVA-595 — Road Weather & Maintenance Decision Support

- **Difficulty:** Omega (Tier 5)
- **Industry:** Roads / Maintenance
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Road weather must inform maintenance decisions with forecasting and alerts.
- **Core engineering problem:** Weather decision support with road conditions, forecasts and crew alerts.
- **Architecture:** Modular monolith; weather ingestion; condition engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (weather data)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** Road conditions, forecasts, alerts
- **Why it is industrial:** Road-grade decision support with weather intelligence
