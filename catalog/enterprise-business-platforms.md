# Enterprise Business Platforms — Catalog

110 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-001 — Workforce Compliance Evidence Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise HR / Compliance
- **Business problem:** Regulators fine organizations that cannot evidence who had access to what and when; auditors demand a provable chain from grant to revocation.
- **Core engineering problem:** Correlating access events, role assignments, policy rules and approvals into a tamper-evident evidence chain.
- **Architecture:** Modular monolith; event-sourced evidence log; CQRS read models; scheduled correlation jobs
- **Java technology stack:** Spring Boot 3, Spring Modulith, Spring Security, Spring Data JPA, Flyway, Quartz, Resilience4j, OTel
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (access events, violation alerts)
- **Security architecture:** OIDC, RBAC+ABAC, Argon2id, hash-chained audit entries, field-level masking
- **Key advanced concepts:** Event sourcing, hash chaining, rule engine, sagas, DLQ
- **Why it is industrial:** Immutable compliance evidence, auditor-grade exports, zero-trust attestation

## JAVA-002 — Procure-to-Pay Reconciliation Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Procurement / Finance
- **Business problem:** Three-way mismatches (PO, goods receipt, invoice) across ERPs create payment leakage, disputes and audit findings.
- **Core engineering problem:** Fuzzy, rule-based matching of millions of POs, receipts and invoices with tolerance rules and exception routing.
- **Architecture:** Modular monolith; outbox + events; worklist exception management; batch ingestion
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, Resilience4j, Apache POI
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (invoice events, approval tasks)
- **Security architecture:** OIDC, RBAC, four-eyes approval, segregation-of-duties checks, audit trail
- **Key advanced concepts:** Matching rules DSL, tolerance engine, outbox, optimistic locking, idempotency
- **Why it is industrial:** Finance-grade matching with SoD enforcement and GR/IR accounting integration

## JAVA-003 — Contract Lifecycle & Obligation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Legal / Enterprise
- **Business problem:** Obligations buried in contracts (renewals, discounts, exit rights) are missed, costing millions and breaching compliance.
- **Core engineering problem:** Extracting obligations into a schedule with rules, alerts, approvals and expiry state machines.
- **Architecture:** Modular monolith; obligation state machines; reminder scheduler; approval workflows
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz, OpenSearch client
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (obligation-due events)
- **Security architecture:** OIDC, ABAC clause-level permissions, redaction of sensitive clauses, audit log
- **Key advanced concepts:** State machines, full-text clause search, version diffing, scheduler, DLQ
- **Why it is industrial:** Contract versioning with diffs, obligation SLA tracking, clause-level security

## JAVA-004 — Enterprise Document Governance Vault

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise Content / Records
- **Business problem:** Unmanaged documents violate retention schedules, leak sensitive data and fail discovery requests.
- **Core engineering problem:** Classify, retain, dispose and legally hold documents across a federated content estate.
- **Architecture:** Modular monolith; classification pipeline; retention policy engine; quarantine workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache Tika, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO, OpenSearch 2
- **Messaging:** RabbitMQ (classification jobs, disposal tasks)
- **Security architecture:** ABAC on classification labels, AES-256 at rest, legal-hold flagging, chain-of-custody
- **Key advanced concepts:** Retention scheduler, legal hold, Tika metadata extraction, disposition proof
- **Why it is industrial:** Litigation-grade custody, retention policies, immutability and disposal certificates

## JAVA-005 — Dynamic Workflow Orchestration Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise / SaaS
- **Business problem:** Business processes span teams and systems; hard-coded flows break on every reorg and ad-hoc work goes untracked.
- **Core engineering problem:** Versioned, model-driven workflows with human tasks, timers, escalation and dynamic routing.
- **Architecture:** Modular monolith; BPMN-style model store; workflow engine; task inbox service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Flowable (local BPM engine)
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (workflow events)
- **Security architecture:** RBAC per process definition, delegation rules, four-eyes on approvals
- **Key advanced concepts:** BPMN models, versioned definitions, timers, escalation, compensation, replay
- **Why it is industrial:** Versioned process models, audit-grade task history, production-grade BPM

## JAVA-006 — Audit-Grade Approval & Policy Chain Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Governance / Risk
- **Business problem:** Approvals must prove WHO approved WHAT under WHICH policy version at the time of the decision.
- **Core engineering problem:** Policy-versioned approval chains with immutable decision records and signature-grade evidence.
- **Architecture:** Modular monolith; policy store with versioning; approval service; evidence ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Hibernate Envers
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (approval requests, decisions)
- **Security architecture:** OIDC, RBAC, dual control, policy-version binding, digital signature simulation
- **Key advanced concepts:** Policy versioning, decision immutability, SoD, appeal workflow
- **Why it is industrial:** Decisions bound to policy versions; auditor-grade decision evidence

## JAVA-007 — Legal Matter & Conflict Intelligence

- **Difficulty:** Advanced (Tier 1)
- **Industry:** LegalTech
- **Business problem:** Law firms take matters that conflict with existing clients; court deadlines slip and cause malpractice exposure.
- **Core engineering problem:** Conflict screening across a parties graph + court-calendar deadline computation.
- **Architecture:** Modular monolith; graph screening; calendar rule engine; docket tracking
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Hibernate Search
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (conflict alerts)
- **Security architecture:** Matter-level ABAC, ethical walls (screening barriers), audit log
- **Key advanced concepts:** Graph screening, deadline rules, ethical walls, fuzzy name matching
- **Why it is industrial:** Ethical-wall enforcement, court-calendar math, privilege-aware search

## JAVA-008 — Expense Fraud & Policy Analytics Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Finance / Audit
- **Business problem:** Expense fraud (split receipts, duplicate claims, weekend mileage) slips through manual review.
- **Core engineering problem:** Scoring claims against policy rules and peer patterns; explainable fraud flags.
- **Architecture:** Modular monolith; scoring pipeline; rules + statistical detectors; case workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, JGraphT
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (claim events)
- **Security architecture:** RBAC, four-eyes, PII masking, whistleblower channel
- **Key advanced concepts:** Rules + anomaly scoring, duplicate clustering, explainable cases
- **Why it is industrial:** Policy-aware scoring with evidence packages; auditor-verifiable flags

## JAVA-009 — Fleet Maintenance Planning System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise Fleet
- **Business problem:** Unscheduled downtime and compliance violations from missed inspections on company vehicles.
- **Core engineering problem:** Meter/calendar-based maintenance scheduling with parts, labor and compliance constraints.
- **Architecture:** Modular monolith; scheduling engine; work order lifecycle; parts kitting
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (service-due events)
- **Security architecture:** RBAC, workshop vs driver roles, odometer tamper checks, audit
- **Key advanced concepts:** Due-soon forecasting, parts reservation, SLA tracking, compliance rules
- **Why it is industrial:** Fleet-wide SLA, compliance inspection ledger, cost analytics per asset

## JAVA-010 — Capacity & Shift Rostering Optimizer

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Workforce Management
- **Business problem:** Scheduling staff to demand curves while respecting labor law, skills, fatigue and fairness.
- **Core engineering problem:** Constraint-based rostering with fairness scoring and rule validation.
- **Architecture:** Modular monolith; rostering solver; rules engine; self-service portal
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold Solver
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (roster-published events)
- **Security architecture:** RBAC, manager/employee scopes, PII protection, audit
- **Key advanced concepts:** Constraint programming, fairness metrics, shift-swap approvals
- **Why it is industrial:** Labor-law rules, soft/hard constraints, explainable rosters

## JAVA-011 — Vendor Risk & SLA Intelligence

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Procurement / Risk
- **Business problem:** Vendors miss SLAs but renewals happen anyway because performance evidence is scattered.
- **Core engineering problem:** Continuous SLA computation from service events + periodic risk assessments.
- **Architecture:** Modular monolith; SLA event processor; risk scorecard; quarterly review workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Micrometer
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (SLA events, review reminders)
- **Security architecture:** RBAC, ABAC per vendor record, audit, four-eyes on risk acceptance
- **Key advanced concepts:** SLA windows, weighted scorecards, review workflows
- **Why it is industrial:** Evidence-based renewals, breach-credit computation, risk registers

## JAVA-012 — Training & Competency Evidence Manager

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR / Compliance
- **Business problem:** Regulated staff must prove current competency before touching systems; expiry causes work stoppage.
- **Core engineering problem:** Competency matrix vs role requirements, expiry monitoring, evidence attachments.
- **Architecture:** Modular monolith; competency rules; expiry scheduler; evidence vault
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (expiry events, gate checks)
- **Security architecture:** RBAC, evidence immutability, versioned sign-offs
- **Key advanced concepts:** Competency matrix, expiry windows, system-access gating
- **Why it is industrial:** Access gated on competency; audit-ready evidence store

## JAVA-013 — Enterprise Search Across Policies & Records

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise Knowledge
- **Business problem:** Employees cannot find governed policies; shadow copies cause stale-policy violations.
- **Core engineering problem:** Secure federated full-text search with per-document ACLs and synonyms.
- **Architecture:** Modular monolith; indexing pipeline; query service with ACL filtering
- **Java technology stack:** Spring Boot 3, Spring Security, OpenSearch client, Spring Data JPA
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (reindex events)
- **Security architecture:** ACL-aware search, field masking, audit of searches
- **Key advanced concepts:** ACL post-filtering, relevance tuning, synonym management
- **Why it is industrial:** Search results never leak documents a user cannot read; staleness alerts

## JAVA-014 — Assets & Depreciation Ledger Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Finance / Accounting
- **Business problem:** Asset registers diverge from reality; depreciation methods per GAAP/local tax rules must be recomputed.
- **Core engineering problem:** Multi-book depreciation engine with retro adjustments and disposal gains/losses.
- **Architecture:** Modular monolith; period-close batch; approval workflows; reports
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (period-close events)
- **Security architecture:** RBAC, four-eyes on manual adjustments, immutable posting audit
- **Key advanced concepts:** Multi-book accounting, retro depreciation, period close, reconciliations
- **Why it is industrial:** Dual-basis depreciation (tax vs GAAP), audit-grade journal trail

## JAVA-015 — Recruitment Pipeline & Hiring Analytics

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR Tech
- **Business problem:** Hiring is opaque: pipeline stages, interviewer feedback and offers live scattered in email.
- **Core engineering problem:** Structured candidate pipeline with SLA timers, structured interviews and offer approvals.
- **Architecture:** Modular monolith; pipeline state machine; interview scheduling; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (stage-change events)
- **Security architecture:** RBAC, GDPR erasure support, panel-based feedback visibility
- **Key advanced concepts:** Pipeline SLAs, structured interviews, offer approval chains
- **Why it is industrial:** Stage SLA analytics, fairness audit trail, GDPR-compliant retention

## JAVA-016 — Employee Offboarding & Access Revocation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Identity / HR
- **Business problem:** Departed employees keep access; offboarding drags for days, creating security exposure.
- **Core engineering problem:** Orchestrate multi-system revocation with proof of completion and escalation.
- **Architecture:** Modular monolith; revocation orchestrator; retry/compensation; proof ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (revocation commands, completion proofs)
- **Security architecture:** OIDC, RBAC, zero-standing-access goal, cryptographic proof records
- **Key advanced concepts:** Saga compensation, retries with backoff, proof collection
- **Why it is industrial:** Provable revocation across systems; auditor reports; SLA tracking

## JAVA-017 — Compensation Planning & Equity Ledger

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR / Finance
- **Business problem:** Compensation cycles with equity grants must be accurate, approval-gated and versioned.
- **Core engineering problem:** Merit/bonus/equity cycle engine with budgets, approvals and vesting schedules.
- **Architecture:** Modular monolith; cycle workflow; budget rollups; vesting calculator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (cycle events)
- **Security architecture:** RBAC + strict comp-access class, dual control, PII masking
- **Key advanced concepts:** Budget rollups, vesting schedules, waterfall approvals
- **Why it is industrial:** Comp-committee reports, deferred grants, audit-grade cycle history

## JAVA-018 — Time, Attendance & Labor Compliance Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Workforce / Compliance
- **Business problem:** Shift-work laws (breaks, maximum hours, night premiums) vary by jurisdiction; violations are expensive.
- **Core engineering problem:** Jurisdiction-aware rule engine over raw punches with exception workflow.
- **Architecture:** Modular monolith; rule engine; exception queue; payroll export
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (exception events)
- **Security architecture:** RBAC, biometric-data handling policy, tamper-evident punches
- **Key advanced concepts:** Jurisdiction rule packs, exceptions, retro corrections
- **Why it is industrial:** Multi-jurisdiction labor rules, court-defensible exception handling

## JAVA-019 — Payroll Rules Engine with Retro Calculation

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Payroll / Finance
- **Business problem:** Retroactive pay changes (backdated raises) require recomputing past periods without corrupting posted payroll.
- **Core engineering problem:** Versioned pay elements with retro deltas; recompute past periods deterministically.
- **Architecture:** Modular monolith; element engine; retro batch; posting ledger
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (payroll events)
- **Security architecture:** RBAC, SoD between entry and approval, PII encryption
- **Key advanced concepts:** Retro deltas, deterministic recompute, payroll posting integrity
- **Why it is industrial:** Retro pay deltas, tax-year correctness, immutable posted payroll

## JAVA-020 — Benefit Elections & Life-Event Processing

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR / Benefits
- **Business problem:** Life events (marriage, birth) must trigger eligible benefit changes within strict windows.
- **Core engineering problem:** Event-driven benefits administration with eligibility rules and evidence capture.
- **Architecture:** Modular monolith; eligibility rules; evidence workflow; carrier file exports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (life events, carrier exports)
- **Security architecture:** PHI protection, ABAC on dependent records, audit
- **Key advanced concepts:** Eligibility windows, evidence, EDI-style exports
- **Why it is industrial:** HIPAA-style PHI handling, strict event windows, carrier integration

## JAVA-021 — Corporate Travel Orchestration Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Travel / Enterprise
- **Business problem:** Travel policy compliance checked AFTER booking creates waste and duty-of-care blind spots.
- **Core engineering problem:** Pre-trip policy checks, approval routing and traveler safety tracking.
- **Architecture:** Modular monolith; policy engine; booking gateway simulation; duty-of-care feed
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebClient
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (trip events)
- **Security architecture:** RBAC, traveler privacy, emergency-contact vault
- **Key advanced concepts:** Policy pre-checks, approval workflows, GDS-style simulation
- **Why it is industrial:** Pre-trip enforcement, duty-of-care alerts, spend analytics

## JAVA-022 — Meeting Room & Workspace Reservation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Workplace Tech
- **Business problem:** Rooms go unused or double-booked; no-shows waste capacity; hot-desking needs conflict-free booking.
- **Core engineering problem:** Resource booking with conflict detection, check-in windows and no-show release.
- **Architecture:** Modular monolith; booking service; calendar integration; check-in IoT simulation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, STOMP WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, building-level visibility scopes, audit
- **Key advanced concepts:** Conflict-free booking, check-in release, density analytics
- **Why it is industrial:** Optimistic locking on slots, no-show reclamation, utilization metrics

## JAVA-023 — Facilities Maintenance Command Center

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Facilities Management
- **Business problem:** Breakdowns in multi-site buildings are handled by ad-hoc calls; no SLA, no cost tracking, no compliance.
- **Core engineering problem:** Work order lifecycle with SLAs, vendor dispatch and preventive schedules.
- **Architecture:** Modular monolith; work orders; vendor portal; preventive maintenance scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (work-order events)
- **Security architecture:** RBAC, contractor scoped access, photo evidence, audit
- **Key advanced concepts:** SLA timers, vendor scorecards, preventive schedules
- **Why it is industrial:** Multi-site SLA dashboards, contractor compliance, cost rollups

## JAVA-024 — Lease Administration & Critical Dates Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Real Estate / Finance
- **Business problem:** Missed lease renewal/termination dates auto-renew at bad terms; rent escalations are miscalculated.
- **Core engineering problem:** Lease abstract with critical-date calendar and escalation calculations.
- **Architecture:** Modular monolith; lease data model; date rules; accounting integration
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (critical-date alerts)
- **Security architecture:** RBAC, portfolio-level ABAC, audit of term changes
- **Key advanced concepts:** Critical-date scheduling, escalation rules, options modeling
- **Why it is industrial:** Options/exercise modeling, rent math, deadline governance

## JAVA-025 — Energy Consumption & Sustainability Reporting

- **Difficulty:** Advanced (Tier 1)
- **Industry:** ESG / Sustainability
- **Business problem:** Enterprises must report scope 1/2 emissions with defensible calculations across sites.
- **Core engineering problem:** Ingest meter data, compute emissions with factor libraries, produce audit-ready reports.
- **Architecture:** Modular monolith; ingestion pipeline; factor registry; report generator
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** RabbitMQ (meter readings)
- **Security architecture:** RBAC, report attestation workflow, immutable factor versions
- **Key advanced concepts:** Factor versioning, gap estimation, attestation
- **Why it is industrial:** Audit-ready ESG math, versioned emission factors, attestation chain

## JAVA-026 — Health, Safety & Incident Management System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** EHS / Industrial Safety
- **Business problem:** Workplace incidents are under-reported; investigations and corrective actions unmanaged; OSHA-style obligations unmet.
- **Core engineering problem:** Incident intake, investigation and corrective-action closed loop with deadlines.
- **Architecture:** Modular monolith; incident workflow; CAPA; regulator-style reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (incident alerts, escalation)
- **Security architecture:** RBAC, anonymous reporting channel, legal-privilege tagging
- **Key advanced concepts:** Closed-loop CAPA, severity classification, reporting templates
- **Why it is industrial:** Regulator-grade recordkeeping, severity SLAs, anonymous intake

## JAVA-027 — Insurance Policy Administration Suite

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Insurance
- **Business problem:** Policy products multiply; agents need configurable products without an IT project per product.
- **Core engineering problem:** Product factory: rules, rates, forms composed into policies with lifecycle.
- **Architecture:** Modular monolith; product definition engine; policy lifecycle; document generation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (policy events)
- **Security architecture:** RBAC, PII encryption, agent scoping, audit
- **Key advanced concepts:** Product factory, rules DSL, renewal engine
- **Why it is industrial:** Product factory patterns, endorsement versioning, state machines

## JAVA-028 — Multi-Tenant SaaS Billing & Rating Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** SaaS / Billing
- **Business problem:** Usage-based SaaS billing must rate millions of events per tenant with plan-specific pricing and credits.
- **Core engineering problem:** High-throughput rating pipeline with tenant isolation and correct invoice generation.
- **Architecture:** Modular monolith; event ingestion; rating; invoice batch; tenant context
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, virtual threads
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (usage events)
- **Security architecture:** Tenant isolation (row-level security), API keys, RBAC per tenant
- **Key advanced concepts:** Row-level security, rating tiers, proration, credits, DLQ
- **Why it is industrial:** Tenant isolation, deterministic invoices, idempotent rating

## JAVA-029 — B2B Supplier Collaboration Portal

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Procurement / Supply
- **Business problem:** Suppliers work via email; order confirmations, ASNs and invoices arrive unstructured and late.
- **Core engineering problem:** Portal + message exchange with validation, versioned contracts and performance views.
- **Architecture:** Modular monolith; supplier portal; document exchange; SLA views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (document events)
- **Security architecture:** Supplier-scoped RBAC, invite flows, document encryption
- **Key advanced concepts:** ASN validation, portal workflows, supplier scorecards
- **Why it is industrial:** EDI-style exchange over portal, supplier-specific visibility

## JAVA-030 — Channel Partner & Rebate Settlement Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Channel Sales / Finance
- **Business problem:** Rebate programs settle on disputed data; partners dispute payouts because calculations are opaque.
- **Core engineering problem:** Rebate rule engine over sales data with accrual, disputes and settlement runs.
- **Architecture:** Modular monolith; rule engine; accrual ledger; dispute workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (sales events, settlement events)
- **Security architecture:** RBAC, partner portal scopes, four-eyes settlement approval
- **Key advanced concepts:** Accrual vs payout, dispute workflows, tiered rules
- **Why it is industrial:** Accrual/settlement duality, audit-ready payouts, dispute SLAs

## JAVA-031 — Enterprise Alerting & Escalation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise Ops
- **Business problem:** Alerts are buried in inboxes; on-call escalations are missed; paging policies are static and unaware.
- **Core engineering problem:** Deduplicated, correlated alerts with escalation policies, on-call schedules and quiet hours.
- **Architecture:** Modular monolith; alert pipeline; policy engine; notification fanout
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (alerts)
- **Security architecture:** RBAC, channel encryption, ack audit
- **Key advanced concepts:** Dedup windows, escalation chains, quiet hours, backpressure
- **Why it is industrial:** Paging reliability, dedup, escalation chains with SLAs

## JAVA-032 — Customer Master Data Management Hub

- **Difficulty:** Advanced (Tier 1)
- **Industry:** MDM / Data
- **Business problem:** Customer records are duplicated and conflicting across systems; marketing and billing disagree on truth.
- **Core engineering problem:** Golden-record resolution with survivorship rules, matching and sync-out.
- **Architecture:** Modular monolith; matching engine; golden record store; distribution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenSearch
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (record changes)
- **Security architecture:** PII masking, field-level ABAC, consent flags
- **Key advanced concepts:** Survivorship, fuzzy matching, sync conflicts, consent
- **Why it is industrial:** Golden records with lineage and merge/unmerge history

## JAVA-033 — Service Desk with SLA, Approval & Automation

- **Difficulty:** Advanced (Tier 1)
- **Industry:** IT Service Management
- **Business problem:** Tickets rot; priority is gamed; changes happen without approval; SLAs are unreported.
- **Core engineering problem:** ITIL-style ticket lifecycle with SLA timers, approval gates and automation hooks.
- **Architecture:** Modular monolith; ticket service; SLA engine; automation hooks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (ticket events)
- **Security architecture:** RBAC, customer scoping, audit of priority changes
- **Key advanced concepts:** SLA timers, approval gates, webhook automations
- **Why it is industrial:** ITIL SLAs with pause/resume, escalation and reporting

## JAVA-034 — Software License Position & Entitlement Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** ITAM / SAM
- **Business problem:** Organizations over- or under-license; vendor audits produce true-up shocks.
- **Core engineering problem:** Reconcile discovered installations against entitlements with license metrics.
- **Architecture:** Modular monolith; discovery ingestion; entitlement matching; true-up reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (discovery events)
- **Security architecture:** RBAC, audit of entitlement edits, vendor scoping
- **Key advanced concepts:** License metrics, reconciliation, true-up math
- **Why it is industrial:** Vendor-audit readiness, metric-specific counting rules

## JAVA-035 — IT Change Advisory & Release Gate Board

- **Difficulty:** Advanced (Tier 1)
- **Industry:** IT Governance
- **Business problem:** Unreviewed changes break production; CAB meetings are unstructured and unrecorded.
- **Core engineering problem:** Change workflow with risk scoring, CAB review and release gates.
- **Architecture:** Modular monolith; change tickets; risk model; CAB workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (change events)
- **Security architecture:** RBAC, SoD (author is not approver), immutable decisions
- **Key advanced concepts:** Risk scoring, CAB quorums, release gates, freeze windows
- **Why it is industrial:** Governed change with audit trails and freeze-window rules

## JAVA-036 — Onboarding Checklist & Day-1 Readiness Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR / IT Ops
- **Business problem:** New hires lack accounts, hardware and training on day one; provisioning is manual and unverifiable.
- **Core engineering problem:** Orchestrated onboarding across HR, IT and facilities with dependency-ordered tasks and proofs.
- **Architecture:** Modular monolith; onboarding plans; task orchestration; provisioning hooks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (task events)
- **Security architecture:** RBAC, PII protection, just-in-time provisioning principle
- **Key advanced concepts:** Dependency-ordered tasks, saga retries, readiness SLA
- **Why it is industrial:** Cross-department orchestration with completion evidence

## JAVA-037 — Employee Relocation & Mobility Management

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR / Mobility
- **Business problem:** Relocations involve vendors, budgets, visas and timelines that routinely blow up without orchestration.
- **Core engineering problem:** Relocation case management with policy-driven budgets and vendor coordination.
- **Architecture:** Modular monolith; case workflow; budget engine; vendor integrations
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (case events)
- **Security architecture:** RBAC, tax-data masking, vendor scoping
- **Key advanced concepts:** Policy-driven budgets, case SLAs, vendor coordination
- **Why it is industrial:** Policy-driven spend caps, multi-vendor coordination, audit

## JAVA-038 — Internal Job Marketplace & Mobility Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** HR Tech
- **Business problem:** Internal talent leaves because open roles are invisible; matching is keyword-broken.
- **Core engineering problem:** Skills-graph matching of employees to internal roles with manager approvals.
- **Architecture:** Modular monolith; skills graph; matching; application workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenSearch
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (match events)
- **Security architecture:** RBAC, confidentiality of applications, manager-chain approvals
- **Key advanced concepts:** Skills graph, ranked matching, internal applications
- **Why it is industrial:** Skills-based matching, internal mobility analytics

## JAVA-039 — Enterprise Forms, Survey & Feedback Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Enterprise Collaboration
- **Business problem:** Forms/surveys are SaaS sprawl; results leak; approvals for sensitive surveys are absent.
- **Core engineering problem:** Form builder with versioning, conditional logic, response security and analytics.
- **Architecture:** Modular monolith; form engine; response store; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (response events)
- **Security architecture:** Anonymous response vault, ABAC on results, retention rules
- **Key advanced concepts:** Conditional logic, response encryption, retention
- **Why it is industrial:** Anonymous-response integrity, approval-gated sensitive surveys

## JAVA-040 — Content Moderation & Review Workflow Suite

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Platform Trust & Safety
- **Business problem:** User-generated content must be reviewed fast with appeals, without breaking platform policy.
- **Core engineering problem:** Moderation queue with policy routing, appeals and moderator performance QA.
- **Architecture:** Modular monolith; queue routing; appeal workflow; QA sampling
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (moderation queues)
- **Security architecture:** RBAC, moderator privacy, decision audit, appeal rights
- **Key advanced concepts:** Queue routing, SLA timers, QA sampling, appeals
- **Why it is industrial:** Trust & safety ops with SLA queues and appeal integrity

## JAVA-041 — Field Service Dispatching & Optimization

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Field Service / Utilities
- **Business problem:** Technicians are dispatched manually; travel time is wasted; SLAs are missed; skills are mismatched.
- **Core engineering problem:** Constraint-based dispatch: skills, SLAs, travel windows, parts availability.
- **Architecture:** Modular monolith; dispatch engine; mobile API; parts check
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (dispatch events)
- **Security architecture:** RBAC, field/mobile scoping, customer PII masking
- **Key advanced concepts:** Constraint solving, travel windows, parts reservation
- **Why it is industrial:** Solver-based dispatch with SLA and skills constraints

## JAVA-042 — Print Shop Job Ticketing & Costing System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Print / Light Manufacturing
- **Business problem:** Print jobs are quoted without real cost data; the shop loses money on complex finishing.
- **Core engineering problem:** Job costing from machine rates, materials and finishing steps; quote-to-invoice.
- **Architecture:** Modular monolith; job tickets; costing engine; prepress workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (job-status events)
- **Security architecture:** RBAC, customer artwork protection, audit
- **Key advanced concepts:** Job costing, finishing steps, prepress checklists
- **Why it is industrial:** Machine-rate costing, quote accuracy, shop-floor tracking

## JAVA-043 — Fitness Club Membership & Class Management

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Fitness / Membership SaaS
- **Business problem:** Studios overbook classes, memberships lapse silently, attendance data is unused.
- **Core engineering problem:** Class capacity booking with waitlists, membership billing and attendance analytics.
- **Architecture:** Modular monolith; booking engine; billing; analytics
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, member privacy, payment tokenization
- **Key advanced concepts:** Waitlist promotion, no-show policies, billing runs
- **Why it is industrial:** Capacity conflicts, fair waitlists, membership lifecycle

## JAVA-044 — Restaurant Chain Back-Office Operations Hub

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Hospitality / QSR
- **Business problem:** Multi-unit restaurants manage recipes, costs and compliance inconsistently; margins leak.
- **Core engineering problem:** Central recipe/costing management with store-level variance reporting.
- **Architecture:** Modular monolith; recipe engine; store sync; variance reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (store-sync events)
- **Security architecture:** RBAC, store scoping, recipe IP protection
- **Key advanced concepts:** Recipe versioning, theoretical vs actual variance
- **Why it is industrial:** Central recipes with store variance and compliance sync

## JAVA-045 — Retail Markdown & Promotion Optimization Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail Merchandising
- **Business problem:** Markdowns too late or too deep destroy margin; promotions overlap and cannibalize.
- **Core engineering problem:** Markdown cadence rules + sell-through forecasting with promotion calendar governance.
- **Architecture:** Modular monolith; rules engine; sell-through models; calendar
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (markdown events)
- **Security architecture:** RBAC, approval limits, audit
- **Key advanced concepts:** Sell-through modeling, cadence rules, promotion conflicts
- **Why it is industrial:** Margin-aware markdowns, promotion conflict detection

## JAVA-046 — Retail Store Replenishment & Allocation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail Supply Chain
- **Business problem:** Stores starve while warehouses hold stock; allocation is spreadsheet-based.
- **Core engineering problem:** Demand-driven allocation across stores with fairness and service-level targets.
- **Architecture:** Modular monolith; forecast integration; allocation solver; ASN generation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (allocation events)
- **Security architecture:** RBAC, store scoping, audit
- **Key advanced concepts:** Fair-share allocation, service levels, ASN outputs
- **Why it is industrial:** Constraint-based allocation, DC-to-store flows

## JAVA-047 — Visual Merchandising & Planogram Compliance

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail
- **Business problem:** HQ planograms are ignored in stores; compliance is verified by expensive store visits.
- **Core engineering problem:** Planogram versioning, store tasking and photo-evidence compliance scoring.
- **Architecture:** Modular monolith; planogram store; tasking; photo evidence
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (compliance events)
- **Security architecture:** RBAC, store scoping, photo retention rules
- **Key advanced concepts:** Planogram versions, evidence scoring, tasking
- **Why it is industrial:** Evidence-based compliance with scoring and audits

## JAVA-048 — Customer Loyalty Ledger & Points Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Loyalty
- **Business problem:** Points liabilities must be ledgered exactly; fraud via point farming is common.
- **Core engineering problem:** Double-entry points ledger with expiry, promotions and fraud detection.
- **Architecture:** Modular monolith; ledger engine; expiry sweeps; fraud rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (point events)
- **Security architecture:** OIDC, member PII, ledger immutability, fraud flags
- **Key advanced concepts:** Double-entry ledger, expiry, point-farming detection
- **Why it is industrial:** Accounted points liability, immutable ledger, fraud rules

## JAVA-049 — Gift Card Issuance, Ledger & Settlement

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Payments
- **Business problem:** Gift card balances must reconcile to the cent; breakage and fraud must be tracked.
- **Core engineering problem:** Card ledger with issuance, redemption, breakage accounting and settlement.
- **Architecture:** Modular monolith; card ledger; settlement; fraud checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (redemption events)
- **Security architecture:** Card tokenization, PIN handling, ledger immutability
- **Key advanced concepts:** Breakage accounting, settlement, replay protection
- **Why it is industrial:** Payment-grade ledger with breakage and settlement

## JAVA-050 — Modern POS Back-Office & Tender Reconciliation

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Payments
- **Business problem:** Store tills vs payment processor reports never match; cash-handling fraud goes undetected.
- **Core engineering problem:** Multi-tender reconciliation (cash, card, wallet) with variance workflows.
- **Architecture:** Modular monolith; tender ingestion; reconciliation; variance cases
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (tender files)
- **Security architecture:** RBAC, dual control on voids, cash-handler profiles
- **Key advanced concepts:** Multi-tender matching, variance thresholds, void analytics
- **Why it is industrial:** Processor file matching, void analytics, cash control

## JAVA-051 — Subscription & Dunning Management Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** SaaS / Billing
- **Business problem:** Involuntary churn from failed cards; naive retries annoy users and break payment rules.
- **Core engineering problem:** Smart retry scheduling with grace periods, dunning notices and payment-method updater.
- **Architecture:** Modular monolith; billing engine; dunning state machine; webhooks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (payment events)
- **Security architecture:** PCI-scope minimization, tokenization, RBAC
- **Key advanced concepts:** Dunning states, smart retries, proration, webhooks
- **Why it is industrial:** Churn-reduction math, PCI-conscious design, idempotent webhooks

## JAVA-052 — Returns Management & Disposition Workflow

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Reverse Logistics
- **Business problem:** Returns are processed inconsistently; refund abuse and disposition waste are invisible.
- **Core engineering problem:** Return authorization with disposition routing (restock, refurbish, recycle) and abuse scoring.
- **Architecture:** Modular monolith; RMA workflow; disposition routing; abuse rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (return events)
- **Security architecture:** RBAC, refund controls, abuse scoring
- **Key advanced concepts:** Disposition routing, abuse scoring, refund windows
- **Why it is industrial:** Disposition economics, abuse detection, policy enforcement

## JAVA-053 — Referral & Affiliate Attribution Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** MarTech / Growth
- **Business problem:** Attributing conversions to referrers across devices and time windows is fraud-prone.
- **Core engineering problem:** Deterministic attribution with fraud detection (self-referral, stacking).
- **Architecture:** Modular monolith; click ingestion; attribution windows; payout engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (conversion events)
- **Security architecture:** API keys per affiliate, payout controls, fraud rules
- **Key advanced concepts:** Attribution windows, fraud rules, payouts
- **Why it is industrial:** Attribution correctness, fraud-resistant payouts

## JAVA-054 — Product Information Management Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Commerce / PIM
- **Business problem:** Product data is scattered across channels; wrong specs, prices and images reach customers.
- **Core engineering problem:** Central product model with channel syndication, validation and workflow.
- **Architecture:** Modular monolith; product model; validation; channel feeds
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (publish events)
- **Security architecture:** RBAC, channel scoping, attribute-level permissions
- **Key advanced concepts:** Channel mapping, validation rules, publishing workflows
- **Why it is industrial:** Channel-specific transformations, data-governance workflows

## JAVA-055 — Dynamic Pricing Engine for B2B Quotes

- **Difficulty:** Advanced (Tier 1)
- **Industry:** B2B Commerce / Pricing
- **Business problem:** B2B quotes take days; prices must respect contracts, tiers, margins and approvals.
- **Core engineering problem:** Quote engine combining contract prices, cost floors and approval thresholds.
- **Architecture:** Modular monolith; pricing pipeline; approval routing; quote PDFs
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (quote events)
- **Security architecture:** RBAC, margin-floor enforcement, approval limits
- **Key advanced concepts:** Price waterfalls, approval thresholds, versioning
- **Why it is industrial:** Price waterfall logic, margin protection, audit of quotes

## JAVA-056 — Catalog Syndication to Marketplace Channels

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Commerce / Marketplace Tech
- **Business problem:** Sellers must publish thousands of products to marketplaces with per-channel field maps, feeds and error handling.
- **Core engineering problem:** Bidirectional syndication with per-channel transforms, retries and listing-status sync.
- **Architecture:** Modular monolith; channel adapters; feed generators; listing state machine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (feed jobs, listing events)
- **Security architecture:** OAuth client credentials per channel, RBAC, secret vault for channel keys
- **Key advanced concepts:** Channel adapters, retries with backoff, idempotent feeds
- **Why it is industrial:** Channel-specific field mapping at scale with error quarantine

## JAVA-057 — E-Auction & Reverse Auction Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Procurement / Sourcing
- **Business problem:** Reverse auctions must be fair, tamper-evident and time-locked; suppliers must trust the platform.
- **Core engineering problem:** Sealed-bid handling with server-time locks, bid ranking and anti-collusion checks.
- **Architecture:** Modular monolith; auction engine; bid store; event clock
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (bid events)
- **Security architecture:** Bid encryption at rest, OIDC suppliers, sealed-bid integrity, audit
- **Key advanced concepts:** Sealed bids, event-sourced ranking, anti-collusion rules
- **Why it is industrial:** Trustworthy auction mechanics with tamper-evident bid log

## JAVA-058 — RMA Diagnostics & Repair Parts Advisor

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Aftermarket Services
- **Business problem:** Returns need diagnosis to route to repair vs replace; wrong routing doubles cost.
- **Core engineering problem:** Guided diagnostics with parts advisories and disposition decisions.
- **Architecture:** Modular monolith; diagnostic tree engine; parts catalog; disposition workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, serial-number checks, warranty validation
- **Key advanced concepts:** Decision trees, parts advisory, disposition routing
- **Why it is industrial:** Symptom-driven triage reducing no-fault-found returns

## JAVA-059 — Car Rental Reservation & Damage Claims

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Car Rental / Mobility
- **Business problem:** Reservations, damage claims and fleet rotation must integrate without double-booking or dispute.
- **Core engineering problem:** Booking with fleet rotation windows, damage intake and claims adjudication.
- **Architecture:** Modular monolith; booking engine; damage workflow; fleet pool
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, photo evidence, PII masking, claim four-eyes
- **Key advanced concepts:** Booking windows, evidence vault, claims workflow
- **Why it is industrial:** Fleet rotation planning, damage evidence chain, SLA dispatch

## JAVA-060 — Warehouse Slotting Optimization System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Warehouse / WMS
- **Business problem:** Slotting products by velocity reduces travel time; manual slotting decays as the mix shifts.
- **Core engineering problem:** Velocity-based slotting with aisle/zoning constraints and re-slot recommendations.
- **Architecture:** Modular monolith; slotting engine; velocity analytics; task generation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (slotting tasks)
- **Security architecture:** RBAC, warehouse zoning scopes, audit
- **Key advanced concepts:** Velocity classes, zone constraints, re-slot work orders
- **Why it is industrial:** Data-driven slotting with measurable travel-time ROI

## JAVA-061 — Order Promise (ATP) Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Supply Chain
- **Business problem:** Promising delivery dates at order time without overselling constrained inventory.
- **Core engineering problem:** ATP computation across network nodes with allocation reservations and re-promise handling.
- **Architecture:** Modular monolith; availability service; reservation ledger; promise engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, virtual threads
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (reservation events)
- **Security architecture:** RBAC, channel scoping, audit of promises
- **Key advanced concepts:** ATP/CTP logic, reservations, promise revision
- **Why it is industrial:** Accurate promises across DCs with reservation integrity

## JAVA-062 — Demand Sensing & Forecast Reconciliation

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Retail / Demand Planning
- **Business problem:** Forecasts vs actuals diverge silently; planners reconcile in spreadsheets.
- **Core engineering problem:** Forecast-vs-actual reconciliation with bias detection and overrides.
- **Architecture:** Modular monolith; forecast store; reconciliation batch; planner workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (forecast publish)
- **Security architecture:** RBAC, planner hierarchy scopes, audit
- **Key advanced concepts:** Forecast reconciliation, bias metrics, override audit
- **Why it is industrial:** Statistical reconciliation with planner-accountable overrides

## JAVA-063 — Production Planning & Finite Scheduling Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Manufacturing / Planning
- **Business problem:** Finite capacity scheduling must respect setups, tooling, changeovers and material availability.
- **Core engineering problem:** Finite scheduler with setup optimization, constraint checking and what-if runs.
- **Architecture:** Modular monolith; scheduling solver; what-if sandbox; MRP integration
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (schedule publish)
- **Security architecture:** RBAC, planner approvals, schedule freeze windows
- **Key advanced concepts:** Constraint solving, setup minimization, what-if simulation
- **Why it is industrial:** Solver-backed scheduling with changeover optimization

## JAVA-064 — Quality Non-Conformance & CAPA Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Manufacturing / Quality
- **Business problem:** Non-conformances need containment, root cause, corrective action and effectiveness checks.
- **Core engineering problem:** Closed-loop NC to CAPA workflow with effectiveness verification.
- **Architecture:** Modular monolith; NC workflow; CAPA tracker; effectiveness checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (quality events)
- **Security architecture:** RBAC, electronic sign-off, audit trail
- **Key advanced concepts:** 8D/CAPA flow, effectiveness verification, trend analytics
- **Why it is industrial:** Regulator-grade quality workflows (ISO/AS-style)

## JAVA-065 — Supplier Quality Scorecard & Audit Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Manufacturing / Supplier Quality
- **Business problem:** Supplier quality is reactive; scorecards are static and unverifiable.
- **Core engineering problem:** Live supplier scorecards from incoming inspection, lot data and corrective actions.
- **Architecture:** Modular monolith; scorecard engine; inspection data; supplier portal
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (inspection events)
- **Security architecture:** RBAC, supplier portal scopes, score dispute workflow
- **Key advanced concepts:** Weighted scorecards, dispute resolution, audit
- **Why it is industrial:** Evidence-backed supplier ratings driving sourcing decisions

## JAVA-066 — Procurement Sourcing Events & Bid Analytics

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Procurement / Sourcing
- **Business problem:** RFx events need sealed responses, weighted scoring and compliant award decisions.
- **Core engineering problem:** Multi-round sourcing with weighted evaluation and award governance.
- **Architecture:** Modular monolith; sourcing workflow; evaluation matrices; award engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (RFx events)
- **Security architecture:** RBAC, bid confidentiality walls, four-eyes awards
- **Key advanced concepts:** Weighted scoring, sealed responses, award gates
- **Why it is industrial:** Compliant, auditable sourcing decisions

## JAVA-067 — Trade Promotion Management Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** CPG / Trade Promotions
- **Business problem:** Trade spend accrues in disconnected silos; payouts exceed budgets with no attribution.
- **Core engineering problem:** Promotion planning, accrual, claim validation and deduction matching.
- **Architecture:** Modular monolith; promotion plan; accrual engine; claim workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (claim events)
- **Security architecture:** RBAC, approval limits, four-eyes payouts
- **Key advanced concepts:** Accrual vs payout, deduction matching, ROI
- **Why it is industrial:** Trade-spend control with deduction-matching accuracy

## JAVA-068 — Sales Territory, Quota & Compensation Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Sales Operations
- **Business problem:** Territories, quotas and commissions must compute fairly and auditably when plans change.
- **Core engineering problem:** Plan-versioned crediting engine with territory realignment and disputes.
- **Architecture:** Modular monolith; plan versioning; crediting engine; dispute workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (deal events)
- **Security architecture:** RBAC, compensation data masking, dispute trail
- **Key advanced concepts:** Plan versioning, crediting rules, alignment
- **Why it is industrial:** Versioned comp plans with crediting audit and disputes

## JAVA-069 — Sales Forecasting with Opportunity Risk Scoring

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Sales Analytics
- **Business problem:** Forecasts are opinion-based; pipeline risk and history must inform commit numbers.
- **Core engineering problem:** Pipeline scoring with risk-adjusted commit/upside rollups.
- **Architecture:** Modular monolith; scoring models; rollup engine; forecast workflows
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, territory visibility scopes, audit
- **Key advanced concepts:** Risk scoring, rollups, forecast categories
- **Why it is industrial:** Risk-adjusted forecasting with versioned commits

## JAVA-070 — Call Center WFM: Forecasting & Scheduling

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Contact Center Ops
- **Business problem:** Staffing against arrival patterns with shrinkage and skills is a constraint problem, not a spreadsheet.
- **Core engineering problem:** Erlang-based staffing with shrinkage, skills and adherence tracking.
- **Architecture:** Modular monolith; forecasting models; schedule optimizer; adherence feed
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Timefold
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (adherence events)
- **Security architecture:** RBAC, agent PII protection, audit
- **Key advanced concepts:** Erlang math, shrinkage, adherence
- **Why it is industrial:** Contact-center-grade staffing with shrinkage models

## JAVA-071 — Campaign Orchestration & Offer Decisioning

- **Difficulty:** Advanced (Tier 1)
- **Industry:** MarTech
- **Business problem:** Campaigns blast without decisioning; offers must be right-channel, right-time, budget-aware.
- **Core engineering problem:** Real-time offer decisioning with eligibility, frequency caps and budget pacing.
- **Architecture:** Modular monolith; decision service; campaign store; pacing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (offer events)
- **Security architecture:** RBAC, channel consent enforcement, PII masking
- **Key advanced concepts:** Decisioning, frequency caps, pacing
- **Why it is industrial:** Real-time decisioning with consent-aware channeling

## JAVA-072 — Email Reputation & Deliverability Analytics

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Email / Deliverability
- **Business problem:** Bulk email damages domain reputation; senders need bounce/complaint intelligence.
- **Core engineering problem:** Ingest FBL/bounce data, score sender reputation, throttle and quarantine.
- **Architecture:** Modular monolith; feedback pipeline; reputation scoring; throttle service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (FBL events)
- **Security architecture:** RBAC, API keys for MTA integration, audit
- **Key advanced concepts:** Reputation scoring, throttling, quarantine
- **Why it is industrial:** Deliverability operations with feedback-loop automation

## JAVA-073 — Marketing Spend Attribution & Reconciliation

- **Difficulty:** Advanced (Tier 1)
- **Industry:** MarTech / Finance
- **Business problem:** Marketing spend is untracked against revenue; attribution models are black boxes.
- **Core engineering problem:** Configurable attribution models with reconciliation against media invoices.
- **Architecture:** Modular monolith; attribution engine; invoice reconciliation; report builder
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (touch events)
- **Security architecture:** RBAC, budget owners, audit
- **Key advanced concepts:** Attribution models, reconciliation, budget pacing
- **Why it is industrial:** Finance-grade spend reconciliation with model auditability

## JAVA-074 — Digital Asset Management with Rights Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Media / DAM
- **Business problem:** Digital assets need usage rights, expirations and transformations enforced centrally.
- **Core engineering problem:** DAM with rights metadata, renditions and license enforcement.
- **Architecture:** Modular monolith; asset store; rendition pipeline; rights engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (rendition jobs)
- **Security architecture:** RBAC, rights enforcement, watermarking, audit
- **Key advanced concepts:** Renditions, rights windows, watermarks
- **Why it is industrial:** Rights-managed asset delivery with expiry enforcement

## JAVA-075 — Event Management & Badge Security Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Events / Access Control
- **Business problem:** Events need credentialed access with zone rules, capacity and counterfeit resistance.
- **Core engineering problem:** Badge issuance with zone-based access rules, capacity and reprint controls.
- **Architecture:** Modular monolith; badge service; access rules; gate readers simulation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (access events)
- **Security architecture:** QR/token badges, zone RBAC, revocation, audit
- **Key advanced concepts:** Zone access, capacity caps, revocation
- **Why it is industrial:** Credentialed access with per-zone policy and capacity

## JAVA-076 — Content Publishing Workflow & Editorial Calendar

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Media / Publishing
- **Business problem:** Editorial teams need scheduled publishing with approvals, embargoes and rollback.
- **Core engineering problem:** Scheduled publishing with embargo dates, approval gates and version history.
- **Architecture:** Modular monolith; editorial workflow; scheduler; version store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (publish events)
- **Security architecture:** RBAC, embargo enforcement, version diff
- **Key advanced concepts:** Embargoes, approvals, versioning
- **Why it is industrial:** Embargo-safe publishing with approval gates

## JAVA-077 — Job-Site Safety, Permits & Orientation System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Construction / Safety
- **Business problem:** Job sites need worker orientation, permits and badge-validated entry for compliance.
- **Core engineering problem:** Orientation tracking, permit issuance and gate validation with expiry checks.
- **Architecture:** Modular monolith; worker registry; permit engine; gate validation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (gate events)
- **Security architecture:** RBAC, badge validation, permit expiry enforcement
- **Key advanced concepts:** Permit expiry, orientation gatekeeping, analytics
- **Why it is industrial:** Site access gated on valid permits and orientations

## JAVA-078 — Subcontractor Progress & Payment Certification

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Construction / Payments
- **Business problem:** Subcontractor progress payments need certified quantities, lien waivers and retention math.
- **Core engineering problem:** Progress certification workflow with retention, lien waivers and payment release.
- **Architecture:** Modular monolith; progress claim workflow; retention engine; payment batches
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (claim events)
- **Security architecture:** RBAC, four-eyes certification, waiver tracking
- **Key advanced concepts:** Retention math, lien waivers, certification
- **Why it is industrial:** Contract-grade progress payments with retention tracking

## JAVA-079 — Equipment Rental & Utilization Marketplace

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Equipment Rental
- **Business problem:** Rental fleets suffer idle time and maintenance clashes; utilization must be optimized.
- **Core engineering problem:** Rental booking with maintenance windows, utilization pricing and damage deposits.
- **Architecture:** Modular monolith; booking engine; maintenance calendar; pricing
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (rental events)
- **Security architecture:** RBAC, deposit handling, damage evidence
- **Key advanced concepts:** Utilization pricing, maintenance windows, deposits
- **Why it is industrial:** Utilization-driven pricing with maintenance-aware booking

## JAVA-080 — Construction Materials & As-Built Traceability

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Construction / Traceability
- **Business problem:** Materials provenance and as-built records must be traceable for warranty and regulation.
- **Core engineering problem:** Lot-level material traceability from delivery to installed location.
- **Architecture:** Modular monolith; material ledger; as-built store; barcode ingestion
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (scan events)
- **Security architecture:** RBAC, immutable trace entries, audit
- **Key advanced concepts:** Lot genealogy, as-built records, recall queries
- **Why it is industrial:** Lot-level genealogy with recall-ready queries

## JAVA-081 — Tender Evaluation & Award Governance System

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Construction / Procurement
- **Business problem:** Tender awards must be transparent, compliant and audit-defensible against challenges.
- **Core engineering problem:** Multi-criteria tender evaluation with sealed bids and award justification.
- **Architecture:** Modular monolith; tender workflow; evaluation engine; award records
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (tender events)
- **Security architecture:** RBAC, bid confidentiality, four-eyes award, audit
- **Key advanced concepts:** Sealed evaluation, criteria weighting, justification
- **Why it is industrial:** Challenge-proof award governance with full audit

## JAVA-082 — Hotel Property Management Backbone

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Hospitality / PMS
- **Business problem:** Hotel PMS must handle reservations, housekeeping, billing and channels without double-booking.
- **Core engineering problem:** Room inventory with channel sync, billing folios and housekeeping flows.
- **Architecture:** Modular monolith; reservation engine; folio billing; channel adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (channel events)
- **Security architecture:** RBAC, guest PII (PCI-aware), audit
- **Key advanced concepts:** Overbooking controls, folios, channel parity
- **Why it is industrial:** Channel-parity booking with folio-level billing

## JAVA-083 — Restaurant Table Flow & Kitchen Sync Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Hospitality / QSR
- **Business problem:** Kitchen and floor must synchronize orders, wait times and table turns in real time.
- **Core engineering problem:** Table state with kitchen ticket sync, wait-time prediction and turn analytics.
- **Architecture:** Modular monolith; table service; kitchen display simulation; WebSocket
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, STOMP
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (ticket events)
- **Security architecture:** RBAC, order modification audit, void controls
- **Key advanced concepts:** Kitchen sync, wait estimates, table turns
- **Why it is industrial:** Real-time floor-kitchen synchronization at rush scale

## JAVA-084 — Salon & Clinic Appointment Grid Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Booking / Services
- **Business problem:** Salon/clinic grids must handle multi-service, multi-staff booking with buffer times.
- **Core engineering problem:** Constraint-based appointment grid with staff skills and buffer rules.
- **Architecture:** Modular monolith; booking engine; staff calendar; reminders
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, client PII, no-show policy
- **Key advanced concepts:** Multi-resource booking, buffers, no-shows
- **Why it is industrial:** Multi-resource scheduling with business-rule buffers

## JAVA-085 — Parking Operations & Enforcement Back-Office

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Parking / Mobility
- **Business problem:** Parking enforcement needs evidence-grade violations, payments and appeals.
- **Core engineering problem:** Violation intake with photo evidence, fine lifecycle and appeal workflow.
- **Architecture:** Modular monolith; violation workflow; payment; appeal engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (violation events)
- **Security architecture:** RBAC, evidence immutability, appeal rights
- **Key advanced concepts:** Evidence chains, fine lifecycle, appeals
- **Why it is industrial:** Evidence-grade enforcement with appeal integrity

## JAVA-086 — Mortgage Workflow & Document Checklist Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Mortgage / Lending
- **Business problem:** Mortgage origination requires document checklists, conditions and compliance tracking.
- **Core engineering problem:** Loan-file workflow with condition tracking and document completeness scoring.
- **Architecture:** Modular monolith; loan workflow; condition engine; document vault
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (condition events)
- **Security architecture:** RBAC, borrower PII encryption, e-sign audit
- **Key advanced concepts:** Condition tracking, doc scoring, e-signatures
- **Why it is industrial:** Regulator-grade loan files with condition automation

## JAVA-087 — Land & Property Records, Mutation & Deed Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** GovTech / Land Administration
- **Business problem:** Land records need mutation workflows with legal checks, objections and immutable history.
- **Core engineering problem:** Parcel registry with mutation state machines, objection windows and audit.
- **Architecture:** Modular monolith; parcel registry; mutation workflow; objection intake
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (mutation events)
- **Security architecture:** RBAC, digital sign-off, public read scopes
- **Key advanced concepts:** Mutation state machines, objection windows, history
- **Why it is industrial:** Legal-grade mutation processing with objection windows

## JAVA-088 — Lease Abstraction & Obligation Extraction Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** LegalTech / Real Estate
- **Business problem:** Lease abstraction from documents is error-prone manual work; obligations get missed.
- **Core engineering problem:** ML-assisted clause extraction with human review and obligation scheduling.
- **Architecture:** Modular monolith; extraction pipeline; review UI; obligation scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache PDFBox
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (extraction jobs)
- **Security architecture:** RBAC, clause-level permissions, review audit
- **Key advanced concepts:** Extraction + human-in-the-loop, obligation sync
- **Why it is industrial:** Human-in-the-loop extraction feeding obligation calendars

## JAVA-089 — Museum Collection & Conservation Manager

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Cultural Heritage
- **Business problem:** Museum collections need condition tracking, loans, conservation and provenance.
- **Core engineering problem:** Collection management with condition reporting, loan workflows and provenance.
- **Architecture:** Modular monolith; collection registry; loan workflow; conservation tasks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (loan events)
- **Security architecture:** RBAC, provenance immutability, insurance records
- **Key advanced concepts:** Condition history, loans, provenance chain
- **Why it is industrial:** Provenance-grade records with conservation scheduling

## JAVA-090 — Library Consortium Circulation & Resource Sharing

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Library Consortium
- **Business problem:** Consortium circulation must manage shared catalogs, holds and inter-library loans fairly.
- **Core engineering problem:** Shared catalog with hold routing, ILL fulfillment and lending policies.
- **Architecture:** Modular monolith; union catalog; hold engine; ILL workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (circulation events)
- **Security architecture:** RBAC, patron privacy, lending-policy engine
- **Key advanced concepts:** Union catalog, hold fairness, ILL routing
- **Why it is industrial:** Consortium-scale circulation with policy-driven lending

## JAVA-091 — Arena, Stadium & Event Access Operations

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Events / Venues
- **Business problem:** Stadium access must scan tens of thousands of tickets with anti-fraud and capacity control.
- **Core engineering problem:** High-throughput ticket validation with rotation codes and capacity gates.
- **Architecture:** Modular monolith; ticket service; gate validator; capacity counter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, virtual threads
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (access events)
- **Security architecture:** Rotating ticket codes, replay prevention, zone RBAC
- **Key advanced concepts:** Rotating codes, capacity gates, replay detection
- **Why it is industrial:** High-throughput validation with anti-replay and capacity

## JAVA-092 — Talent Agency Rostering & Booking Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Entertainment / Talent
- **Business problem:** Agencies must roster talent across bookings with availability, conflicts and commissions.
- **Core engineering problem:** Talent rostering with conflict detection, options and commission accounting.
- **Architecture:** Modular monolith; roster engine; booking workflow; commission ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (booking events)
- **Security architecture:** RBAC, talent PII, commission audit
- **Key advanced concepts:** Conflict detection, options, commission math
- **Why it is industrial:** Agency-grade rostering with commission integrity

## JAVA-093 — Music Rights & Royalty Statement Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Music / Rights
- **Business problem:** Royalty statements must compute usage to rights to payout with defensible math.
- **Core engineering problem:** Usage ingestion to rights matching and royalty statement generation.
- **Architecture:** Modular monolith; usage pipeline; rights store; statement engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (usage events)
- **Security architecture:** RBAC, statement confidentiality, audit
- **Key advanced concepts:** Rights matching, statement math, disputes
- **Why it is industrial:** Defensible royalty math with statement-level audit

## JAVA-094 — Research Grant Lifecycle & Funding Governance

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Research / Grants
- **Business problem:** Grant funds must be governed from call to closeout with compliance and reporting.
- **Core engineering problem:** Grant lifecycle with budgets, milestones, reviews and compliance reporting.
- **Architecture:** Modular monolith; grant workflow; budget engine; review boards
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (grant events)
- **Security architecture:** RBAC, reviewer conflict checks, four-eyes
- **Key advanced concepts:** Budget governance, milestones, reviewer conflicts
- **Why it is industrial:** Compliance-grade grant governance with conflict screening

## JAVA-095 — R&D Idea Pipeline & Stage-Gate Management

- **Difficulty:** Advanced (Tier 1)
- **Industry:** R&D / Innovation
- **Business problem:** Ideas stall without stage-gate rigor; portfolio decisions lack evidence.
- **Core engineering problem:** Idea pipeline with stage gates, evidence requirements and portfolio scoring.
- **Architecture:** Modular monolith; pipeline workflow; gate reviews; portfolio views
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, IP confidentiality, audit
- **Key advanced concepts:** Stage gates, evidence gates, portfolio scoring
- **Why it is industrial:** Gate-driven innovation portfolio with evidence rigor

## JAVA-096 — Patent & Invention Disclosure Docketing

- **Difficulty:** Advanced (Tier 1)
- **Industry:** LegalTech / IP
- **Business problem:** Patent docketing must never miss a deadline; PTO-style deadlines are jurisdiction-specific.
- **Core engineering problem:** Docket engine with jurisdiction deadline rules and docketing workflows.
- **Architecture:** Modular monolith; docket calendar; deadline rules; family records
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (deadline events)
- **Security architecture:** RBAC, invention confidentiality, audit
- **Key advanced concepts:** Deadline computation, patent families, alerts
- **Why it is industrial:** Deadline-critical docketing with jurisdiction rules

## JAVA-097 — Laboratory Scheduling & Consumables Tracker

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Lab Operations
- **Business problem:** Shared lab instruments and consumables are overbooked and untracked.
- **Core engineering problem:** Lab resource booking with consumable inventory and safety approvals.
- **Architecture:** Modular monolith; booking engine; inventory; safety workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, safety training gates, audit
- **Key advanced concepts:** Resource booking, inventory thresholds, training gates
- **Why it is industrial:** Instrument booking gated on training and inventory

## JAVA-098 — Veterinary Practice Management Platform

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Veterinary
- **Business problem:** Vet practices need appointments, medical records, pharmacy and billing integrated.
- **Core engineering problem:** Vet practice management with records, prescriptions and treatment plans.
- **Architecture:** Modular monolith; appointment engine; medical records; pharmacy stock
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (appointment events)
- **Security architecture:** RBAC, owner-data protection, prescription controls
- **Key advanced concepts:** Treatment plans, controlled-drug tracking, billing
- **Why it is industrial:** Practice-grade records with controlled-substance controls

## JAVA-099 — Farm Input, Yield & Traceability Ledger

- **Difficulty:** Advanced (Tier 1)
- **Industry:** AgriTech
- **Business problem:** Farm inputs and yields must be traced for certification and buyer trust.
- **Core engineering problem:** Field-level input/yield ledger with certification exports and anomaly flags.
- **Architecture:** Modular monolith; field registry; input ledger; certification engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (field events)
- **Security architecture:** RBAC, cooperative scoping, immutable entries
- **Key advanced concepts:** Input-output ledger, certification exports
- **Why it is industrial:** Certification-grade traceability from field to buyer

## JAVA-100 — Call Center Quality Assurance & Scoring Engine

- **Difficulty:** Advanced (Tier 1)
- **Industry:** Contact Center QA
- **Business problem:** Call QA samples must be scored fairly with calibration and coachable insights.
- **Core engineering problem:** Sampling with scoring rubrics, calibration sessions and trend analytics.
- **Architecture:** Modular monolith; sampling engine; rubric scoring; calibration workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (QA events)
- **Security architecture:** RBAC, call privacy, calibration audit
- **Key advanced concepts:** Stratified sampling, rubric calibration, trends
- **Why it is industrial:** Calibrated QA scoring with agent-level analytics

## JAVA-101 — B2B Credit Application & Underwriting Workbench

- **Difficulty:** Expert (Tier 2)
- **Industry:** B2B Credit / Underwriting
- **Business problem:** Credit applications need financial spreading, scoring and approval chains.
- **Core engineering problem:** Application workflow with financial spreading, scoring and limit decisions.
- **Architecture:** Modular monolith; underwriting workflow; spreading engine; scoring
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (application events)
- **Security architecture:** RBAC, four-eyes limits, PII encryption
- **Key advanced concepts:** Financial spreading, scorecards, limit gates
- **Why it is industrial:** Underwriting workbench with spreading and limit governance

## JAVA-102 — Accounts Payable Invoice Automation

- **Difficulty:** Expert (Tier 2)
- **Industry:** Finance / AP
- **Business problem:** Invoices arrive as PDFs/emails; AP teams key data manually with error and fraud risk.
- **Core engineering problem:** OCR-assisted invoice capture with PO matching and fraud checks.
- **Architecture:** Modular monolith; capture pipeline; matching engine; approval flow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache PDFBox
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (capture jobs)
- **Security architecture:** RBAC, four-eyes, duplicate/fraud checks
- **Key advanced concepts:** Capture + matching, duplicate detection, approvals
- **Why it is industrial:** Capture-to-pay automation with fraud-aware matching

## JAVA-103 — Treasury Cash Forecasting & Positioning

- **Difficulty:** Expert (Tier 2)
- **Industry:** Treasury / Finance
- **Business problem:** Cash positions are stale; treasurers need rolling forecasts and what-if scenarios.
- **Core engineering problem:** Cash forecasting with bank feeds, variance tracking and scenario modeling.
- **Architecture:** Modular monolith; forecast engine; bank feed adapter; scenarios
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (feed events)
- **Security architecture:** RBAC, bank-data encryption, audit
- **Key advanced concepts:** Forecast rollups, variance, scenarios
- **Why it is industrial:** Treasury-grade forecasting with bank-feed reconciliation

## JAVA-104 — Enterprise Spend Analytics & Category Intelligence

- **Difficulty:** Expert (Tier 2)
- **Industry:** Procurement Analytics
- **Business problem:** Spend is fragmented across systems; category managers cannot see tail spend or compliance.
- **Core engineering problem:** Spend cube with normalization, classification and policy-compliance scoring.
- **Architecture:** Modular monolith; spend pipeline; classification; OLAP-style rollups
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** —
- **Security architecture:** RBAC, category scopes, data masking
- **Key advanced concepts:** Spend cubes, normalization, compliance scores
- **Why it is industrial:** Spend intelligence with normalization and compliance KPIs

## JAVA-105 — Travel & Expense Pre-Trip Compliance Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Travel / Finance
- **Business problem:** Pre-trip approvals prevent spend rather than post-trip policing.
- **Core engineering problem:** Pre-trip policy engine with budget checks and approval routing.
- **Architecture:** Modular monolith; policy engine; trip workflow; budget checks
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (trip events)
- **Security architecture:** RBAC, approval limits, traveler privacy
- **Key advanced concepts:** Policy pre-checks, budgets, approvals
- **Why it is industrial:** Policy enforcement BEFORE spend with budget coupling

## JAVA-106 — Statutory Audit Sampling & Evidence Workbench

- **Difficulty:** Expert (Tier 2)
- **Industry:** Audit / Finance
- **Business problem:** Statutory audits need sampling, evidence collection and working papers.
- **Core engineering problem:** Statistical sampling with evidence gathering and working-paper exports.
- **Architecture:** Modular monolith; sampling engine; evidence vault; paper exports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** —
- **Security architecture:** RBAC, evidence immutability, auditor scopes
- **Key advanced concepts:** Sampling methods, evidence chains, exports
- **Why it is industrial:** Audit-grade sampling with tamper-evident evidence

## JAVA-107 — Subsidiary Consolidation & Intercompany Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Finance / Consolidation
- **Business problem:** Subsidiary books must consolidate with intercompany elimination and currency translation.
- **Core engineering problem:** Consolidation engine with elimination, translation and ownership math.
- **Architecture:** Modular monolith; consolidation batch; intercompany matching; reports
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (close events)
- **Security architecture:** RBAC, four-eyes close, audit
- **Key advanced concepts:** Eliminations, FX translation, ownership tiers
- **Why it is industrial:** Group-close math with elimination and translation rules

## JAVA-108 — Fixed Asset Tagging & Physical Audit Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Finance / Fixed Assets
- **Business problem:** Physical asset audits (tagging, locations, existence) must reconcile with the ledger.
- **Core engineering problem:** Mobile-assisted tagging with reconciliation and discrepancy workflows.
- **Architecture:** Modular monolith; tagging API; reconciliation; discrepancy cases
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (scan events)
- **Security architecture:** RBAC, scan integrity, discrepancy four-eyes
- **Key advanced concepts:** Scan-based audits, reconciliation, discrepancies
- **Why it is industrial:** Physical-vs-ledger reconciliation with discrepancy governance

## JAVA-109 — Robotic Process Automation Control Tower

- **Difficulty:** Expert (Tier 2)
- **Industry:** Enterprise Automation / RPA
- **Business problem:** Bot fleets run unattended; failures, credentials and audit need central control.
- **Core engineering problem:** RPA control tower with bot scheduling, credential vault and run audit.
- **Architecture:** Modular monolith; bot registry; scheduler; credential vault; run log
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (bot events)
- **Security architecture:** RBAC, credential vault with rotation, run audit
- **Key advanced concepts:** Bot scheduling, secrets, run forensics
- **Why it is industrial:** Unattended automation with credential governance

## JAVA-110 — Complaint Resolution & Regulatory Response Tracker

- **Difficulty:** Expert (Tier 2)
- **Industry:** Regulatory / Complaints
- **Business problem:** Regulatory complaints carry strict response deadlines and evidence obligations.
- **Core engineering problem:** Complaint intake with deadline tracking, evidence and regulator-grade responses.
- **Architecture:** Modular monolith; complaint workflow; deadline engine; evidence vault
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (complaint events)
- **Security architecture:** RBAC, PII redaction, deadline audit
- **Key advanced concepts:** Regulatory deadlines, evidence, responses
- **Why it is industrial:** Regulator-grade complaint handling with deadline proof
