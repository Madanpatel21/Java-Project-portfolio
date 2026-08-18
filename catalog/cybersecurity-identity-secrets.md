# Cybersecurity / Identity / Secrets — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-396 — Enterprise Identity & Access Administration

- **Difficulty:** Architect (Tier 3)
- **Industry:** Cybersecurity / IAM
- **Business problem:** Enterprise identity must be administered with lifecycle, groups and attribute sync.
- **Core engineering problem:** Identity administration with lifecycle workflows, group policy and sync.
- **Architecture:** Modular monolith; identity store; lifecycle workflow; sync engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (identity events)
- **Security architecture:** OIDC, Argon2id, admin RBAC, audit
- **Key advanced concepts:** Lifecycle workflows, group policy, sync
- **Why it is industrial:** IAM-grade administration with lifecycle governance

## JAVA-397 — Identity Governance & Access Certification

- **Difficulty:** Architect (Tier 3)
- **Industry:** Cybersecurity / IGA
- **Business problem:** Access rights must be certified periodically with reviewer campaigns and remediation.
- **Core engineering problem:** Access certification campaigns with review workflows, revocations and reporting.
- **Architecture:** Modular monolith; campaign engine; review workflow; remediation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (cert events)
- **Security architecture:** RBAC, reviewer delegation, audit
- **Key advanced concepts:** Campaigns, reviews, revocations
- **Why it is industrial:** IGA-grade certification with remediation loops

## JAVA-398 — Privileged Access Management Vault

- **Difficulty:** Architect (Tier 3)
- **Industry:** Cybersecurity / PAM
- **Business problem:** Privileged credentials must be vaulted, checked out and rotated with full session audit.
- **Core engineering problem:** PAM vault with checkout workflows, session recording sim and rotation.
- **Architecture:** Modular monolith; vault service; checkout workflow; rotation engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (PAM events)
- **Security architecture:** AES-GCM secrets, break-glass, session audit
- **Key advanced concepts:** Checkout, rotation, break-glass, session audit
- **Why it is industrial:** PAM-grade vault with break-glass and rotation

## JAVA-399 — Secrets Management & Rotation Service

- **Difficulty:** Architect (Tier 3)
- **Industry:** Cybersecurity / Secrets
- **Business problem:** Application secrets must be managed with rotation, versions and least-privilege access.
- **Core engineering problem:** Secrets service with versioning, rotation schedules and access policies.
- **Architecture:** Modular monolith; secrets store; rotation scheduler; access policy
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (secret events)
- **Security architecture:** mTLS, AES-GCM, access policies, audit
- **Key advanced concepts:** Versioning, rotation, access policies
- **Why it is industrial:** Secrets-grade management with rotation automation

## JAVA-400 — mTLS Certificate Lifecycle & Auto-Renewal

- **Difficulty:** Architect (Tier 3)
- **Industry:** Cybersecurity / PKI
- **Business problem:** Certificates must be issued, renewed and revoked with mTLS enforcement.
- **Core engineering problem:** Certificate lifecycle with ACME-style issuance, renewal bots and revocation.
- **Architecture:** Modular monolith; CA service; renewal bot; revocation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (cert events)
- **Security architecture:** mTLS, key protection, OCSP-style status
- **Key advanced concepts:** ACME-style issuance, renewal, revocation
- **Why it is industrial:** PKI-grade lifecycle with renewal automation

## JAVA-401 — Federated SSO & Session Boundary Broker

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / IAM
- **Business problem:** SSO sessions must be brokered across apps with session boundaries and logout propagation.
- **Core engineering problem:** Federated SSO with session registry, propagation and token exchange.
- **Architecture:** Modular monolith; SSO broker; session registry; propagation service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (session events)
- **Security architecture:** OIDC, PKCE, logout propagation, audit
- **Key advanced concepts:** Session boundaries, propagation, exchange
- **Why it is industrial:** SSO-grade brokering with session propagation

## JAVA-402 — OAuth Authorization Server & Token Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / IAM
- **Business problem:** An authorization server must issue tokens with consent, scopes and revocation.
- **Core engineering problem:** OAuth AS with JWT issuance, consent screens, scope validation and revocation.
- **Architecture:** Modular monolith; token service; consent service; revocation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (token events)
- **Security architecture:** JWT/JWS, PKCE, refresh rotation, audit
- **Key advanced concepts:** Token issuance, consent, revocation
- **Why it is industrial:** OAuth-grade authorization with refresh rotation

## JAVA-403 — Policy Decision Point (PDP) Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / ABAC
- **Business problem:** Centralized policy decisions must evaluate attributes consistently across apps.
- **Core engineering problem:** PDP service with policy evaluation, caching and decision audit.
- **Architecture:** Modular monolith; PDP engine; policy store; decision cache
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (decision events)
- **Security architecture:** ABAC policies, decision audit, mTLS
- **Key advanced concepts:** Policy evaluation, caching, explainability
- **Why it is industrial:** ABAC-grade decisions with explainability

## JAVA-404 — Zero Trust Session Risk Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Zero Trust
- **Business problem:** Session risk must be evaluated continuously with device, network and behavior signals.
- **Core engineering problem:** Zero-trust risk engine with signal fusion and step-up enforcement.
- **Architecture:** Modular monolith; signal ingestion; risk engine; enforcement API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (signal streams)
- **Security architecture:** OIDC, device trust, step-up actions
- **Key advanced concepts:** Signal fusion, risk scoring, step-up
- **Why it is industrial:** Zero-trust-grade risk with step-up enforcement

## JAVA-405 — Password Manager with Breach Check

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Secrets
- **Business problem:** A password manager must store, share and breach-check credentials safely.
- **Core engineering problem:** Password vault with zero-knowledge-style encryption, sharing and breach checks.
- **Architecture:** Modular monolith; vault service; sharing engine; breach checker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (vault events)
- **Security architecture:** Argon2id, AES-GCM, zero-knowledge design, audit
- **Key advanced concepts:** Zero-knowledge design, sharing, breach checks
- **Why it is industrial:** Vault-grade security with zero-knowledge design

## JAVA-406 — MFA & TOTP Enrollment Orchestrator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / MFA
- **Business problem:** MFA enrollment must orchestrate TOTP, backup codes and device trust.
- **Core engineering problem:** MFA orchestrator with TOTP validation, recovery and enrollment flows.
- **Architecture:** Modular monolith; MFA service; TOTP engine; recovery flows
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (MFA events)
- **Security architecture:** TOTP, backup codes, replay protection
- **Key advanced concepts:** TOTP validation, recovery, enrollment
- **Why it is industrial:** MFA-grade orchestration with recovery safety

## JAVA-407 — Credential Stuffing & Brute-Force Defense

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Auth Defense
- **Business problem:** Credential attacks must be blocked with progressive throttling and anomaly detection.
- **Core engineering problem:** Auth defense with throttling, lockouts and credential-stuffing detection.
- **Architecture:** Modular monolith; defense engine; throttle service; anomaly detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (auth events)
- **Security architecture:** Lockouts, throttling, breach-password checks
- **Key advanced concepts:** Progressive throttling, stuff detection
- **Why it is industrial:** Defense-grade auth protection with attack detection

## JAVA-408 — API Security Gateway & Schema Validator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / API Security
- **Business problem:** APIs must be validated against schemas with threat filtering at the gateway.
- **Core engineering problem:** API security gateway with schema validation, filtering and threat rules.
- **Architecture:** Modular monolith; gateway service; schema validator; threat engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring WebFlux, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (gateway events)
- **Security architecture:** API keys, mTLS, rate limits, audit
- **Key advanced concepts:** Schema validation, threat filtering, limits
- **Why it is industrial:** Gateway-grade API security with schema enforcement

## JAVA-409 — Web Application Firewall (WAF) Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / WAF
- **Business problem:** Web traffic must be filtered for OWASP attacks with virtual patching.
- **Core engineering problem:** WAF engine with rule packs, anomaly scoring and blocking.
- **Architecture:** Modular monolith; filter chain; rule engine; scoring service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring WebFlux, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (WAF events)
- **Security architecture:** Rule packs, IP reputation, audit
- **Key advanced concepts:** Rule packs, anomaly scoring, blocking
- **Why it is industrial:** WAF-grade filtering with virtual patching

## JAVA-410 — Rate-Limit & Traffic Shaping Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Traffic
- **Business problem:** Rate limits must be enforced per identity, IP and endpoint with fairness.
- **Core engineering problem:** Rate-limit service with token buckets, policies and distributed counters.
- **Architecture:** Modular monolith; limiter engine; policy store; counters
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (limit events)
- **Security architecture:** API keys, policy scoping, audit
- **Key advanced concepts:** Token buckets, policies, fairness
- **Why it is industrial:** Limiting-grade enforcement with distributed counters

## JAVA-411 — Bot Management & Human Verification

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Bot Defense
- **Business problem:** Bots must be distinguished from humans with challenge flows and reputation.
- **Core engineering problem:** Bot management with device fingerprinting, challenges and reputation scores.
- **Architecture:** Modular monolith; fingerprint service; challenge engine; reputation store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (bot events)
- **Security architecture:** Fingerprinting, challenges, IP reputation
- **Key advanced concepts:** Fingerprints, challenges, reputation
- **Why it is industrial:** Bot-defense-grade detection with challenge flows

## JAVA-412 — DDoS Anomaly Detector (netflow-style)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / DDoS
- **Business problem:** DDoS anomalies must be detected from flow data with baseline deviation.
- **Core engineering problem:** DDoS detector with flow baselines, deviation scoring and mitigation hooks.
- **Architecture:** Modular monolith; flow ingestion; baseline engine; mitigation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (flow data)
- **Security architecture:** RBAC, mitigation audit, scoping
- **Key advanced concepts:** Baselines, deviation, mitigation
- **Why it is industrial:** DDoS-grade detection with mitigation integration

## JAVA-413 — Intrusion Detection Log Analyzer

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / IDS
- **Business problem:** Intrusion logs must be analyzed for attack signatures and correlated alerts.
- **Core engineering problem:** IDS log analyzer with signature matching, correlation and alerting.
- **Architecture:** Modular monolith; log pipeline; signature engine; correlation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (log streams)
- **Security architecture:** RBAC, alert confidentiality, audit
- **Key advanced concepts:** Signature matching, correlation, alerts
- **Why it is industrial:** IDS-grade analysis with signature correlation

## JAVA-414 — Honeypot Deployment & Triage Console

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Deception
- **Business problem:** Honeypots must capture attacker behavior and triage threats safely.
- **Core engineering problem:** Honeypot deployment with interaction capture, analysis and triage.
- **Architecture:** Modular monolith; honeypot simulators; capture store; triage engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (honeypot events)
- **Security architecture:** Isolated sandboxes, analyst scoping, audit
- **Key advanced concepts:** Interaction capture, analysis, triage
- **Why it is industrial:** Deception-grade analysis with safe triage

## JAVA-415 — Threat Intelligence Feed Aggregator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Threat Intel
- **Business problem:** Threat feeds must be aggregated, deduplicated and scored for actionability.
- **Core engineering problem:** Threat-intel aggregator with feed ingestion, scoring and IOC search.
- **Architecture:** Modular monolith; feed pipeline; scoring engine; IOC store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (intel events)
- **Security architecture:** RBAC, intel confidentiality, audit
- **Key advanced concepts:** Feed ingestion, scoring, IOCs
- **Why it is industrial:** Intel-grade aggregation with IOC search

## JAVA-416 — Vulnerability Scanner Orchestrator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / VM
- **Business problem:** Vulnerability scanners must be orchestrated with scheduling and result dedup.
- **Core engineering problem:** Scanner orchestrator with scheduling, dedup and asset correlation.
- **Architecture:** Modular monolith; scan scheduler; result dedup; asset store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scan events)
- **Security architecture:** RBAC, scan credentials vault, audit
- **Key advanced concepts:** Scheduling, dedup, correlation
- **Why it is industrial:** VM-grade orchestration with result deduplication

## JAVA-417 — Vulnerability Lifecycle & SLA Tracker

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / VM
- **Business problem:** Vulnerabilities must be tracked with SLA-driven remediation workflows.
- **Core engineering problem:** Vuln lifecycle with risk scoring, SLAs and remediation tracking.
- **Architecture:** Modular monolith; vuln registry; risk engine; remediation workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (vuln events)
- **Security architecture:** RBAC, exception approvals, audit
- **Key advanced concepts:** Risk scoring, SLAs, exceptions
- **Why it is industrial:** Vuln-grade lifecycle with SLA enforcement

## JAVA-418 — SBOM Generator & License Compliance

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Supply Chain
- **Business problem:** SBOMs must be generated, compared and checked for license compliance.
- **Core engineering problem:** SBOM generator with dependency graph, license checks and diffing.
- **Architecture:** Modular monolith; SBOM engine; license store; diff service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (SBOM events)
- **Security architecture:** RBAC, pipeline integration, audit
- **Key advanced concepts:** SBOM generation, license checks, diffs
- **Why it is industrial:** Supply-chain-grade SBOM with license compliance

## JAVA-419 — Dependency Risk & CVE Mapper

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Supply Chain
- **Business problem:** Dependencies must be mapped to CVEs with reachability and exploitability scoring.
- **Core engineering problem:** Dependency-CVE mapper with version matching, scoring and alerts.
- **Architecture:** Modular monolith; dependency store; CVE ingestion; scoring engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (CVE events)
- **Security architecture:** RBAC, alert routing, audit
- **Key advanced concepts:** Version matching, scoring, alerts
- **Why it is industrial:** Dependency-grade risk mapping with CVE scoring

## JAVA-420 — Container Image Security Scanner

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Containers
- **Business problem:** Container images must be scanned for vulnerabilities and misconfigurations.
- **Core engineering problem:** Image scanner with layer analysis, policy gates and admission hooks.
- **Architecture:** Modular monolith; scan pipeline; policy engine; admission API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (scan events)
- **Security architecture:** RBAC, policy gates, audit
- **Key advanced concepts:** Layer analysis, policy gates, admission
- **Why it is industrial:** Container-grade scanning with admission control

## JAVA-421 — Runtime Application Self-Protection (RASP)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / RASP
- **Business problem:** Applications must protect themselves at runtime with inline threat detection.
- **Core engineering problem:** RASP agent patterns with instrumentation hooks and threat rules.
- **Architecture:** Modular monolith; agent API; threat rules; telemetry
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (RASP events)
- **Security architecture:** In-app policies, telemetry, audit
- **Key advanced concepts:** Inline detection, rules, telemetry
- **Why it is industrial:** RASP-grade protection with inline telemetry

## JAVA-422 — Security Chaos Engineering Harness

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Chaos
- **Business problem:** Security controls must be tested under failure with controlled experiments.
- **Core engineering problem:** Security chaos harness with attack simulations and control verification.
- **Architecture:** Modular monolith; experiment engine; attack simulators; verification
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (experiment events)
- **Security architecture:** Sandbox isolation, audit, approvals
- **Key advanced concepts:** Attack simulations, verification, blast-radius control
- **Why it is industrial:** Chaos-grade security testing with blast-radius control

## JAVA-423 — Phishing Simulation Campaign Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Awareness
- **Business problem:** Phishing campaigns must simulate, track and remediate risky behavior.
- **Core engineering problem:** Phishing simulation with campaign engine, click tracking and training.
- **Architecture:** Modular monolith; campaign engine; tracking service; training workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Mailpit (local)
- **Messaging:** Kafka (phishing events)
- **Security architecture:** Sandboxed links, PII protection, audit
- **Key advanced concepts:** Campaigns, click tracking, remediation
- **Why it is industrial:** Awareness-grade simulation with training loops

## JAVA-424 — Security Awareness Scoring Platform

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Awareness
- **Business problem:** Security awareness must be scored per employee with risk-based training plans.
- **Core engineering problem:** Awareness scoring with behavior signals and training assignments.
- **Architecture:** Modular monolith; scoring engine; training service; assignments
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (behavior events)
- **Security architecture:** RBAC, HR-data protection, audit
- **Key advanced concepts:** Behavior scoring, assignments, plans
- **Why it is industrial:** Awareness-grade scoring with training plans

## JAVA-425 — Insider Threat & UEBA Detector

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / UEBA
- **Business problem:** Insider threats must be detected from behavior anomalies with case escalation.
- **Core engineering problem:** UEBA detector with behavior baselines, anomaly scoring and cases.
- **Architecture:** Modular monolith; behavior pipeline; baseline engine; case workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext), OpenSearch 2
- **Messaging:** Kafka (activity streams)
- **Security architecture:** RBAC, privacy controls, case audit
- **Key advanced concepts:** Baselines, anomaly scoring, cases
- **Why it is industrial:** UEBA-grade detection with privacy controls

## JAVA-426 — Digital Forensics Evidence Manager

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Forensics
- **Business problem:** Forensic evidence must be collected with integrity hashes and chain-of-custody.
- **Core engineering problem:** Evidence manager with hash integrity, custody chains and case linkage.
- **Architecture:** Modular monolith; evidence store; hash service; custody ledger
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (evidence events)
- **Security architecture:** RBAC, hash integrity, custody audit
- **Key advanced concepts:** Integrity hashes, custody, cases
- **Why it is industrial:** Forensics-grade evidence with custody chains

## JAVA-427 — Incident Response Runbook Orchestrator

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / IR
- **Business problem:** Incident response must run coordinated runbooks with tasks and timelines.
- **Core engineering problem:** IR orchestration with runbooks, task assignments and evidence collection.
- **Architecture:** Modular monolith; runbook engine; task service; evidence links
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (IR events)
- **Security architecture:** RBAC, war-room scoping, audit
- **Key advanced concepts:** Runbooks, tasks, timelines
- **Why it is industrial:** IR-grade orchestration with runbook automation

## JAVA-428 — Threat Hunting Query Workbench

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Hunting
- **Business problem:** Threat hunters need query workbenches over security data with saved hunts.
- **Core engineering problem:** Hunting workbench with query builder, saved hunts and result triage.
- **Architecture:** Modular monolith; query engine; hunt store; triage workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (hunt events)
- **Security architecture:** RBAC, data scoping, audit
- **Key advanced concepts:** Query builder, saved hunts, triage
- **Why it is industrial:** Hunting-grade workbench with query governance

## JAVA-429 — Security Data Lake Ingestion Pipeline

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / SIEM
- **Business problem:** Security data lake must ingest logs with normalization, retention and search.
- **Core engineering problem:** Security data lake with ingestion pipelines, normalization and retention tiers.
- **Architecture:** Modular monolith; ingestion pipeline; normalizer; retention engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2, MinIO
- **Messaging:** Kafka (log streams)
- **Security architecture:** RBAC, tenant isolation, audit
- **Key advanced concepts:** Normalization, retention tiers, search
- **Why it is industrial:** SIEM-grade ingestion with retention tiers

## JAVA-430 — SIEM Correlation Rule Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / SIEM
- **Business problem:** Correlation rules must detect multi-event attack patterns in real time.
- **Core engineering problem:** Correlation engine with rule matching, windows and alert generation.
- **Architecture:** Modular monolith; rule engine; window service; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (event streams)
- **Security architecture:** RBAC, rule governance, audit
- **Key advanced concepts:** Rule matching, windows, alerts
- **Why it is industrial:** SIEM-grade correlation with rule governance

## JAVA-431 — SOAR Playbook Automation

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / SOAR
- **Business problem:** Playbooks must automate response with approvals for destructive actions.
- **Core engineering problem:** SOAR playbook engine with step automation, approvals and integrations.
- **Architecture:** Modular monolith; playbook engine; step runner; approval service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (playbook events)
- **Security architecture:** RBAC, destructive-action approvals, audit
- **Key advanced concepts:** Playbooks, approvals, integrations
- **Why it is industrial:** SOAR-grade automation with human gates

## JAVA-432 — Audit Log Immutability & Chain Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Audit
- **Business problem:** Audit logs must be immutable with hash chains and tamper detection.
- **Core engineering problem:** Audit chain service with hash chaining, verification and tamper alerts.
- **Architecture:** Modular monolith; audit store; hash chain; verification API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (audit events)
- **Security architecture:** RBAC, chain integrity, verification
- **Key advanced concepts:** Hash chains, verification, tamper alerts
- **Why it is industrial:** Audit-grade immutability with tamper detection

## JAVA-433 — Security Posture Scoring Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Posture
- **Business problem:** Security posture must be scored across assets with benchmarks and trends.
- **Core engineering problem:** Posture engine with benchmark checks, scoring and remediation tracking.
- **Architecture:** Modular monolith; check engine; scoring service; remediation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (posture events)
- **Security architecture:** RBAC, exception workflows, audit
- **Key advanced concepts:** Benchmarks, scoring, remediation
- **Why it is industrial:** Posture-grade scoring with benchmark checks

## JAVA-434 — Compliance Evidence Auto-Collector

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Compliance
- **Business problem:** Compliance evidence must be collected continuously from controls.
- **Core engineering problem:** Evidence auto-collector with control mappings, collectors and reports.
- **Architecture:** Modular monolith; control registry; collector framework; report engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (evidence events)
- **Security architecture:** RBAC, evidence integrity, audit
- **Key advanced concepts:** Control mappings, collectors, reports
- **Why it is industrial:** Compliance-grade evidence with control mapping

## JAVA-435 — Data Classification & DLP Scanner

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / DLP
- **Business problem:** Sensitive data must be classified and its movement controlled.
- **Core engineering problem:** DLP scanner with classifiers, policies and incident workflow.
- **Architecture:** Modular monolith; scan pipeline; classifier engine; incident workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (DLP events)
- **Security architecture:** RBAC, incident confidentiality, audit
- **Key advanced concepts:** Classifiers, policies, incidents
- **Why it is industrial:** DLP-grade classification with incident handling

## JAVA-436 — Field-Level Encryption Gateway

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Encryption
- **Business problem:** Field-level encryption must protect PII transparently across services.
- **Core engineering problem:** Encryption gateway with format-preserving options, key policies and audit.
- **Architecture:** Modular monolith; crypto service; key policy; audit log
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (crypto events)
- **Security architecture:** AES-GCM/FPE, key rotation, access audit
- **Key advanced concepts:** Field encryption, rotation, policies
- **Why it is industrial:** Encryption-grade gateway with key policy

## JAVA-437 — Data Masking & Tokenization Service

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Privacy
- **Business problem:** Data masking and tokenization must protect non-production and shared data.
- **Core engineering problem:** Masking/tokenization service with format preservation and reversibility controls.
- **Architecture:** Modular monolith; masking engine; token vault; policy store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (mask events)
- **Security architecture:** RBAC, token vault security, audit
- **Key advanced concepts:** Format preservation, reversibility, policies
- **Why it is industrial:** Privacy-grade masking with token vault

## JAVA-438 — Key Management Service (local HSM emulated)

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / KMS
- **Business problem:** Key management must emulate HSM behavior with key states and rotation.
- **Core engineering problem:** KMS with key lifecycle, usage policies and HSM-style API.
- **Architecture:** Modular monolith; key store; lifecycle engine; usage policy
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (key events)
- **Security architecture:** mTLS, key isolation, usage audit
- **Key advanced concepts:** Key lifecycle, usage policies, rotation
- **Why it is industrial:** KMS-grade key management with HSM-style API

## JAVA-439 — Signed Artifact & Release Verification

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Supply Chain
- **Business problem:** Release artifacts must be signed and verified before deployment.
- **Core engineering problem:** Signed-artifact verification with key trust, hashes and policies.
- **Architecture:** Modular monolith; signature service; trust store; verification API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (release events)
- **Security architecture:** Ed25519 signatures, trust chains, audit
- **Key advanced concepts:** Signing, verification, trust chains
- **Why it is industrial:** Supply-chain-grade signing with trust chains

## JAVA-440 — Supply Chain Attestation Ledger

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Supply Chain
- **Business problem:** Software supply chains must attest provenance with immutable records.
- **Core engineering problem:** Attestation ledger with build provenance, hashes and policy verification.
- **Architecture:** Modular monolith; attestation store; provenance pipeline; policy check
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (attestation events)
- **Security architecture:** RBAC, hash integrity, policy gates
- **Key advanced concepts:** Provenance, attestations, policies
- **Why it is industrial:** Attestation-grade provenance with policy gates

## JAVA-441 — Cloud-Native Policy-as-Code Auditor

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Cloud-Native
- **Business problem:** Cloud-native policies must be audited as code with drift detection.
- **Core engineering problem:** Policy-as-code auditor with rule evaluation and drift alerts.
- **Architecture:** Modular monolith; policy engine; drift detector; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (policy events)
- **Security architecture:** RBAC, rule governance, audit
- **Key advanced concepts:** Policy evaluation, drift, alerts
- **Why it is industrial:** Policy-grade auditing with drift detection

## JAVA-442 — Network Micro-Segmentation Policy Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Network
- **Business problem:** Micro-segmentation policies must be modeled and enforced with least privilege.
- **Core engineering problem:** Segmentation policy engine with flow analysis and rule generation.
- **Architecture:** Modular monolith; policy model; flow analyzer; rule generator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext)
- **Messaging:** Kafka (flow data)
- **Security architecture:** RBAC, least-privilege checks, audit
- **Key advanced concepts:** Flow analysis, least privilege, rules
- **Why it is industrial:** Segmentation-grade policy with least-privilege verification

## JAVA-443 — Zero-Day Exploit Simulation Sandbox

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Exploit Research
- **Business problem:** Zero-day exploits must be simulated in isolated sandboxes for validation.
- **Core engineering problem:** Exploit simulation sandbox with payload execution and observation.
- **Architecture:** Modular monolith; sandbox runner; payload store; observation engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (sandbox events)
- **Security architecture:** Isolation, analyst scoping, audit
- **Key advanced concepts:** Sandboxing, payloads, observation
- **Why it is industrial:** Exploit-grade sandboxing with isolation

## JAVA-444 — Biometric Liveness & Presentation-Attack Defense

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Biometrics
- **Business problem:** Biometric systems must detect presentation attacks with liveness checks.
- **Core engineering problem:** Liveness defense with challenge-response, signal checks and scoring.
- **Architecture:** Modular monolith; liveness engine; challenge service; scoring
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (biometric events)
- **Security architecture:** Biometric-data protection, spoof detection, audit
- **Key advanced concepts:** Challenge-response, liveness scoring
- **Why it is industrial:** Biometrics-grade liveness with spoof detection

## JAVA-445 — Secure Document Shredding & Retention Policy Engine

- **Difficulty:** Enterprise Platform (Tier 4)
- **Industry:** Cybersecurity / Data Governance
- **Business problem:** Documents must be shredded per retention policies with proof of destruction.
- **Core engineering problem:** Secure shredding with retention rules, destruction proofs and legal holds.
- **Architecture:** Modular monolith; retention engine; shredder service; hold registry
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Quartz
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (shred events)
- **Security architecture:** RBAC, destruction proof, legal hold
- **Key advanced concepts:** Retention rules, destruction, holds
- **Why it is industrial:** Governance-grade shredding with destruction proof
