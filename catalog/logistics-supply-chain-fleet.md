# Logistics / Supply Chain / Fleet — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-446 — Transportation Management System

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / TMS
- **Business problem:** Transportation must plan, execute and settle shipments across modes with carrier contracts.
- **Core engineering problem:** TMS with shipment lifecycle, carrier selection and cost settlement.
- **Architecture:** Modular monolith; shipment workflow; carrier engine; settlement service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (shipment events)
- **Security architecture:** RBAC, carrier scoping, four-eyes settlement
- **Key advanced concepts:** Shipment lifecycle, carrier selection, settlement
- **Why it is industrial:** TMS-grade execution with settlement integrity

## JAVA-447 — Real-Time Shipment Visibility Platform

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Visibility
- **Business problem:** Shippers need real-time shipment visibility with milestones and exception alerts.
- **Core engineering problem:** Visibility platform with milestone ingestion, ETAs and exception workflows.
- **Architecture:** Modular monolith; milestone pipeline; ETA engine; exception service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (tracking events)
- **Security architecture:** RBAC, shipment data scoping, audit
- **Key advanced concepts:** Milestones, ETA prediction, exceptions
- **Why it is industrial:** Visibility-grade tracking with exception handling

## JAVA-448 — Carrier Rate Shopping & Tender Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Procurement
- **Business problem:** Carrier rates must be shopped across contracts with service-level tradeoffs.
- **Core engineering problem:** Rate shopping engine with contract rates, service maps and cost optimization.
- **Architecture:** Modular monolith; rate store; shopping engine; optimizer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (rate events)
- **Security architecture:** RBAC, contract confidentiality, audit
- **Key advanced concepts:** Rate shopping, service maps, optimization
- **Why it is industrial:** Procurement-grade rate shopping with optimization

## JAVA-449 — Multi-Carrier Parcel Label Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Parcel
- **Business problem:** Parcel labels must be generated per carrier with validation and tracking registration.
- **Core engineering problem:** Multi-carrier label service with label generation, validation and registration.
- **Architecture:** Modular monolith; label engine; carrier adapters; validation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (label events)
- **Security architecture:** RBAC, API keys per carrier, audit
- **Key advanced concepts:** Label generation, carrier adapters, validation
- **Why it is industrial:** Parcel-grade labeling with carrier adapters

## JAVA-450 — Freight Audit, Payment & GL Coding

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Finance
- **Business problem:** Freight invoices must be audited against contracts and GL-coded automatically.
- **Core engineering problem:** Freight audit with rate verification, dispute handling and GL coding.
- **Architecture:** Modular monolith; audit pipeline; rate engine; GL coding service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (invoice events)
- **Security architecture:** RBAC, four-eyes payments, audit
- **Key advanced concepts:** Rate verification, disputes, GL coding
- **Why it is industrial:** Finance-grade freight audit with GL integration

## JAVA-451 — Load Planning & 3D Palletization Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Planning
- **Business problem:** Loads must be planned with 3D palletization, weight limits and route constraints.
- **Core engineering problem:** Load planner with 3D packing heuristics, weight checks and route constraints.
- **Architecture:** Modular monolith; packing engine; weight checker; route constraints
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (load events)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** 3D packing, weight limits, constraints
- **Why it is industrial:** Planning-grade palletization with safety constraints

## JAVA-452 — Route Optimization & Daily Dispatch Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Routing
- **Business problem:** Daily routes must be optimized for distance, windows and driver fairness.
- **Core engineering problem:** Route optimizer with time windows, capacity and fairness constraints.
- **Architecture:** Modular monolith; routing solver; window service; fairness engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (route events)
- **Security architecture:** RBAC, dispatch scoping, audit
- **Key advanced concepts:** VRP solving, time windows, fairness
- **Why it is industrial:** Routing-grade optimization with driver fairness

## JAVA-453 — Driver Hours-of-Service & ELD Rules

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Compliance
- **Business problem:** Driver hours-of-service rules must be enforced with ELD-style logs.
- **Core engineering problem:** HOS engine with duty-status rules, violation detection and coaching.
- **Architecture:** Modular monolith; HOS engine; ELD ingestion; violation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (ELD events)
- **Security architecture:** RBAC, driver privacy, audit
- **Key advanced concepts:** HOS rules, violations, coaching
- **Why it is industrial:** Compliance-grade HOS with violation coaching

## JAVA-454 — Proof of Delivery & Exception Workflow

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Last Mile
- **Business problem:** Proof of delivery must capture evidence with exceptions and customer verification.
- **Core engineering problem:** POD workflow with photo evidence, exceptions and customer signatures.
- **Architecture:** Modular monolith; POD service; evidence store; exception workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (POD events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Photo evidence, exceptions, signatures
- **Why it is industrial:** Last-mile-grade POD with evidence integrity

## JAVA-455 — Cold Chain Monitoring & Excursion Alerts

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Cold Chain
- **Business problem:** Temperature excursions must be monitored with alerting and disposition decisions.
- **Core engineering problem:** Cold-chain monitor with excursion detection, alerting and disposition.
- **Architecture:** Modular monolith; telemetry ingestion; excursion engine; disposition
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (temp streams)
- **Security architecture:** RBAC, sensor integrity, audit
- **Key advanced concepts:** Excursion detection, alerting, disposition
- **Why it is industrial:** Cold-chain-grade monitoring with disposition rules

## JAVA-456 — Yard Management & Gate Automation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Yard
- **Business problem:** Yard operations must manage gates, docks and trailer dwell with automation.
- **Core engineering problem:** Yard management with gate automation, dock scheduling and dwell analytics.
- **Architecture:** Modular monolith; gate service; dock scheduler; dwell analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (yard events)
- **Security architecture:** RBAC, gate auth, audit
- **Key advanced concepts:** Gate automation, dock scheduling, dwell
- **Why it is industrial:** Yard-grade operations with dwell analytics

## JAVA-457 — Cross-Dock Scheduling & Sort Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Cross-Dock
- **Business problem:** Cross-dock flows must sort shipments with dock-door assignments and wave planning.
- **Core engineering problem:** Cross-dock scheduler with wave planning, door assignment and sort logic.
- **Architecture:** Modular monolith; wave planner; door assigner; sort engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (sort events)
- **Security architecture:** RBAC, dock scoping, audit
- **Key advanced concepts:** Wave planning, door assignment, sorting
- **Why it is industrial:** Cross-dock-grade scheduling with wave optimization

## JAVA-458 — Last-Mile Delivery Slotting Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Last Mile
- **Business problem:** Delivery slots must be offered with capacity, cost and courier constraints.
- **Core engineering problem:** Slotting engine with capacity windows, courier constraints and pricing.
- **Architecture:** Modular monolith; slot engine; capacity service; pricing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (slot events)
- **Security architecture:** RBAC, customer scoping, audit
- **Key advanced concepts:** Slot capacity, constraints, pricing
- **Why it is industrial:** Last-mile-grade slotting with capacity control

## JAVA-459 — Delivery Driver Marketplace & Payouts

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Gig Economy
- **Business problem:** Delivery drivers need a marketplace with offers, earnings and payouts.
- **Core engineering problem:** Driver marketplace with offer matching, earnings ledger and payout batches.
- **Architecture:** Modular monolith; matching engine; earnings ledger; payout service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (offer events)
- **Security architecture:** OIDC, device verification, ledger immutability
- **Key advanced concepts:** Offer matching, earnings, payouts
- **Why it is industrial:** Marketplace-grade earnings with payout integrity

## JAVA-460 — Drone Delivery Mission Planner (simulated)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Drones
- **Business problem:** Drone delivery missions must be planned with airspace, weather and battery constraints.
- **Core engineering problem:** Drone mission planner with geofencing, weather checks and battery models.
- **Architecture:** Modular monolith; mission planner; geofence engine; weather adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (mission events)
- **Security architecture:** RBAC, airspace rules, audit
- **Key advanced concepts:** Geofencing, weather, battery models
- **Why it is industrial:** Drone-grade mission planning with airspace rules

## JAVA-461 — Warehouse Execution System

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / WMS
- **Business problem:** Warehouse execution must direct putaway, picking and replenishment in real time.
- **Core engineering problem:** WES with task interleaving, wave release and real-time directives.
- **Architecture:** Modular monolith; task engine; wave service; directives
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (WES events)
- **Security architecture:** RBAC, zone scoping, audit
- **Key advanced concepts:** Task interleaving, waves, directives
- **Why it is industrial:** WES-grade execution with task optimization

## JAVA-462 — Voice & Light-Directed Picking

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Picking
- **Business problem:** Voice and light-directed picking must guide operators with confirmations.
- **Core engineering problem:** Picking directives with voice/light simulation, confirmations and accuracy.
- **Architecture:** Modular monolith; directive engine; simulator; accuracy analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (pick events)
- **Security architecture:** RBAC, operator identity, audit
- **Key advanced concepts:** Directives, confirmations, accuracy
- **Why it is industrial:** Picking-grade direction with accuracy tracking

## JAVA-463 — Returns Grading & Disposition Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Reverse
- **Business problem:** Returns must be graded and disposed optimally (restock, refurbish, recycle).
- **Core engineering problem:** Returns grading with disposition rules, economics and routing.
- **Architecture:** Modular monolith; grading engine; disposition rules; routing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (return events)
- **Security architecture:** RBAC, refund controls, audit
- **Key advanced concepts:** Grading, disposition economics, routing
- **Why it is industrial:** Reverse-logistics-grade grading with economics

## JAVA-464 — Customs Brokerage & HS Classification

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Customs
- **Business problem:** Shipments must be classified with HS codes and customs docs.
- **Core engineering problem:** Customs brokerage with HS classification, doc generation and clearance.
- **Architecture:** Modular monolith; classification engine; doc generator; clearance workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (customs events)
- **Security architecture:** RBAC, doc integrity, audit
- **Key advanced concepts:** HS classification, docs, clearance
- **Why it is industrial:** Customs-grade brokerage with classification

## JAVA-465 — Duty & Tariff Calculation Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Trade Compliance
- **Business problem:** Duties and tariffs must be computed per origin, commodity and agreements.
- **Core engineering problem:** Duty engine with trade agreements, tariff lookups and calculations.
- **Architecture:** Modular monolith; duty engine; tariff store; agreement rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (duty events)
- **Security architecture:** RBAC, four-eyes calculations, audit
- **Key advanced concepts:** Tariffs, agreements, calculations
- **Why it is industrial:** Trade-grade duty computation with agreement logic

## JAVA-466 — Trade Compliance Screening (export controls)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Export Controls
- **Business problem:** Export shipments must be screened against control lists with license checks.
- **Core engineering problem:** Export-control screening with list matching, license validation and holds.
- **Architecture:** Modular monolith; screening service; license store; hold workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (screening events)
- **Security architecture:** RBAC, hold governance, audit
- **Key advanced concepts:** List matching, licenses, holds
- **Why it is industrial:** Trade-grade screening with license enforcement

## JAVA-467 — Document Set Generator for Shipments

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Documentation
- **Business problem:** Shipment document sets must be generated, validated and versioned.
- **Core engineering problem:** Document set generator with templates, data merge and validation.
- **Architecture:** Modular monolith; template engine; merge service; validator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (doc events)
- **Security architecture:** RBAC, doc integrity, audit
- **Key advanced concepts:** Templates, merge, validation
- **Why it is industrial:** Documentation-grade generation with validation

## JAVA-468 — Incoterms & Cost Allocation Calculator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Trade Terms
- **Business problem:** Incoterms must be applied to allocate costs, risks and responsibilities.
- **Core engineering problem:** Incoterms engine with cost allocation and responsibility matrices.
- **Architecture:** Modular monolith; terms engine; cost allocator; responsibility matrix
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (terms events)
- **Security architecture:** RBAC, contract scoping, audit
- **Key advanced concepts:** Incoterms rules, cost allocation, matrices
- **Why it is industrial:** Trade-grade terms management with allocation

## JAVA-469 — Ocean Carrier Booking & Allocation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Ocean
- **Business problem:** Ocean bookings must manage allocations, containers and documentation.
- **Core engineering problem:** Ocean booking with allocation management, container tracking and docs.
- **Architecture:** Modular monolith; booking engine; allocation service; doc workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (booking events)
- **Security architecture:** RBAC, carrier scoping, audit
- **Key advanced concepts:** Allocations, container tracking, docs
- **Why it is industrial:** Ocean-grade booking with allocation control

## JAVA-470 — Container Tracking & Demurrage Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Ocean
- **Business problem:** Container tracking must manage demurrage, detention and free-time deadlines.
- **Core engineering problem:** Container tracker with free-time computation, demurrage ledger and alerts.
- **Architecture:** Modular monolith; container registry; free-time engine; charge ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (container events)
- **Security architecture:** RBAC, charge dispute workflow, audit
- **Key advanced concepts:** Free-time math, demurrage, disputes
- **Why it is industrial:** Ocean-grade container management with demurrage

## JAVA-471 — Port Community & Vessel Turnaround

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Ports
- **Business problem:** Port community systems must coordinate vessel calls, berths and services.
- **Core engineering problem:** Port community platform with vessel calls, berth windows and service coordination.
- **Architecture:** Modular monolith; call registry; berth planner; service board
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (port events)
- **Security architecture:** RBAC, stakeholder scoping, audit
- **Key advanced concepts:** Vessel calls, berths, coordination
- **Why it is industrial:** Port-grade coordination with stakeholder workflows

## JAVA-472 — Hinterland Rail Shuttle Planning

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Rail
- **Business problem:** Hinterland rail shuttles must be planned with slot allocation and tracking.
- **Core engineering problem:** Rail shuttle planning with slot allocation, capacity and tracking.
- **Architecture:** Modular monolith; shuttle planner; slot service; tracking feed
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (rail events)
- **Security architecture:** RBAC, operator scoping, audit
- **Key advanced concepts:** Slot allocation, capacity, tracking
- **Why it is industrial:** Rail-grade shuttle planning with capacity

## JAVA-473 — Freight Forwarding Shipment Orchestrator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Freight Forwarding
- **Business problem:** Forwarders must orchestrate multi-leg shipments with handoffs and documentation.
- **Core engineering problem:** Freight forwarding orchestrator with leg management, handoffs and docs.
- **Architecture:** Modular monolith; shipment orchestrator; leg service; doc workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (leg events)
- **Security architecture:** RBAC, party scoping, audit
- **Key advanced concepts:** Leg management, handoffs, docs
- **Why it is industrial:** Forwarding-grade orchestration with handoffs

## JAVA-474 — NVOCC Consolidation & Deconsolidation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / NVOCC
- **Business problem:** Consolidation must group cargo, manage containers and deconsolidate efficiently.
- **Core engineering problem:** NVOCC consolidation with cargo grouping, container plans and deconsolidation.
- **Architecture:** Modular monolith; consolidation engine; container plan; deconsolidation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (consolidation events)
- **Security architecture:** RBAC, consignee scoping, audit
- **Key advanced concepts:** Grouping, container plans, deconsolidation
- **Why it is industrial:** NVOCC-grade consolidation with efficiency

## JAVA-475 — Air Cargo ULD Build-Up Planning

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Air Cargo
- **Business problem:** ULD build-up must be planned with weight, balance and priority constraints.
- **Core engineering problem:** ULD planner with build-up optimization, weight limits and priorities.
- **Architecture:** Modular monolith; ULD planner; weight engine; priority service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (ULD events)
- **Security architecture:** RBAC, security screening flags, audit
- **Key advanced concepts:** Build-up optimization, weights, priorities
- **Why it is industrial:** Air-cargo-grade ULD planning with safety

## JAVA-476 — Ground Handling Slot & Resource Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Ground Handling
- **Business problem:** Ground handling must allocate slots and resources for flights and cargo.
- **Core engineering problem:** Ground handling engine with slot allocation, resource scheduling and SLAs.
- **Architecture:** Modular monolith; slot service; resource scheduler; SLA timers
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (GH events)
- **Security architecture:** RBAC, ramp safety rules, audit
- **Key advanced concepts:** Slots, resources, SLAs
- **Why it is industrial:** Ground-handling-grade allocation with SLAs

## JAVA-477 — Dangerous Goods Validation & Placarding

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Hazmat
- **Business problem:** Dangerous goods must be validated against regulations with placarding rules.
- **Core engineering problem:** DG validation with regulation tables, segregation rules and placards.
- **Architecture:** Modular monolith; DG validator; regulation store; segregation engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (DG events)
- **Security architecture:** RBAC, four-eyes DG approval, audit
- **Key advanced concepts:** DG tables, segregation, placards
- **Why it is industrial:** Hazmat-grade validation with segregation

## JAVA-478 — Parcel Sortation Machine Simulation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Sortation
- **Business problem:** Parcel sortation must be simulated with chute logic and throughput tuning.
- **Core engineering problem:** Sortation simulator with chute assignment, throughput and failure modes.
- **Architecture:** Modular monolith; simulator; chute engine; failure injection
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (sort events)
- **Security architecture:** RBAC, facility scoping, audit
- **Key advanced concepts:** Chute logic, throughput, failures
- **Why it is industrial:** Sortation-grade simulation with failure modes

## JAVA-479 — Locker Network & PIN Distribution

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Lockers
- **Business problem:** Locker networks must manage PIN distribution, dwell and pickup workflows.
- **Core engineering problem:** Locker network with PIN generation, dwell rules and pickup verification.
- **Architecture:** Modular monolith; locker registry; PIN service; pickup workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (locker events)
- **Security architecture:** RBAC, PIN security, audit
- **Key advanced concepts:** PIN generation, dwell rules, verification
- **Why it is industrial:** Locker-grade operations with secure PINs

## JAVA-480 — Package Exception Video & Photo Review

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Exceptions
- **Business problem:** Package exceptions need photo/video review with resolution workflows.
- **Core engineering problem:** Exception review with media evidence, claims and resolution tracking.
- **Architecture:** Modular monolith; exception workflow; media store; resolution service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (exception events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Media review, claims, resolutions
- **Why it is industrial:** Exception-grade handling with media evidence

## JAVA-481 — Shipment Cost Allocation & P&L Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Finance
- **Business problem:** Shipment costs must be allocated to P&L with activity-based costing.
- **Core engineering problem:** Cost allocation engine with ABC logic, profitability views and accruals.
- **Architecture:** Modular monolith; cost engine; allocation rules; P&L service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (cost events)
- **Security architecture:** RBAC, cost confidentiality, audit
- **Key advanced concepts:** ABC allocation, profitability, accruals
- **Why it is industrial:** Finance-grade allocation with profitability views

## JAVA-482 — Delivery Density Heatmap & Territory Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Planning
- **Business problem:** Delivery density must be analyzed to shape territories and routes.
- **Core engineering problem:** Density heatmap engine with territory shaping and route hints.
- **Architecture:** Modular monolith; density pipeline; heatmap service; territory engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (order events)
- **Security architecture:** RBAC, territory scoping, audit
- **Key advanced concepts:** Heatmaps, territories, route hints
- **Why it is industrial:** Planning-grade density analysis with territory shaping

## JAVA-483 — Dynamic ETA Prediction Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Predictive
- **Business problem:** ETAs must be predicted dynamically from traffic, weather and historical patterns.
- **Core engineering problem:** ETA prediction service with feature pipelines and model scoring.
- **Architecture:** Modular monolith; feature pipeline; model service; ETA API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (tracking streams)
- **Security architecture:** RBAC, model governance, audit
- **Key advanced concepts:** Feature pipelines, model scoring, feedback
- **Why it is industrial:** Predictive-grade ETA with model governance

## JAVA-484 — Fleet Telematics Ingestion Hub

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Telematics
- **Business problem:** Fleet telematics must ingest vehicle data at scale with alerting.
- **Core engineering problem:** Telematics hub with device ingestion, geofencing and driver alerts.
- **Architecture:** Modular monolith; ingestion pipeline; geofence engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (telemetry streams)
- **Security architecture:** RBAC, device identity, audit
- **Key advanced concepts:** Device ingestion, geofencing, alerts
- **Why it is industrial:** Telematics-grade ingestion with alerting

## JAVA-485 — Predictive Maintenance for Truck Fleets

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Maintenance
- **Business problem:** Truck fleets need predictive maintenance from telemetry and service history.
- **Core engineering problem:** Fleet predictive maintenance with anomaly detection and service planning.
- **Architecture:** Modular monolith; anomaly engine; service planner; parts service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (telemetry)
- **Security architecture:** RBAC, workshop scoping, audit
- **Key advanced concepts:** Anomaly detection, service planning
- **Why it is industrial:** Fleet-grade predictive maintenance with planning

## JAVA-486 — Fleet Compliance & Violation Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Compliance
- **Business problem:** Fleet violations must be tracked with fines, coaching and license management.
- **Core engineering problem:** Violation ledger with fine tracking, coaching and driver license checks.
- **Architecture:** Modular monolith; violation service; fine ledger; coaching workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (violation events)
- **Security architecture:** RBAC, driver privacy, audit
- **Key advanced concepts:** Violations, fines, coaching
- **Why it is industrial:** Compliance-grade violation management with coaching

## JAVA-487 — Asset Tracking with Geofence Events

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Asset Tracking
- **Business problem:** Assets must be tracked with geofence events and custody transfers.
- **Core engineering problem:** Asset tracker with geofencing, custody chains and utilization analytics.
- **Architecture:** Modular monolith; asset registry; geofence service; custody ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (asset events)
- **Security architecture:** RBAC, custody integrity, audit
- **Key advanced concepts:** Geofences, custody, utilization
- **Why it is industrial:** Asset-grade tracking with custody chains

## JAVA-488 — Reverse Logistics Network Design

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Network Design
- **Business problem:** Reverse logistics networks must be designed with cost and service tradeoffs.
- **Core engineering problem:** Network design with flow models, facility scenarios and cost analysis.
- **Architecture:** Modular monolith; flow model; scenario engine; cost analyzer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scenario events)
- **Security architecture:** RBAC, scenario scoping, audit
- **Key advanced concepts:** Flow models, scenarios, costs
- **Why it is industrial:** Design-grade network modeling with scenarios

## JAVA-489 — Fleet Electrification Charge Scheduling

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / EV Fleets
- **Business problem:** EV fleet charging must be scheduled against routes, batteries and tariffs.
- **Core engineering problem:** Charge scheduler with battery models, route plans and tariff windows.
- **Architecture:** Modular monolith; charge scheduler; battery models; tariff service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (charge events)
- **Security architecture:** RBAC, site scoping, audit
- **Key advanced concepts:** Battery models, tariffs, schedules
- **Why it is industrial:** EV-grade charging optimization with tariffs

## JAVA-490 — Intermodal Container Optimization

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Intermodal
- **Business problem:** Intermodal containers must be optimized across rail, road and port legs.
- **Core engineering problem:** Intermodal optimizer with mode selection, timing and cost tradeoffs.
- **Architecture:** Modular monolith; optimizer; mode engine; timing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (intermodal events)
- **Security architecture:** RBAC, corridor scoping, audit
- **Key advanced concepts:** Mode selection, timing, costs
- **Why it is industrial:** Intermodal-grade optimization with tradeoffs

## JAVA-491 — Pallet Pool & Returnable Asset Tracking

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Returnables
- **Business problem:** Pallet pools must track returnable assets, deposits and losses.
- **Core engineering problem:** Returnable tracking with pool accounting, deposits and loss analytics.
- **Architecture:** Modular monolith; pool registry; deposit ledger; loss analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (pool events)
- **Security architecture:** RBAC, partner scoping, audit
- **Key advanced concepts:** Pool accounting, deposits, losses
- **Why it is industrial:** Returnable-grade pooling with deposit integrity

## JAVA-492 — Warehouse Labor Standards & Incentives

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Labor
- **Business problem:** Warehouse labor standards must be engineered with incentives and fairness.
- **Core engineering problem:** Labor standards engine with engineered rates, incentives and fairness rules.
- **Architecture:** Modular monolith; standards service; incentive engine; fairness rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (labor events)
- **Security architecture:** RBAC, payroll integration, audit
- **Key advanced concepts:** Engineered standards, incentives, fairness
- **Why it is industrial:** Labor-grade standards with incentive integrity

## JAVA-493 — Loading Dock Appointment Booking

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Docks
- **Business problem:** Dock appointments must be booked with carrier, load and dwell constraints.
- **Core engineering problem:** Dock appointment system with slot booking, constraints and dwell optimization.
- **Architecture:** Modular monolith; appointment engine; constraint service; dwell analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (dock events)
- **Security architecture:** RBAC, carrier scoping, audit
- **Key advanced concepts:** Slot booking, constraints, dwell
- **Why it is industrial:** Dock-grade appointmenting with dwell optimization

## JAVA-494 — Multi-Echelon Inventory Deployment

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Network
- **Business problem:** Multi-echelon inventory must be deployed with service-level targets.
- **Core engineering problem:** Inventory deployment with echelon balancing, safety stocks and transfers.
- **Architecture:** Modular monolith; deployment engine; safety-stock service; transfer planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (inventory events)
- **Security architecture:** RBAC, DC scoping, audit
- **Key advanced concepts:** Echelon balancing, safety stocks, transfers
- **Why it is industrial:** Network-grade deployment with service levels

## JAVA-495 — Bulk Tanker Allocation & Scheduling

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Logistics / Bulk
- **Business problem:** Bulk tanker fleets must be allocated with product, wash and compatibility rules.
- **Core engineering problem:** Tanker allocation with product compatibility, wash cycles and routes.
- **Architecture:** Modular monolith; allocation engine; compatibility rules; wash planner
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (tanker events)
- **Security architecture:** RBAC, HSE rules, audit
- **Key advanced concepts:** Compatibility, wash cycles, allocation
- **Why it is industrial:** Bulk-grade allocation with compatibility rules
