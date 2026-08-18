# Telecom / Networking / Media — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-346 — Subscriber Provisioning & SIM Lifecycle

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / OSS
- **Business problem:** Subscriber provisioning must orchestrate SIM lifecycle, services and devices consistently.
- **Core engineering problem:** Provisioning workflow with SIM states, service activation and device binding.
- **Architecture:** Modular monolith; provisioning workflow; SIM registry; activation adapters
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (provisioning events)
- **Security architecture:** RBAC, SIM auth (Ki-style), audit
- **Key advanced concepts:** SIM lifecycle, activation, compensation
- **Why it is industrial:** OSS-grade provisioning with SIM lifecycle integrity

## JAVA-347 — 5G Network Slice Orchestrator (simulated)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / 5G
- **Business problem:** Network slices must be orchestrated with QoS profiles, quotas and lifecycle (simulated).
- **Core engineering problem:** Slice orchestration with templates, quotas, isolation policies and lifecycle.
- **Architecture:** Modular monolith; slice manager; QoS engine; quota service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (slice events)
- **Security architecture:** RBAC, tenant isolation, audit
- **Key advanced concepts:** Slice templates, QoS profiles, quotas
- **Why it is industrial:** 5G-grade slice orchestration with tenant isolation

## JAVA-348 — Cell Site Performance & KPI Analytics

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / RAN
- **Business problem:** Cell site KPIs must be monitored with degradation detection and ticket integration.
- **Core engineering problem:** KPI analytics with thresholds, trend detection and ticketing.
- **Architecture:** Modular monolith; KPI pipeline; anomaly detection; ticket adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (KPI streams)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** KPI thresholds, trends, ticketing
- **Why it is industrial:** RAN-grade KPI monitoring with degradation detection

## JAVA-349 — Fault & Alarm Correlation Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / NOC
- **Business problem:** Faults and alarms must be correlated to root causes and deduplicated across network layers.
- **Core engineering problem:** Alarm correlation engine with topology awareness and root-cause inference.
- **Architecture:** Modular monolith; correlation engine; topology store; root-cause service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext), Redis 7
- **Messaging:** Kafka (alarm streams)
- **Security architecture:** RBAC, NOC scoping, audit
- **Key advanced concepts:** Alarm correlation, root cause, dedup
- **Why it is industrial:** NOC-grade correlation with topology awareness

## JAVA-350 — Radio Access Capacity Planning

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Planning
- **Business problem:** Radio capacity must be planned from traffic forecasts with headroom policies.
- **Core engineering problem:** Capacity planning with traffic models, headroom rules and what-ifs.
- **Architecture:** Modular monolith; capacity models; forecast integration; what-if service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (traffic data)
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Traffic models, headroom, what-ifs
- **Why it is industrial:** Planning-grade capacity modeling with headroom rules

## JAVA-351 — Tower Lease & Colocation Billing

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Real Estate
- **Business problem:** Tower leases must be managed with site data, colocations and revenue billing.
- **Core engineering problem:** Tower lease administration with colocation billing and escalation tracking.
- **Architecture:** Modular monolith; site registry; lease engine; billing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (lease events)
- **Security architecture:** RBAC, four-eyes contracts, audit
- **Key advanced concepts:** Colocation billing, escalations, renewals
- **Why it is industrial:** Tower-grade lease administration with revenue integrity

## JAVA-352 — VoIP Call Session Controller (SIP-style)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Voice
- **Business problem:** VoIP calls need session control with routing, features and CDR generation (SIP-style).
- **Core engineering problem:** Call session controller with routing rules, feature codes and CDRs.
- **Architecture:** Modular monolith; call engine; routing rules; CDR service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Netty
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (CDR events)
- **Security architecture:** RBAC, fraud checks, CDR integrity
- **Key advanced concepts:** SIP-style signaling, routing, CDRs
- **Why it is industrial:** Voice-grade session control with CDR integrity

## JAVA-353 — SMS Gateway & Campaign Delivery Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Messaging
- **Business problem:** SMS campaigns must deliver at scale with routing, DLR tracking and throttling.
- **Core engineering problem:** SMS gateway with campaign engine, DLR processing and carrier routing.
- **Architecture:** Modular monolith; campaign engine; DLR pipeline; routing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (SMS events)
- **Security architecture:** RBAC, content policies, audit
- **Key advanced concepts:** DLR tracking, throttling, routing
- **Why it is industrial:** Messaging-grade delivery with DLR reconciliation

## JAVA-354 — Number Portability & Routing Database

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Numbering
- **Business problem:** Number portability must route calls correctly with sync between databases.
- **Core engineering problem:** Porting workflow with number database sync, routing updates and validation.
- **Architecture:** Modular monolith; port workflow; routing DB; validation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (port events)
- **Security architecture:** RBAC, four-eyes ports, audit
- **Key advanced concepts:** Port lifecycle, routing sync, validation
- **Why it is industrial:** Numbering-grade porting with routing correctness

## JAVA-355 — OSS Inventory & Network Topology Store

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / OSS
- **Business problem:** Network inventory must model topology with versions and impact analysis.
- **Core engineering problem:** Inventory/topology store with versioning, discovery and impact queries.
- **Architecture:** Modular monolith; inventory registry; topology engine; impact analyzer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext)
- **Messaging:** Kafka (discovery events)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** Topology modeling, versions, impact
- **Why it is industrial:** OSS-grade inventory with topology impact analysis

## JAVA-356 — Configuration Drift & Compliance Scanner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Assurance
- **Business problem:** Configuration drift from golden configs must be detected and remediated.
- **Core engineering problem:** Drift scanner with golden-config comparison, policies and remediation.
- **Architecture:** Modular monolith; config store; diff engine; policy rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (drift events)
- **Security architecture:** RBAC, change approvals, audit
- **Key advanced concepts:** Golden configs, diffs, remediation
- **Why it is industrial:** Assurance-grade drift detection with remediation

## JAVA-357 — Bandwidth Policy & DPI Rule Provisioner

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Policy
- **Business problem:** Bandwidth policies must be provisioned per subscriber with fair-use enforcement.
- **Core engineering problem:** Policy provisioner with bandwidth rules, DPI-style classification and enforcement.
- **Architecture:** Modular monolith; policy engine; classification rules; enforcement API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (policy events)
- **Security architecture:** RBAC, policy versioning, audit
- **Key advanced concepts:** Bandwidth rules, classification, enforcement
- **Why it is industrial:** Policy-grade provisioning with fair-use rules

## JAVA-358 — CDN Request Routing & Cache Purge

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / CDN
- **Business problem:** CDN requests must be routed to healthy edges with cache purging and consistency.
- **Core engineering problem:** CDN routing with edge health, cache invalidation and purge propagation.
- **Architecture:** Modular monolith; routing engine; health checks; purge service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (purge events)
- **Security architecture:** RBAC, API keys, audit
- **Key advanced concepts:** Edge routing, invalidation, purges
- **Why it is industrial:** CDN-grade routing with purge consistency

## JAVA-359 — Video Transcoding Job Orchestrator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Media / Transcoding
- **Business problem:** Video jobs must be transcoded with priorities, profiles and failure handling.
- **Core engineering problem:** Transcoding orchestrator with job queue, profiles and retry policies.
- **Architecture:** Modular monolith; job queue; profile store; retry engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (job events)
- **Security architecture:** RBAC, content protection, audit
- **Key advanced concepts:** Job priorities, profiles, retries
- **Why it is industrial:** Transcode-grade orchestration with failure recovery

## JAVA-360 — Live Stream Playout & Ad Stitch Scheduler

- **Difficulty:** Architect (Tier 3)
- **Industry:** Media / Streaming
- **Business problem:** Live streams need ad stitching with SCTE-style markers and scheduling.
- **Core engineering problem:** Ad stitch scheduler with marker processing, ad decisions and manifests.
- **Architecture:** Modular monolith; marker processor; ad decision service; manifest builder
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (marker events)
- **Security architecture:** RBAC, ad-campaign scoping, audit
- **Key advanced concepts:** SCTE-style markers, ad decisions, manifests
- **Why it is industrial:** Streaming-grade ad stitching with marker integrity

## JAVA-361 — IPTV Channel Lineup & EPG Service

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / IPTV
- **Business problem:** IPTV channel lineups must be managed with EPG data and entitlements.
- **Core engineering problem:** Lineup service with EPG ingestion, entitlements and guide APIs.
- **Architecture:** Modular monolith; lineup store; EPG pipeline; entitlement engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (EPG events)
- **Security architecture:** RBAC, content entitlements, audit
- **Key advanced concepts:** EPG ingestion, entitlements, guides
- **Why it is industrial:** IPTV-grade lineup management with entitlements

## JAVA-362 — Content Delivery Health & QoS Monitor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / QoS
- **Business problem:** Content delivery health must be monitored with QoS scoring and issue localization.
- **Core engineering problem:** Delivery monitor with QoS metrics, scoring and localization.
- **Architecture:** Modular monolith; telemetry ingestion; QoS scoring; localization
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (QoS streams)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** QoS scoring, localization, alerts
- **Why it is industrial:** Delivery-grade QoS monitoring with localization

## JAVA-363 — Viewer Session & Concurrency Telemetry

- **Difficulty:** Architect (Tier 3)
- **Industry:** Media / Analytics
- **Business problem:** Viewer sessions and concurrency must be tracked for capacity and engagement.
- **Core engineering problem:** Session telemetry with concurrency counting, engagement metrics and alerts.
- **Architecture:** Modular monolith; session pipeline; concurrency engine; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), Redis 7
- **Messaging:** Kafka (session events)
- **Security architecture:** RBAC, privacy controls, audit
- **Key advanced concepts:** Concurrency counting, engagement, alerts
- **Why it is industrial:** Streaming-grade telemetry with concurrency accuracy

## JAVA-364 — Digital Rights Enforcement Gateway

- **Difficulty:** Architect (Tier 3)
- **Industry:** Media / DRM
- **Business problem:** Content must be protected with license issuance, keys and playback policies.
- **Core engineering problem:** DRM gateway with license issuance, key management and policy enforcement.
- **Architecture:** Modular monolith; license service; key store; policy engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (license events)
- **Security architecture:** Content keys encrypted, device auth, audit
- **Key advanced concepts:** License issuance, key rotation, policies
- **Why it is industrial:** DRM-grade license management with key security

## JAVA-365 — Broadcast Playout & Schedule Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Media / Broadcast
- **Business problem:** Broadcast playout must schedule with secondary events, rights and automation.
- **Core engineering problem:** Playout scheduler with event timing, rights checks and automation triggers.
- **Architecture:** Modular monolith; schedule engine; rights checker; automation API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (playout events)
- **Security architecture:** RBAC, schedule approvals, audit
- **Key advanced concepts:** Event timing, rights checks, triggers
- **Why it is industrial:** Broadcast-grade playout with rights enforcement

## JAVA-366 — Telecom Order Fulfillment Orchestrator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Fulfillment
- **Business problem:** Customer orders must orchestrate across CRM, provisioning and billing.
- **Core engineering problem:** Order orchestration with decomposition, dependency ordering and fallouts.
- **Architecture:** Modular monolith; order engine; decomposition; fallout handling
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (order events)
- **Security architecture:** RBAC, four-eyes changes, audit
- **Key advanced concepts:** Order decomposition, dependencies, fallouts
- **Why it is industrial:** Fulfillment-grade orchestration with fallout recovery

## JAVA-367 — Field Force Workforce Management (telco)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Workforce
- **Business problem:** Field technicians must be scheduled with skills, geography and SLAs.
- **Core engineering problem:** Field WFM with skill matching, route clustering and SLA timers.
- **Architecture:** Modular monolith; dispatch engine; skill matcher; SLA service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (job events)
- **Security architecture:** RBAC, technician scoping, audit
- **Key advanced concepts:** Skill matching, clustering, SLAs
- **Why it is industrial:** WFM-grade dispatching with SLA management

## JAVA-368 — Network Lab Environment Booking & Reset

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Labs
- **Business problem:** Network lab environments must be booked, reset and torn down safely.
- **Core engineering problem:** Lab management with bookings, sandbox isolation and reset automation.
- **Architecture:** Modular monolith; lab registry; booking engine; reset automation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (lab events)
- **Security architecture:** RBAC, sandbox isolation, audit
- **Key advanced concepts:** Bookings, isolation, resets
- **Why it is industrial:** Lab-grade management with safe resets

## JAVA-369 — BGP Peer Health & Route Leak Detector

- **Difficulty:** Architect (Tier 3)
- **Industry:** Networking / BGP
- **Business problem:** BGP peers must be monitored for route leaks, hijacks and health anomalies.
- **Core engineering problem:** BGP monitor with route analysis, leak detection and health scoring.
- **Architecture:** Modular monolith; BGP ingestion; route analyzer; leak detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (BGP events)
- **Security architecture:** RBAC, NOC scoping, audit
- **Key advanced concepts:** Route analysis, leak detection, scoring
- **Why it is industrial:** BGP-grade monitoring with leak detection

## JAVA-370 — Peering Settlement & Traffic Accounting

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Peering
- **Business problem:** Peering traffic must be accounted and settled with dispute resolution.
- **Core engineering problem:** Peering settlement with traffic accounting, rate cards and disputes.
- **Architecture:** Modular monolith; traffic accounting; settlement engine; dispute workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (flow data)
- **Security architecture:** RBAC, four-eyes settlements, audit
- **Key advanced concepts:** Traffic accounting, rate cards, disputes
- **Why it is industrial:** Peering-grade settlement with dispute workflows

## JAVA-371 — Router Config Backup & Diff Auditor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Networking / Ops
- **Business problem:** Router configurations must be backed up, diffed and reviewed for safety.
- **Core engineering problem:** Config backup with diff analysis, review gates and rollback.
- **Architecture:** Modular monolith; config store; diff engine; review workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (config events)
- **Security architecture:** RBAC, change approvals, audit
- **Key advanced concepts:** Diffs, review gates, rollbacks
- **Why it is industrial:** Network-grade config management with safety gates

## JAVA-372 — Spectrum Monitoring & Interference Mapper

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Spectrum
- **Business problem:** Spectrum monitoring must map interference sources and correlate with complaints.
- **Core engineering problem:** Interference mapper with signal reports, geolocation and complaint correlation.
- **Architecture:** Modular monolith; report ingestion; geolocation engine; correlation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (signal events)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** Geolocation, correlation, complaint linkage
- **Why it is industrial:** Spectrum-grade interference mapping with correlation

## JAVA-373 — Fiber Plant GIS & Splice Records

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / OSP
- **Business problem:** Fiber plant records with splice points and OTDR-style traces must be maintained.
- **Core engineering problem:** Fiber GIS with splice records, traces and fault localization.
- **Architecture:** Modular monolith; fiber registry; splice store; fault locator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (OTDR events)
- **Security architecture:** RBAC, plant scoping, audit
- **Key advanced concepts:** Splice records, traces, localization
- **Why it is industrial:** OSP-grade fiber management with fault localization

## JAVA-374 — Cable Fault Localization Assistant

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Assurance
- **Business problem:** Cable faults must be localized from test data with dispatch recommendations.
- **Core engineering problem:** Fault localization with test-data analysis and dispatch workflows.
- **Architecture:** Modular monolith; test ingestion; localization engine; dispatch
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (test events)
- **Security architecture:** RBAC, region scoping, audit
- **Key advanced concepts:** Localization, dispatch, SLA
- **Why it is industrial:** Assurance-grade localization with dispatch integration

## JAVA-375 — Data Usage Metering & Fair-Use Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / BSS
- **Business problem:** Data usage must be metered with fair-use policies and throttling decisions.
- **Core engineering problem:** Metering engine with usage buckets, fair-use rules and throttle events.
- **Architecture:** Modular monolith; metering pipeline; policy rules; throttle service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (usage events)
- **Security architecture:** RBAC, policy versioning, audit
- **Key advanced concepts:** Usage buckets, fair-use, throttling
- **Why it is industrial:** BSS-grade metering with fair-use enforcement

## JAVA-376 — Roaming Partner Settlement & TAP-style Files

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Roaming
- **Business problem:** Roaming settlements must process TAP-style files with rate application.
- **Core engineering problem:** Roaming settlement with TAP-style file processing, rating and disputes.
- **Architecture:** Modular monolith; file pipeline; rating engine; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (roaming events)
- **Security architecture:** RBAC, four-eyes settlements, audit
- **Key advanced concepts:** TAP-style files, rating, disputes
- **Why it is industrial:** Roaming-grade settlement with file integrity

## JAVA-377 — Voucher & Top-Up Batch Generation

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Prepaid
- **Business problem:** Voucher and top-up batches must be generated securely with denomination control.
- **Core engineering problem:** Voucher batch engine with secure generation, activation and reconciliation.
- **Architecture:** Modular monolith; batch generator; activation service; reconciliation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (top-up events)
- **Security architecture:** RBAC, PIN security, batch audit
- **Key advanced concepts:** Secure batches, activation, reconciliation
- **Why it is industrial:** Prepaid-grade voucher management with PIN security

## JAVA-378 — Fraudulent Call Pattern Detection

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Fraud
- **Business problem:** Fraudulent call patterns must be detected in real time with disconnect actions.
- **Core engineering problem:** Call fraud detection with pattern rules, scoring and actions.
- **Architecture:** Modular monolith; pattern engine; scoring; action service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (CDR streams)
- **Security architecture:** RBAC, action audit, four-eyes
- **Key advanced concepts:** Pattern rules, scoring, actions
- **Why it is industrial:** Fraud-grade detection with automated countermeasures

## JAVA-379 — SIM Box & Bypass Fraud Detector

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Fraud
- **Business problem:** SIM box and bypass fraud must be detected from CDR signatures.
- **Core engineering problem:** SIM-box detector with CDR signature analysis and investigation cases.
- **Architecture:** Modular monolith; signature engine; CDR analysis; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (CDR streams)
- **Security architecture:** RBAC, case confidentiality, audit
- **Key advanced concepts:** Signature detection, cases, dispositions
- **Why it is industrial:** Fraud-grade SIM-box detection with case management

## JAVA-380 — Location-Based Service & Geofence Engine

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / LBS
- **Business problem:** Location services must process geofences and subscriber location updates.
- **Core engineering problem:** Geofence engine with location events, zone logic and notifications.
- **Architecture:** Modular monolith; location pipeline; geofence service; notifications
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext), Redis 7
- **Messaging:** Kafka (location events)
- **Security architecture:** RBAC, location privacy consent, audit
- **Key advanced concepts:** Geofence logic, zones, notifications
- **Why it is industrial:** LBS-grade geofencing with consent-aware privacy

## JAVA-381 — Emergency Call Routing & Priority Handling

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Emergency
- **Business problem:** Emergency calls must be prioritized and routed with location context.
- **Core engineering problem:** Emergency routing with priority queues, location lookup and logging.
- **Architecture:** Modular monolith; priority queue; location service; call log
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (call events)
- **Security architecture:** RBAC, emergency log integrity, audit
- **Key advanced concepts:** Priority routing, location, logging
- **Why it is industrial:** Emergency-grade routing with mandatory logging

## JAVA-382 — Customer Experience Score Aggregator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / CX
- **Business problem:** Customer experience must be scored from network, billing and support signals.
- **Core engineering problem:** CX score aggregator with signal weighting, journeys and alerts.
- **Architecture:** Modular monolith; signal ingestion; scoring engine; journey service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (signal streams)
- **Security architecture:** RBAC, customer privacy, audit
- **Key advanced concepts:** Signal weighting, journeys, alerts
- **Why it is industrial:** CX-grade scoring with journey context

## JAVA-383 — Broadband Speed Test Aggregation Service

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Broadband
- **Business problem:** Speed tests must be aggregated with location and plan context for quality insights.
- **Core engineering problem:** Speed-test aggregation with plan benchmarks and issue detection.
- **Architecture:** Modular monolith; test ingestion; benchmark engine; issue detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (test events)
- **Security architecture:** RBAC, privacy controls, audit
- **Key advanced concepts:** Aggregation, benchmarks, issues
- **Why it is industrial:** Broadband-grade aggregation with benchmark comparison

## JAVA-384 — Mesh Network Self-Healing Coordinator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Mesh
- **Business problem:** Mesh networks must self-heal with topology updates and re-routing.
- **Core engineering problem:** Self-healing coordinator with topology awareness, re-routing and alerts.
- **Architecture:** Modular monolith; topology store; healing engine; re-route service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (mesh events)
- **Security architecture:** RBAC, device identity, audit
- **Key advanced concepts:** Self-healing, re-routing, topologies
- **Why it is industrial:** Mesh-grade resilience with automated re-routing

## JAVA-385 — IoT Device Provisioning for Operators

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / IoT
- **Business problem:** Operator IoT platforms must provision devices with connectivity profiles and quotas.
- **Core engineering problem:** IoT provisioning with device identities, profiles and quota management.
- **Architecture:** Modular monolith; device registry; profile engine; quota service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** MQTT broker
- **Security architecture:** Device certs, RBAC, audit
- **Key advanced concepts:** Device identity, profiles, quotas
- **Why it is industrial:** IoT-grade provisioning with device identity

## JAVA-386 — Voicemail Platform & Transcription Queue

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Voicemail
- **Business problem:** Voicemail platforms must store, transcribe (local) and notify subscribers.
- **Core engineering problem:** Voicemail service with storage, local transcription queue and notifications.
- **Architecture:** Modular monolith; message store; transcription queue; notification service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (VM events)
- **Security architecture:** RBAC, subscriber privacy, audit
- **Key advanced concepts:** Transcription queue, notifications, storage
- **Why it is industrial:** Voicemail-grade platform with transcription queueing

## JAVA-387 — Unified Comms Presence & Busy-Lamp Service

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / UC
- **Business problem:** Unified comms need presence, busy-lamp and contact aggregation.
- **Core engineering problem:** Presence service with subscription fanout, busy-lamp and aggregation.
- **Architecture:** Modular monolith; presence engine; subscription store; aggregator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (presence events)
- **Security architecture:** RBAC, presence privacy, audit
- **Key advanced concepts:** Presence fanout, subscriptions, aggregation
- **Why it is industrial:** UC-grade presence with subscription fanout

## JAVA-388 — Contact Center Routing Engine (telco)

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Contact Center
- **Business problem:** Contact-center routing must match skills, queues and SLAs in real time.
- **Core engineering problem:** ACD-style routing with skill matching, queue policies and SLA timers.
- **Architecture:** Modular monolith; routing engine; queue service; SLA timers
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (call events)
- **Security architecture:** RBAC, agent scoping, audit
- **Key advanced concepts:** Skill routing, queue policies, SLAs
- **Why it is industrial:** ACD-grade routing with SLA-aware queueing

## JAVA-389 — Trunk Capacity & Erlang-B Simulator

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Planning
- **Business problem:** Trunk capacity must be sized with Erlang models and grade-of-service targets.
- **Core engineering problem:** Erlang-B/C simulator with traffic loads, blocking and sizing recommendations.
- **Architecture:** Modular monolith; Erlang engine; traffic models; sizing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, planner scoping, audit
- **Key advanced concepts:** Erlang math, blocking, sizing
- **Why it is industrial:** Planning-grade trunk sizing with Erlang accuracy

## JAVA-390 — Network Event Streaming & CEP Processor

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / OSS
- **Business problem:** Network events must be processed as complex event streams with correlation windows.
- **Core engineering problem:** CEP processor with event windows, pattern matching and actions.
- **Architecture:** Modular monolith; CEP engine; window service; action executor
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (network events)
- **Security architecture:** RBAC, NOC scoping, audit
- **Key advanced concepts:** Event windows, patterns, actions
- **Why it is industrial:** CEP-grade stream processing with windowed patterns

## JAVA-391 — Service Activation & Test Harness

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Activation
- **Business problem:** Service activation must test circuits end-to-end with automated test harnesses.
- **Core engineering problem:** Activation with test orchestration, diagnostics and completion gates.
- **Architecture:** Modular monolith; activation workflow; test harness; diagnostics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (activation events)
- **Security architecture:** RBAC, four-eyes, audit
- **Key advanced concepts:** Test orchestration, diagnostics, gates
- **Why it is industrial:** Activation-grade testing with completion gates

## JAVA-392 — Telecom Billing Mediation Pipeline

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Billing
- **Business problem:** Usage events must be mediated, normalized and enriched for rating.
- **Core engineering problem:** Mediation pipeline with event normalization, enrichment and deduplication.
- **Architecture:** Modular monolith; mediation pipeline; normalization; enrichment
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (usage events)
- **Security architecture:** RBAC, event integrity, audit
- **Key advanced concepts:** Normalization, enrichment, dedup
- **Why it is industrial:** Mediation-grade pipeline with event integrity

## JAVA-393 — Rating Engine for Usage Events

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Billing
- **Business problem:** Rating engines must price usage events per plan with accurate rounding.
- **Core engineering problem:** Rating engine with plan logic, rounding rules and dispute support.
- **Architecture:** Modular monolith; rating service; plan store; rounding engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (rated events)
- **Security architecture:** RBAC, plan versioning, audit
- **Key advanced concepts:** Plan logic, rounding, disputes
- **Why it is industrial:** Rating-grade pricing with rounding correctness

## JAVA-394 — Campaign SMS Shortcode Registry

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Messaging
- **Business problem:** Shortcodes must be provisioned, validated and monitored for campaigns.
- **Core engineering problem:** Shortcode registry with validation, provisioning and usage monitoring.
- **Architecture:** Modular monolith; registry service; validation engine; monitor
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (shortcode events)
- **Security architecture:** RBAC, content approval, audit
- **Key advanced concepts:** Provisioning, validation, monitoring
- **Why it is industrial:** Shortcode-grade governance with validation

## JAVA-395 — Call Detail Record Enrichment Pipeline

- **Difficulty:** Architect (Tier 3)
- **Industry:** Telecom / Analytics
- **Business problem:** CDRs must be enriched with subscriber, plan and location context for analytics.
- **Core engineering problem:** CDR enrichment with joins, lookups and quality validation.
- **Architecture:** Modular monolith; enrichment pipeline; lookup caches; validation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (CDR streams)
- **Security architecture:** RBAC, data masking, audit
- **Key advanced concepts:** Enrichment, lookups, validation
- **Why it is industrial:** Analytics-grade enrichment with data quality
