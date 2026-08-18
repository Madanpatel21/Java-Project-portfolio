# Banking / FinTech / Insurance — Catalog

82 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-111 — Core Banking Ledger & Posting Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Core Systems
- **Business problem:** A bank's books must post millions of entries a day with exact balance integrity; a single lost posting is a regulatory event.
- **Core engineering problem:** Double-entry ledger with idempotent posting, balance invariant checks and EOD close.
- **Architecture:** Modular monolith; posting engine; balance store; EOD batch; CQRS read side
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, virtual threads, Lombok-free records
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (posting events, EOD signals)
- **Security architecture:** mTLS between services, RBAC, dual control on overrides, hash-chained journal entries
- **Key advanced concepts:** Double-entry invariants, idempotency keys, optimistic locking, EOD close
- **Why it is industrial:** Bank-grade ledger integrity with tamper-evident journal

## JAVA-112 — Real-Time Payment Processing Hub

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Payments
- **Business problem:** Instant payments demand sub-second authorization with fraud checks and non-repudiation.
- **Core engineering problem:** High-throughput payment pipeline with velocity checks, sanctions screening and settlement.
- **Architecture:** Modular monolith; payment pipeline; risk gates; settlement journal
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, virtual threads, Resilience4j
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (payment events, DLQ)
- **Security architecture:** mTLS, HSM-style signing sim, JWS payloads, replay protection, audit
- **Key advanced concepts:** Pipeline gates, velocity limits, exactly-once settlement
- **Why it is industrial:** Instant-payment semantics with layered risk gates

## JAVA-113 — International Wire Transfer Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Payments
- **Business problem:** Cross-border wires need STP rates, charges, intermediaries and compliance checks per corridor.
- **Core engineering problem:** Wire lifecycle with corridor routing, charges computation and AML screening.
- **Architecture:** Modular monolith; wire state machine; corridor engine; screening hook
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (wire events, screening results)
- **Security architecture:** RBAC, four-eyes release, mTLS to gateway sim
- **Key advanced concepts:** Corridor routing, charges tables, state machines
- **Why it is industrial:** Wire transfer state machines with corridor-aware charges

## JAVA-114 — ACH Batch Processing & Returns Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Payments
- **Business problem:** ACH files arrive in batches with returns, NOCs and settlement windows that must be processed correctly.
- **Core engineering problem:** Batch file parsing, return reason handling, risk scoring of returns.
- **Architecture:** Modular monolith; batch pipeline; returns workflow; settlement calc
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (file events)
- **Security architecture:** RBAC, file hash verification, dual control
- **Key advanced concepts:** NACHA-style parsing, return codes, windows
- **Why it is industrial:** Deterministic batch processing with return-reason state machines

## JAVA-115 — Card Transaction Authorization Switch

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Cards
- **Business problem:** Authorization decisions must run in milliseconds with velocity, limits and stand-in processing.
- **Core engineering problem:** Low-latency auth switch with rule engine and stand-in mode when host is down.
- **Architecture:** Modular monolith; auth engine; rule cache; stand-in module
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine, virtual threads
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (auth events)
- **Security architecture:** mTLS, PIN verification sim, token validation, replay detection
- **Key advanced concepts:** Hot rule cache, stand-in mode, latency budgets
- **Why it is industrial:** Millisecond auth with degraded-mode stand-in processing

## JAVA-116 — Card Scheme Clearing & Settlement Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Cards
- **Business problem:** Card clearing files (presentments, chargebacks, fees) must net and settle across schemes correctly.
- **Core engineering problem:** Scheme file processing with interchange computation and settlement positions.
- **Architecture:** Modular monolith; clearing pipeline; interchange engine; settlement ledger
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (clearing events)
- **Security architecture:** RBAC, four-eyes settlement, file integrity hashes
- **Key advanced concepts:** Interchange math, netting, dispute lifecycle
- **Why it is industrial:** Scheme-grade clearing with interchange accuracy

## JAVA-117 — Card Issuance & Token Vault Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Cards
- **Business problem:** Card PANs must be tokenized everywhere; token lifecycle must follow card lifecycle.
- **Core engineering problem:** Token vault with PAN encryption, lifecycle events and detokenization controls.
- **Architecture:** Modular monolith; token service; PAN vault; lifecycle events
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, AES-GCM via JCA
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (lifecycle events)
- **Security architecture:** AES-GCM PAN vault, key rotation sim, mTLS, strict detok audit
- **Key advanced concepts:** Tokenization, key rotation, PCI-style scope control
- **Why it is industrial:** PCI-style token vault with key rotation and detok audit

## JAVA-118 — Mobile Wallet & Money Movement Platform

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Wallet
- **Business problem:** A wallet must move money between accounts with ledger accuracy, limits and KYC tiers.
- **Core engineering problem:** Wallet ledger with tiered limits, transaction limits and settlement to bank rails.
- **Architecture:** Modular monolith; wallet ledger; limit engine; rail adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (wallet events)
- **Security architecture:** OIDC, device binding, step-up auth for high value, ledger immutability
- **Key advanced concepts:** Tiered limits, rail settlement, balance invariants
- **Why it is industrial:** Money-movement platform with tiered KYC limits

## JAVA-119 — Recurring Payments & Mandate Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Payments
- **Business problem:** Recurring payments need mandates, retry calendars and customer consent tracking.
- **Core engineering problem:** Mandate lifecycle with retry scheduling, bank-file generation and consent audit.
- **Architecture:** Modular monolith; mandate state machine; retry calendar; file generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (mandate events)
- **Security architecture:** OIDC, consent records, PCI-scope minimization
- **Key advanced concepts:** Mandates, retry calendars, bank files
- **Why it is industrial:** Consent-audited recurring payment processing

## JAVA-120 — Payment Reconciliation & Exceptions Workbench

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Operations
- **Business problem:** Payment exceptions pile up; ops teams need a workbench with root-cause queues and resolution SLAs.
- **Core engineering problem:** Exception queueing with enrichment, resolution workflows and root-cause analytics.
- **Architecture:** Modular monolith; exception pipeline; enrichment; resolution workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (exception events)
- **Security architecture:** RBAC, dual control on manual fixes, audit
- **Key advanced concepts:** Enrichment, root-cause clustering, SLA timers
- **Why it is industrial:** Ops workbench with root-cause clustering and SLA management

## JAVA-121 — Payment Dispute & Chargeback Management

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Disputes
- **Business problem:** Chargebacks run on scheme deadlines; evidence must be assembled and submitted on time.
- **Core engineering problem:** Dispute lifecycle with deadline calendars, evidence vault and scheme file outputs.
- **Architecture:** Modular monolith; dispute state machine; deadline engine; evidence vault
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (dispute events)
- **Security architecture:** RBAC, evidence immutability, scheme deadline audit
- **Key advanced concepts:** Deadline calendars, evidence assembly, representments
- **Why it is industrial:** Scheme-deadline-driven dispute processing with evidence chains

## JAVA-122 — Bank Statement & Notification Generator

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Statements
- **Business problem:** Statements and notifications must be generated correctly per account type and delivered securely.
- **Core engineering problem:** Statement generation pipeline with templates, delivery and secure notification routing.
- **Architecture:** Modular monolith; statement engine; template store; notification router
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (statement events)
- **Security architecture:** RBAC, encrypted delivery, no-secret-in-logs rules
- **Key advanced concepts:** Template rendering, batch generation, delivery audit
- **Why it is industrial:** High-volume statement generation with secure delivery

## JAVA-123 — Digital Lending Origination Platform

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Lending
- **Business problem:** Loan origination must orchestrate KYC, credit checks, pricing and offer generation.
- **Core engineering problem:** Origination workflow with third-party orchestration, pricing and e-sign documents.
- **Architecture:** Modular monolith; origination workflow; third-party adapters; offer engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (application events)
- **Security architecture:** OIDC, PII encryption, e-sign audit, vendor scoping
- **Key advanced concepts:** Orchestration, vendor adapters, pricing waterfall
- **Why it is industrial:** Loan origination with resilient third-party orchestration

## JAVA-124 — Loan Servicing & Delinquency Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Lending
- **Business problem:** Servicing loans means payment allocation, delinquency tracking and collections handoff.
- **Core engineering problem:** Payment allocation waterfall, delinquency aging and workout workflows.
- **Architecture:** Modular monolith; servicing engine; allocation waterfall; delinquency jobs
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (payment events)
- **Security architecture:** RBAC, four-eyes on manual adjustments, PII masking
- **Key advanced concepts:** Allocation waterfalls, aging, workout states
- **Why it is industrial:** Servicing-grade allocation and delinquency management

## JAVA-125 — Amortization, Interest Accrual & Fee Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Lending
- **Business problem:** Interest accrual, fees and amortization must be exact to the cent across schedules.
- **Core engineering problem:** Accrual engine with multiple day-count conventions, fees and payment schedules.
- **Architecture:** Modular monolith; accrual engine; schedule generator; fee rules
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (accrual events)
- **Security architecture:** RBAC, immutable schedule versions, audit
- **Key advanced concepts:** Day-count conventions, fee rules, schedule versions
- **Why it is industrial:** Cent-exact accrual math with versioned schedules

## JAVA-126 — Credit Decisioning & Policy Rules Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Credit Risk
- **Business problem:** Credit decisions must be fast, consistent and explainable per policy version.
- **Core engineering problem:** Policy rules engine with scorecards, versioned policies and decision explainability.
- **Architecture:** Modular monolith; decision service; policy store; scorecard engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (decision events)
- **Security architecture:** RBAC, policy version binding, decision audit
- **Key advanced concepts:** Scorecards, policy versions, explainability
- **Why it is industrial:** Versioned credit policy with explainable decisions

## JAVA-127 — Risk-Based Pricing & Limit Management

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Credit Risk
- **Business problem:** Limits must be risk-based: exposure, utilization, behavior and collateral feed limit engines.
- **Core engineering problem:** Limit computation with exposure aggregation and behavioral scoring.
- **Architecture:** Modular monolith; limit engine; exposure aggregator; behavioral models
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (exposure events)
- **Security architecture:** RBAC, limit-override dual control, audit
- **Key advanced concepts:** Exposure aggregation, limit recomputation, overrides
- **Why it is industrial:** Risk-based limits with exposure-aware recomputation

## JAVA-128 — Collections & Recovery Workflow Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Collections
- **Business problem:** Collections needs strategy-driven contact plans with agent workflows and promise tracking.
- **Core engineering problem:** Strategy-driven collections campaigns with promises-to-pay and agent queues.
- **Architecture:** Modular monolith; strategy engine; contact scheduler; promise ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (collection events)
- **Security architecture:** RBAC, call recording privacy, promise audit
- **Key advanced concepts:** Strategy rules, contact plans, promise tracking
- **Why it is industrial:** Strategy-driven collections with promise-to-pay tracking

## JAVA-129 — Debt Restructuring & Forbearance Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Lending
- **Business problem:** Distressed borrowers need restructuring plans with NPV comparisons and approval governance.
- **Core engineering problem:** Restructuring workflows with NPV comparison, approval chains and re-aging rules.
- **Architecture:** Modular monolith; restructuring workflow; NPV engine; approval chain
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (plan events)
- **Security architecture:** RBAC, four-eyes, hardship data protection
- **Key advanced concepts:** NPV comparison, re-aging, approval gates
- **Why it is industrial:** Governed restructuring with NPV-justified decisions

## JAVA-130 — Credit Bureau Reporting & Dispute Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Credit Reporting
- **Business problem:** Bureau reporting must be timely, accurate and support consumer dispute corrections.
- **Core engineering problem:** Bureau file generation (Metro2-style) with dispute intake and correction workflows.
- **Architecture:** Modular monolith; reporting engine; file generator; dispute workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (report events)
- **Security architecture:** RBAC, consumer PII controls, correction audit
- **Key advanced concepts:** Metro2-style files, disputes, corrections
- **Why it is industrial:** Bureau-grade reporting with dispute-correction loops

## JAVA-131 — Anti-Money-Laundering Transaction Monitoring

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / AML
- **Business problem:** Transaction monitoring must detect money laundering patterns with explainable alerts and low false positives.
- **Core engineering problem:** Scenario engine over transaction streams with segmentation, thresholds and alert review.
- **Architecture:** Modular monolith; scenario engine; stream processor; alert workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (transaction streams)
- **Security architecture:** RBAC, alert confidentiality, four-eyes disposition
- **Key advanced concepts:** Scenarios, segmentation, threshold tuning, explainability
- **Why it is industrial:** AML scenario engine with tunable, explainable alerts

## JAVA-132 — KYC Onboarding & Identity Verification Orchestrator

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / KYC
- **Business problem:** Onboarding must orchestrate document capture, verification vendors and risk assessment.
- **Core engineering problem:** KYC orchestration with vendor adapters, document checks and risk tiering.
- **Architecture:** Modular monolith; onboarding workflow; vendor adapters; risk tiering
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (KYC events)
- **Security architecture:** OIDC, PII encryption, vendor isolation, audit
- **Key advanced concepts:** Vendor orchestration, doc validation, tiering
- **Why it is industrial:** KYC orchestration with resilient vendor integration

## JAVA-133 — Customer Screening Against Watchlists

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Sanctions
- **Business problem:** Names must be screened against watchlists with fuzzy matching and false-positive reduction.
- **Core engineering problem:** High-volume fuzzy screening with scoring, tuning and case management.
- **Architecture:** Modular monolith; screening service; fuzzy matcher; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (screening requests)
- **Security architecture:** RBAC, hit confidentiality, audit
- **Key advanced concepts:** Fuzzy matching, hit scoring, tunable thresholds
- **Why it is industrial:** Sanctions screening with fuzzy-hit triage at volume

## JAVA-134 — Sanctions & Regulatory Watchlist Update Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Compliance
- **Business problem:** Watchlist data updates must be ingested, versioned and propagated without downtime.
- **Core engineering problem:** Watchlist ingestion with versioning, delta propagation and audit.
- **Architecture:** Modular monolith; ingestion pipeline; version store; distribution
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** RabbitMQ (list updates)
- **Security architecture:** RBAC, list integrity hashes, propagation audit
- **Key advanced concepts:** Delta ingestion, versioned lists, propagation
- **Why it is industrial:** Versioned watchlist propagation with integrity proof

## JAVA-135 — Fraud Detection & Scoring Decision Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Fraud
- **Business problem:** Fraud decisions must run at transaction time with model scores, rules and device signals.
- **Core engineering problem:** Real-time scoring decision service with rules, model invocation and feedback loop.
- **Architecture:** Modular monolith; decision service; feature store; feedback loop
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (scoring events)
- **Security architecture:** RBAC, model governance, decision audit
- **Key advanced concepts:** Real-time scoring, feature caching, feedback
- **Why it is industrial:** Transaction-time fraud scoring with model governance

## JAVA-136 — Fraud Case Management & SAR Filing Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / AML
- **Business problem:** Suspicious activity must be documented, reviewed and filed as regulatory reports (SAR-style).
- **Core engineering problem:** Case management with evidence assembly, filing workflow and regulator formats.
- **Architecture:** Modular monolith; case workflow; evidence vault; filing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (case events)
- **Security architecture:** RBAC, tip-off confidentiality, four-eyes filing
- **Key advanced concepts:** Case assembly, regulator formats, deadlines
- **Why it is industrial:** SAR-style case management with filing deadlines

## JAVA-137 — Insurance Underwriting Decision Platform

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Underwriting
- **Business problem:** Underwriters need data-driven risk decisions with rules, models and referral logic.
- **Core engineering problem:** Underwriting decision platform with rules, models and referral routing.
- **Architecture:** Modular monolith; decision engine; rules store; referral workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** RabbitMQ (decision events)
- **Security architecture:** RBAC, decision audit, policy versioning
- **Key advanced concepts:** Rules + models, referral logic, explainability
- **Why it is industrial:** Underwriting decisions with referral governance

## JAVA-138 — Insurance Claims Intake & Straight-Through Processing

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Claims
- **Business problem:** Claims must intake, validate and auto-adjudicate where possible with fraud checks.
- **Core engineering problem:** Claims intake with straight-through processing rules, fraud flags and exception routing.
- **Architecture:** Modular monolith; intake pipeline; STP rules; exception queue
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (claim events)
- **Security architecture:** RBAC, claimant PII, fraud flags, audit
- **Key advanced concepts:** STP rules, fraud scoring, exception routing
- **Why it is industrial:** Claims STP with fraud-aware exception routing

## JAVA-139 — Catastrophe Exposure & Reinsurance Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Catastrophe
- **Business problem:** Cat events require exposure aggregation, reinsurance recovery calculation and scenario stress.
- **Core engineering problem:** Exposure aggregation with reinsurance structures and cat scenario simulation.
- **Architecture:** Modular monolith; exposure engine; treaty structures; scenario simulator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (cat events)
- **Security architecture:** RBAC, treaty confidentiality, audit
- **Key advanced concepts:** Aggregation, treaties, scenario stress
- **Why it is industrial:** Cat exposure math with reinsurance recovery modeling

## JAVA-140 — Policy Renewal, Lapse & Grace Period Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Policy
- **Business problem:** Policies must renew, lapse and reinstate with grace-period and billing synchronization.
- **Core engineering problem:** Policy lifecycle state machine with renewal, lapse and reinstatement rules.
- **Architecture:** Modular monolith; lifecycle engine; billing sync; renewal jobs
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (lifecycle events)
- **Security architecture:** RBAC, four-eyes reinstatement, audit
- **Key advanced concepts:** Lifecycle state machines, grace rules, sync
- **Why it is industrial:** Policy lifecycle with billing-synced grace periods

## JAVA-141 — Actuarial Valuation & Reserve Calculation Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Actuarial
- **Business problem:** Reserve calculations must be reproducible, versioned and auditable by actuaries.
- **Core engineering problem:** Actuarial valuation engine with method libraries, versioning and audit.
- **Architecture:** Modular monolith; valuation engine; method library; run registry
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (run events)
- **Security architecture:** RBAC, run immutability, actuary sign-off
- **Key advanced concepts:** Valuation methods, reproducible runs, sign-off
- **Why it is industrial:** Reproducible actuarial runs with sign-off governance

## JAVA-142 — Motor Claims: Accident Triage & FNOL

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Motor Claims
- **Business problem:** Accident claims need rapid triage, repair estimation and liability assessment.
- **Core engineering problem:** FNOL intake with triage scoring, repair estimates and liability workflow.
- **Architecture:** Modular monolith; FNOL workflow; triage engine; repair estimation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (FNOL events)
- **Security architecture:** RBAC, photo evidence, fraud flags, audit
- **Key advanced concepts:** Triage scoring, repair estimates, liability
- **Why it is industrial:** FNOL triage with evidence and liability assessment

## JAVA-143 — Health Claims Adjudication & Pricing Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Health Claims
- **Business problem:** Health claims must adjudicate against policy, coding and provider contracts.
- **Core engineering problem:** Adjudication engine with benefit rules, coding validation and contract pricing.
- **Architecture:** Modular monolith; adjudication pipeline; benefit rules; pricing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (claim events)
- **Security architecture:** RBAC, PHI protection, coding audit
- **Key advanced concepts:** Benefit rules, coding checks, contract pricing
- **Why it is industrial:** Adjudication with coding-aware pricing integrity

## JAVA-144 — Premium Billing, Collections & Dunning Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Billing
- **Business problem:** Premiums must bill, collect and dunning-manage across products and payment methods.
- **Core engineering problem:** Premium billing engine with installment plans, collections and dunning.
- **Architecture:** Modular monolith; billing engine; installment plans; dunning workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (billing events)
- **Security architecture:** RBAC, PCI-scope control, audit
- **Key advanced concepts:** Installments, dunning states, allocations
- **Why it is industrial:** Premium billing with dunning and allocation rules

## JAVA-145 — Wealth Portfolio Rebalancing & Drift Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Wealth / Advisory
- **Business problem:** Portfolios drift from targets; rebalancing needs tax-aware, cost-aware trade proposals.
- **Core engineering problem:** Drift detection with tax-aware rebalancing proposals and approval workflows.
- **Architecture:** Modular monolith; drift engine; rebalancing optimizer; approval workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (rebalance events)
- **Security architecture:** RBAC, advisor-client scoping, trade approval
- **Key advanced concepts:** Drift computation, tax-aware proposals, approvals
- **Why it is industrial:** Tax-aware rebalancing with governed trade proposals

## JAVA-146 — Trading Risk Limits & Pre-Trade Checks

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Risk
- **Business problem:** Pre-trade checks must enforce risk limits in real time across desks and asset classes.
- **Core engineering problem:** Limit engine with real-time utilization, breach alerts and kill-switch.
- **Architecture:** Modular monolith; limit service; utilization aggregator; breach pipeline
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (trade events)
- **Security architecture:** RBAC, kill-switch dual control, audit
- **Key advanced concepts:** Limit trees, utilization, breach workflows
- **Why it is industrial:** Real-time limit enforcement with breach kill-switch

## JAVA-147 — Order Management & Smart Order Routing

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Trading
- **Business problem:** Orders must route smartly across simulated venues with best-execution logic.
- **Core engineering problem:** OMS with smart order routing, venue simulation and execution analytics.
- **Architecture:** Modular monolith; order service; router; venue simulators
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (order events)
- **Security architecture:** RBAC, desk scoping, order audit
- **Key advanced concepts:** Smart routing, venue sims, execution quality
- **Why it is industrial:** Best-execution routing against simulated venues

## JAVA-148 — Market Data Tick Storage & Distribution

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Market Data
- **Business problem:** Tick data must be stored, compressed and served to consumers at low latency.
- **Core engineering problem:** Tick storage with compression, replay and subscription distribution.
- **Architecture:** Modular monolith; tick pipeline; compressed store; replay service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Chronicle-style queues
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (ticks)
- **Security architecture:** RBAC, market-data entitlements, audit
- **Key advanced concepts:** Tick compression, replay, subscriptions
- **Why it is industrial:** Market-data distribution with entitlement and replay

## JAVA-149 — Algo Backtesting & Paper Trading Lab

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Quant
- **Business problem:** Strategies must be backtested on historical data with realistic fills, costs and stats.
- **Core engineering problem:** Backtesting lab with event-driven simulation, cost models and stat reports.
- **Architecture:** Modular monolith; backtest engine; data feed; stat library
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (backtest jobs)
- **Security architecture:** RBAC, strategy IP isolation, audit
- **Key advanced concepts:** Event-driven simulation, fill models, stats
- **Why it is industrial:** Realistic backtesting with cost-aware fill simulation

## JAVA-150 — P&L Attribution & Risk Analytics Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Risk
- **Business problem:** P&L must be attributed to market moves, positions and new trades with drill-down.
- **Core engineering problem:** P&L attribution engine with risk factor decomposition and drill-down APIs.
- **Architecture:** Modular monolith; attribution engine; risk store; drill-down API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (attribution jobs)
- **Security architecture:** RBAC, desk scoping, audit
- **Key advanced concepts:** Attribution decomposition, factor sensitivities
- **Why it is industrial:** Attribution-grade P&L decomposition with drill-down

## JAVA-151 — Derivative Lifecycle & Margin Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Derivatives
- **Business problem:** Derivative contracts need lifecycle events, valuations and margin computations.
- **Core engineering problem:** Derivative lifecycle with ISDA-style events, valuation and margin engines.
- **Architecture:** Modular monolith; contract state machines; valuation engine; margin calc
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (lifecycle events)
- **Security architecture:** RBAC, four-eyes on lifecycle events, audit
- **Key advanced concepts:** Lifecycle events, valuation curves, margin math
- **Why it is industrial:** ISDA-style lifecycle processing with margin integrity

## JAVA-152 — Corporate Actions Processing Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Operations
- **Business problem:** Corporate actions (dividends, splits, mergers) must be captured, elected and processed.
- **Core engineering problem:** Corporate action capture with entitlement computation and election workflows.
- **Architecture:** Modular monolith; CA workflow; entitlement engine; election service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (CA events)
- **Security architecture:** RBAC, election deadlines, four-eyes
- **Key advanced concepts:** Entitlements, elections, deadline calendars
- **Why it is industrial:** Corporate-action processing with election governance

## JAVA-153 — Settlement & Custody Reconciliation Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Settlement
- **Business problem:** Settlement and custody positions must reconcile against counterparties and depositories daily.
- **Core engineering problem:** Multi-source reconciliation with matching rules, breaks and resolution workflows.
- **Architecture:** Modular monolith; reconciliation engine; break queues; resolution workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (break events)
- **Security architecture:** RBAC, break resolution dual control, audit
- **Key advanced concepts:** Matching rules, break aging, resolution SLAs
- **Why it is industrial:** Custody-grade reconciliation with break management

## JAVA-154 — Regulatory Reporting Data Pipeline (BCBS-style)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Regulatory
- **Business problem:** Regulators require standardized risk and capital reports on fixed calendars.
- **Core engineering problem:** BCBS-style report generation with data lineage, validation and submission packages.
- **Architecture:** Modular monolith; report pipeline; validation rules; submission workflow
- **Java technology stack:** Spring Boot 3, Spring Batch, Spring Data JPA, Flyway, Apache POI
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (report events)
- **Security architecture:** RBAC, report sign-off, data lineage audit
- **Key advanced concepts:** Report lineage, validation, submission gates
- **Why it is industrial:** Regulatory reporting with lineage and sign-off

## JAVA-155 — Transaction Cost Analysis (TCA) Platform

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Analytics
- **Business problem:** Trading costs must be measured against benchmarks and decomposed by cause.
- **Core engineering problem:** TCA platform with benchmark comparison and cost decomposition.
- **Architecture:** Modular monolith; TCA engine; benchmark store; report builder
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (TCA jobs)
- **Security architecture:** RBAC, desk scoping, audit
- **Key advanced concepts:** Benchmarks, cost decomposition, slippage
- **Why it is industrial:** TCA-grade cost decomposition with benchmarks

## JAVA-156 — Market Abuse & Insider Trading Surveillance

- **Difficulty:** Expert (Tier 2)
- **Industry:** Capital Markets / Surveillance
- **Business problem:** Market abuse patterns (spoofing, front-running) must be detected across orders and quotes.
- **Core engineering problem:** Surveillance engine with pattern detection over order/trade streams.
- **Architecture:** Modular monolith; pattern engine; stream processor; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (order/trade streams)
- **Security architecture:** RBAC, investigation confidentiality, audit
- **Key advanced concepts:** Pattern detection, stream windows, cases
- **Why it is industrial:** Surveillance-grade pattern detection over market streams

## JAVA-157 — Digital Onboarding & eKYC Orchestrator

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Onboarding
- **Business problem:** Digital account opening must orchestrate document capture, liveness checks, bureau pulls and risk tiering in minutes.
- **Core engineering problem:** Fully digital eKYC orchestration with vendor adapters, retries and compliance evidence.
- **Architecture:** Modular monolith; onboarding saga; vendor adapters; evidence store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Resilience4j
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (onboarding events)
- **Security architecture:** OIDC, liveness-check integration, PII encryption, vendor isolation
- **Key advanced concepts:** Saga orchestration, vendor retries, risk tiering
- **Why it is industrial:** End-to-end digital onboarding with vendor resilience

## JAVA-158 — Kiosk & Agent Banking Operations Hub

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Branch Tech
- **Business problem:** Agent networks and kiosks need offline-capable operations with sync, limits and anti-fraud controls.
- **Core engineering problem:** Agent banking with offline queue, sync, float management and fraud controls.
- **Architecture:** Modular monolith; agent service; offline sync; float ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (sync events)
- **Security architecture:** Device binding, agent limits, biometric auth sim
- **Key advanced concepts:** Offline queue, float management, conflict resolution
- **Why it is industrial:** Agent banking with offline resilience and float controls

## JAVA-159 — SME Lending & Credit Guarantee Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / SME Lending
- **Business problem:** SMEs need credit where banks fear risk; guarantee schemes bridge the gap with claim workflows.
- **Core engineering problem:** SME lending with government guarantee rules, claim workflows and portfolio caps.
- **Architecture:** Modular monolith; origination workflow; guarantee engine; claim service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (guarantee events)
- **Security architecture:** RBAC, four-eyes approvals, guarantee audit
- **Key advanced concepts:** Guarantee coverage math, claims, portfolio caps
- **Why it is industrial:** Guarantee-scheme lending with claim-grade audit

## JAVA-160 — Microfinance Group Lending & Collections

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Microfinance
- **Business problem:** Group lending needs joint liability, meeting cycles and field collection tracking.
- **Core engineering problem:** Group loan lifecycle with joint liability tracking and field collection sync.
- **Architecture:** Modular monolith; group ledger; meeting scheduler; field sync
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (collection events)
- **Security architecture:** RBAC, field-officer scoping, offline auth
- **Key advanced concepts:** Joint liability, meeting cycles, offline sync
- **Why it is industrial:** Group-lending operations with liability tracking

## JAVA-161 — Peer-to-Peer Lending Marketplace

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Marketplace Lending
- **Business problem:** A P2P marketplace must match lenders to borrowers with credit tiers, portfolios and fair allocation.
- **Core engineering problem:** Marketplace matching with credit tiers, auto-invest rules and secondary transfers.
- **Architecture:** Modular monolith; matching engine; auto-invest; secondary market
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (loan events)
- **Security architecture:** OIDC, lender accreditation rules, audit
- **Key advanced concepts:** Matching rules, auto-invest, secondary transfers
- **Why it is industrial:** Marketplace mechanics with allocation fairness

## JAVA-162 — Invoice Financing & Factoring Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Working Capital
- **Business problem:** Invoice financing needs verification, risk scoring and collections on the underlying receivables.
- **Core engineering problem:** Invoice verification, advance computation and receivable tracking.
- **Architecture:** Modular monolith; invoice workflow; advance engine; collections
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (invoice events)
- **Security architecture:** RBAC, invoice veracity checks, dual control
- **Key advanced concepts:** Advance rates, verification, receivable aging
- **Why it is industrial:** Factoring-grade receivable management with fraud checks

## JAVA-163 — Supply Chain Finance & Dynamic Discounting

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Supply Chain Finance
- **Business problem:** Suppliers want early payment; buyers want extended terms; both need the program's economics managed.
- **Core engineering problem:** Dynamic discounting with program rules, approval flows and settlement.
- **Architecture:** Modular monolith; program engine; discounting calc; settlement
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (funding events)
- **Security architecture:** RBAC, dual party consent, audit
- **Key advanced concepts:** Dynamic discounting math, program limits, settlement
- **Why it is industrial:** SCF program economics with dual-party consent

## JAVA-164 — Card Loyalty Points & Rewards Ledger

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Loyalty
- **Business problem:** Card rewards must ledger points exactly; liability reporting is a balance-sheet item.
- **Core engineering problem:** Double-entry points ledger with earn/burn rules, expiry and liability reports.
- **Architecture:** Modular monolith; points ledger; rules engine; expiry sweeps
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (point events)
- **Security architecture:** OIDC, ledger immutability, fraud rules
- **Key advanced concepts:** Double-entry ledger, expiry, liability rollups
- **Why it is industrial:** Liability-grade points accounting with expiry rules

## JAVA-165 — Cashback Rules & Offer Settlement Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Offers
- **Business problem:** Cashback programs must attribute purchases, apply rules and settle with merchants exactly.
- **Core engineering problem:** Cashback rule engine with attribution, caps and merchant settlement.
- **Architecture:** Modular monolith; rules engine; attribution; settlement files
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (purchase events)
- **Security architecture:** RBAC, merchant scoping, four-eyes settlement
- **Key advanced concepts:** Attribution windows, caps, settlement
- **Why it is industrial:** Cashback attribution with merchant settlement integrity

## JAVA-166 — Gift, Prepaid & Stored-Value Ledger

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Stored Value
- **Business problem:** Prepaid and stored-value products need issuance, redemption and float management with full audit.
- **Core engineering problem:** Stored-value ledger with issuance batches, redemption and float reporting.
- **Architecture:** Modular monolith; card ledger; batch issuance; redemption engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (redemption events)
- **Security architecture:** Tokenized cards, PIN handling, ledger immutability
- **Key advanced concepts:** Batch issuance, float management, redemption
- **Why it is industrial:** Stored-value float management with batch issuance

## JAVA-167 — Digital Gold & Commodity Accumulation Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Commodities
- **Business problem:** Digital gold accumulation must track grams, market prices and delivery options.
- **Core engineering problem:** Metal accumulation ledger with price ingestion, storage fees and delivery workflow.
- **Architecture:** Modular monolith; accumulation ledger; price feed; delivery workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (price updates)
- **Security architecture:** RBAC, vault audit, delivery verification
- **Key advanced concepts:** Gram ledger, storage fees, delivery SLAs
- **Why it is industrial:** Commodity-grade accumulation with vault reconciliation

## JAVA-168 — Remittance Corridor & FX Routing Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Remittances
- **Business problem:** Remittance corridors need FX routing, partner fees and delivery confirmation tracking.
- **Core engineering problem:** Corridor routing with FX windows, partner selection and delivery states.
- **Architecture:** Modular monolith; routing engine; FX window; partner adapters
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (transfer events)
- **Security architecture:** RBAC, partner isolation, compliance checks
- **Key advanced concepts:** Corridor routing, FX windows, delivery states
- **Why it is industrial:** Corridor-optimized remittance routing with partner SLAs

## JAVA-169 — Currency Exchange & Rate Management Desk

- **Difficulty:** Expert (Tier 2)
- **Industry:** Treasury / FX
- **Business problem:** FX desks need rate management with spreads, volatility margins and audit of every quote.
- **Core engineering problem:** Rate management desk with spread rules, volatility margins and quote audit.
- **Architecture:** Modular monolith; rate engine; spread rules; quote log
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (rate events)
- **Security architecture:** RBAC, desk scoping, quote immutability
- **Key advanced concepts:** Spread rules, volatility margins, quote audit
- **Why it is industrial:** Desk-grade rate management with quote-level audit

## JAVA-170 — Treasury Front Office Position & Hedge Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Treasury / Front Office
- **Business problem:** Treasurers need positions, exposures and hedge recommendations across currencies and maturities.
- **Core engineering problem:** Position engine with exposure aggregation, hedge proposals and what-if scenarios.
- **Architecture:** Modular monolith; position engine; exposure aggregator; hedge advisor
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (position events)
- **Security architecture:** RBAC, hedge approval chains, audit
- **Key advanced concepts:** Exposure aggregation, hedge proposals, scenarios
- **Why it is industrial:** Treasury hedging with exposure-aware recommendations

## JAVA-171 — Cash Pooling & Notional Netting Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Treasury / Cash Management
- **Business problem:** Group treasuries pool cash across entities; netting must respect legal, tax and currency constraints.
- **Core engineering problem:** Cash pooling with notional netting, interest allocation and entity ledger.
- **Architecture:** Modular monolith; pooling engine; netting calc; interest allocation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (pooling events)
- **Security architecture:** RBAC, entity scoping, four-eyes
- **Key advanced concepts:** Notional netting, interest allocation, FX legs
- **Why it is industrial:** Cash-pooling math with legal-entity awareness

## JAVA-172 — Interbank Messaging Gateway (SWIFT-style)

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Messaging
- **Business problem:** Interbank messages (SWIFT-style) need parsing, validation, routing and archival with MT/MX semantics.
- **Core engineering problem:** Message gateway with MT/MX-style parsing, validation, routing and archive.
- **Architecture:** Modular monolith; message pipeline; validator; routing table
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (message events)
- **Security architecture:** mTLS, message integrity checks, archive immutability
- **Key advanced concepts:** MT/MX-style parsing, validation, routing
- **Why it is industrial:** Interbank-grade message processing with archival

## JAVA-173 — ATM Monitoring & Cash Logistics Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / ATM Operations
- **Business problem:** ATM networks need cash forecasting, replenishment scheduling and incident monitoring.
- **Core engineering problem:** Cash forecasting per ATM with replenishment routes and incident alerts.
- **Architecture:** Modular monolith; forecasting; replenishment planner; incident feed
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (ATM events)
- **Security architecture:** RBAC, secure key loading sim, audit
- **Key advanced concepts:** Cash forecasting, replenishment routes, incidents
- **Why it is industrial:** ATM fleet operations with cash logistics planning

## JAVA-174 — Branch Teller Capture & Proof System

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Branch Tech
- **Business problem:** Branch capture must proof checks and cash against teller transactions at close.
- **Core engineering problem:** Teller capture with proof-of-cash, over/short reporting and dual control.
- **Architecture:** Modular monolith; teller service; proof engine; close workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (teller events)
- **Security architecture:** RBAC, dual control, over/short limits
- **Key advanced concepts:** Proof-of-cash, over/short, close workflows
- **Why it is industrial:** Branch proof-of-cash with dual-control closes

## JAVA-175 — Card Fraud Rules Sandbox & Simulation

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Fraud
- **Business problem:** Fraud teams need a sandbox to test rules against production-like data without touching production.
- **Core engineering problem:** Fraud rules sandbox with replay, backtesting and promotion workflow.
- **Architecture:** Modular monolith; rules sandbox; replay engine; promotion gates
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (replay streams)
- **Security architecture:** RBAC, sandbox isolation, promotion audit
- **Key advanced concepts:** Rule replay, backtesting, promotion
- **Why it is industrial:** Rule-change governance with production-safe promotion

## JAVA-176 — Banking API Gateway & PSD2 Open Banking

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Open Banking
- **Business problem:** PSD2-style open banking needs consent-managed APIs, TPP registration and rate controls.
- **Core engineering problem:** API gateway with TPP onboarding, consent checks and per-TPP rate limits.
- **Architecture:** Modular monolith; gateway service; TPP registry; consent check
- **Java technology stack:** Spring Boot 3, Spring Security, Spring WebFlux, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (API events)
- **Security architecture:** mTLS + OAuth client credentials, consent enforcement, rate limits
- **Key advanced concepts:** TPP onboarding, consent enforcement, rate limiting
- **Why it is industrial:** Open-banking-grade API gateway with consent enforcement

## JAVA-177 — Consent & Data Sharing Permission Ledger

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Privacy
- **Business problem:** Customers must control which third parties see what data and for how long.
- **Core engineering problem:** Consent ledger with purpose-bound permissions, expiry and revocation.
- **Architecture:** Modular monolith; consent service; purpose registry; revocation engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (consent events)
- **Security architecture:** OIDC, consent immutability, revocation propagation
- **Key advanced concepts:** Purpose-bound consent, expiry, revocation
- **Why it is industrial:** Privacy-grade consent management with revocation propagation

## JAVA-178 — Salary-On-Demand & Earned Wage Access

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Payroll
- **Business problem:** Employees need wages before payday without employer cash-flow disruption.
- **Core engineering problem:** Earned-wage access with accrual computation, advances and payroll netting.
- **Architecture:** Modular monolith; accrual engine; advance ledger; payroll reconciliation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (payroll events)
- **Security architecture:** RBAC, employer/employee scoping, payroll netting
- **Key advanced concepts:** Accrual math, advances, payroll netting
- **Why it is industrial:** Earned-wage access with payroll-grade reconciliation

## JAVA-179 — Insurance Agent Commission & Hierarchy Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Distribution
- **Business problem:** Agent commissions must compute across hierarchies, products and overrides correctly.
- **Core engineering problem:** Hierarchy-aware commission engine with overrides, clawbacks and statements.
- **Architecture:** Modular monolith; hierarchy engine; commission calc; statement service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (commission events)
- **Security architecture:** RBAC, hierarchy scoping, statement confidentiality
- **Key advanced concepts:** Hierarchy rollups, overrides, clawbacks
- **Why it is industrial:** Hierarchy-grade commission math with clawback rules

## JAVA-180 — Policy Administration & Product Factory

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Policy
- **Business problem:** Insurers need product factories: configurable products, rating plans and document generation.
- **Core engineering problem:** Product factory with rating plans, rules and policy document assembly.
- **Architecture:** Modular monolith; product engine; rating plans; document assembly
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, OpenPDF
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (policy events)
- **Security architecture:** RBAC, product versioning, audit
- **Key advanced concepts:** Product factory, rating plans, doc assembly
- **Why it is industrial:** Product-factory architecture with versioned rating plans

## JAVA-181 — Insurance Document & Clause Repository

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Documents
- **Business problem:** Policy documents need clause versioning, endorsement management and tamper-proof storage.
- **Core engineering problem:** Clause repository with versioning, endorsements and hash-verified documents.
- **Architecture:** Modular monolith; clause store; endorsement engine; doc vault
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (doc events)
- **Security architecture:** RBAC, document hashing, version integrity
- **Key advanced concepts:** Clause versioning, endorsements, hash chains
- **Why it is industrial:** Tamper-evident document governance for insurance

## JAVA-182 — Reinsurance Treaty & Cession Calculation

- **Difficulty:** Expert (Tier 2)
- **Industry:** Insurance / Reinsurance
- **Business problem:** Cessions must compute across treaties (quota share, excess of loss) with recovery tracking.
- **Core engineering problem:** Treaty engine with cession calculation, recoveries and bordereau reports.
- **Architecture:** Modular monolith; treaty store; cession engine; bordereau generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (cession events)
- **Security architecture:** RBAC, treaty confidentiality, four-eyes
- **Key advanced concepts:** Quota share, XoL layers, recoveries
- **Why it is industrial:** Reinsurance-grade cession math with bordereau output

## JAVA-183 — Financial Crime Graph & Link Analysis

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Financial Crime
- **Business problem:** Money-laundering networks span entities; link analysis must surface hidden relationships.
- **Core engineering problem:** Graph-based link analysis over parties, accounts and transactions.
- **Architecture:** Modular monolith; graph store; traversal engine; visualization API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, JGraphT
- **Data layer:** PostgreSQL 16 (+graph ext), Redis 7
- **Messaging:** RabbitMQ (graph updates)
- **Security architecture:** RBAC, investigation confidentiality, audit
- **Key advanced concepts:** Graph traversals, centrality, ring detection
- **Why it is industrial:** Financial-crime graph analytics with investigation tools

## JAVA-184 — Behavioral Biometrics for Session Risk

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Fraud
- **Business problem:** Session risk must be scored continuously from behavior signals, not just at login.
- **Core engineering problem:** Behavioral biometrics pipeline scoring session risk in near-real-time.
- **Architecture:** Modular monolith; signal ingestion; scoring engine; risk actions
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (behavior signals)
- **Security architecture:** OIDC, session binding, step-up triggers
- **Key advanced concepts:** Behavior scoring, step-up actions, feedback
- **Why it is industrial:** Continuous session-risk scoring with step-up enforcement

## JAVA-185 — Document Forgery & Tamper Detection Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Document Security
- **Business problem:** Forged documents must be detected via metadata, hashes and visual tamper signatures.
- **Core engineering problem:** Document tamper detection with metadata forensics and reference matching.
- **Architecture:** Modular monolith; document pipeline; forensics engine; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache PDFBox
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** RabbitMQ (doc events)
- **Security architecture:** RBAC, evidence immutability, case audit
- **Key advanced concepts:** Metadata forensics, hash checks, visual signatures
- **Why it is industrial:** Forensic-grade document verification for onboarding

## JAVA-186 — Beneficial Ownership & Structure Resolution

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / KYC
- **Business problem:** Corporate structures must be resolved to beneficial owners for AML compliance.
- **Core engineering problem:** Entity resolution across ownership chains with UBO computation.
- **Architecture:** Modular monolith; structure registry; ownership graph; UBO engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (structure events)
- **Security architecture:** RBAC, structure confidentiality, audit
- **Key advanced concepts:** Ownership chains, UBO computation, thresholds
- **Why it is industrial:** Beneficial-ownership resolution with threshold math

## JAVA-187 — Robo-Advisory Goal & Risk Profiling Engine

- **Difficulty:** Expert (Tier 2)
- **Industry:** Wealth / Robo-Advisory
- **Business problem:** Robo-advisors need goal modeling, risk profiling and portfolio proposals with suitability checks.
- **Core engineering problem:** Goal engine with risk profiling, suitability rules and proposal generation.
- **Architecture:** Modular monolith; goal engine; risk profiler; proposal service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (proposal events)
- **Security architecture:** OIDC, suitability compliance, audit
- **Key advanced concepts:** Risk profiling, suitability, proposals
- **Why it is industrial:** Suitability-compliant advisory with audit trails

## JAVA-188 — Investment Performance & GIPS Reporting

- **Difficulty:** Expert (Tier 2)
- **Industry:** Wealth / Reporting
- **Business problem:** Investment performance must be reported with GIPS-style standards and attribution.
- **Core engineering problem:** Performance engine with time-weighted returns, attribution and composite reports.
- **Architecture:** Modular monolith; performance engine; attribution; report builder
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (report jobs)
- **Security architecture:** RBAC, client scoping, report immutability
- **Key advanced concepts:** TWR computation, attribution, composites
- **Why it is industrial:** GIPS-style performance reporting with attribution

## JAVA-189 — Risk Aggregation & Limit Monitoring Hub

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Risk
- **Business problem:** Risk limits must aggregate across desks, products and entities in near-real-time.
- **Core engineering problem:** Limit aggregation hub with hierarchies, utilization and breach workflows.
- **Architecture:** Modular monolith; limit store; aggregation engine; breach service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Caffeine
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (exposure events)
- **Security architecture:** RBAC, breach escalation, audit
- **Key advanced concepts:** Limit hierarchies, aggregation, breaches
- **Why it is industrial:** Bank-wide limit aggregation with breach escalation

## JAVA-190 — Fraud Network Scoring & Ring Detection

- **Difficulty:** Expert (Tier 2)
- **Industry:** Banking / Fraud
- **Business problem:** Fraud rings operate across accounts; network scoring must detect collusion patterns.
- **Core engineering problem:** Graph-based ring detection with community scoring and case generation.
- **Architecture:** Modular monolith; graph engine; community detection; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (transaction streams)
- **Security architecture:** RBAC, investigation confidentiality, audit
- **Key advanced concepts:** Community detection, ring scoring, cases
- **Why it is industrial:** Fraud-ring detection with graph community analysis

## JAVA-191 — Escrow & Multi-Party Settlement Service

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Payments
- **Business problem:** Marketplace transactions need escrow with milestone releases, disputes and interest handling.
- **Core engineering problem:** Multi-party escrow with milestone conditions, dispute workflows and release rules.
- **Architecture:** Modular monolith; escrow ledger; milestone engine; dispute workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (escrow events)
- **Security architecture:** OIDC, party consent, release dual control
- **Key advanced concepts:** Milestone escrow, release rules, disputes
- **Why it is industrial:** Escrow-grade fund custody with milestone releases

## JAVA-192 — Tax Withholding & Reporting Calculator

- **Difficulty:** Expert (Tier 2)
- **Industry:** FinTech / Tax
- **Business problem:** Payments must withhold taxes per jurisdiction and report on schedules.
- **Core engineering problem:** Withholding engine with jurisdiction rules, certificates and reporting.
- **Architecture:** Modular monolith; withholding rules; certificate store; report generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** RabbitMQ (tax events)
- **Security architecture:** RBAC, tax-data encryption, report audit
- **Key advanced concepts:** Jurisdiction rules, certificates, reporting
- **Why it is industrial:** Tax-grade withholding with jurisdiction-aware rules
