# Data / AI Infrastructure — Catalog

50 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-596 — Data Ingestion & CDC Pipeline Orchestrator

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Ingestion
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** CDC pipelines must capture changes reliably with ordering, dedup and schema evolution.
- **Core engineering problem:** CDC orchestration with change capture, ordering guarantees and schema evolution handling.
- **Architecture:** Modular monolith; capture adapters; ordering engine; schema evolution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (change streams)
- **Security architecture:** RBAC, data-lineage audit, masking
- **Key advanced concepts:** CDC, ordering, dedup, schema evolution
- **Why it is industrial:** Platform-grade CDC with ordering guarantees

## JAVA-597 — Data Quality Rules & SLA Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Quality
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Data quality must be measured with rules, SLAs and anomaly detection.
- **Core engineering problem:** DQ rules engine with profiling, anomaly detection and SLA alerts.
- **Architecture:** Modular monolith; rules engine; profiler; anomaly detector
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (DQ events)
- **Security architecture:** RBAC, rule governance, audit
- **Key advanced concepts:** Profiling, rules, anomaly detection
- **Why it is industrial:** Quality-grade rules with profiling automation

## JAVA-598 — Metadata Catalog & Data Lineage

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Metadata
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Metadata must be cataloged with lineage, ownership and usage analytics.
- **Core engineering problem:** Metadata catalog with lineage graphs, ownership and usage metrics.
- **Architecture:** Modular monolith; catalog store; lineage engine; usage tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext), OpenSearch 2
- **Messaging:** Kafka (metadata events)
- **Security architecture:** RBAC, ABAC on metadata, audit
- **Key advanced concepts:** Lineage graphs, ownership, usage
- **Why it is industrial:** Catalog-grade metadata with lineage integrity

## JAVA-599 — Master Data Management Registry

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / MDM
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Master data must be governed with golden records, workflows and distribution.
- **Core engineering problem:** MDM registry with golden records, approval workflows and distribution.
- **Architecture:** Modular monolith; registry service; workflow engine; distribution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (MDM events)
- **Security architecture:** RBAC, field-level permissions, audit
- **Key advanced concepts:** Golden records, workflows, distribution
- **Why it is industrial:** MDM-grade governance with distribution

## JAVA-600 — Data Contract Enforcement Gateway

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Contracts
- **Business problem:** Data contracts must be enforced at producer/consumer boundaries.
- **Core engineering problem:** Contract gateway with schema validation, SLAs and versioning.
- **Architecture:** Modular monolith; contract store; validator; SLA monitor
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (contract events)
- **Security architecture:** RBAC, consumer auth, audit
- **Key advanced concepts:** Schema validation, SLAs, versions
- **Why it is industrial:** Contract-grade enforcement with SLA monitoring

## JAVA-601 — Feature Store & Training Data Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Features
- **Business problem:** Training data and features must be served consistently between offline and online.
- **Core engineering problem:** Feature store with offline/online serving, point-in-time correctness and backfills.
- **Architecture:** Modular monolith; feature registry; serving layer; backfill engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (feature events)
- **Security architecture:** RBAC, feature-level ACLs, audit
- **Key advanced concepts:** Point-in-time correctness, backfills, serving
- **Why it is industrial:** Feature-grade consistency between training and serving

## JAVA-602 — Model Registry & Version Governance

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Models
- **Business problem:** Models must be versioned, governed and promoted with approvals.
- **Core engineering problem:** Model registry with versions, metrics, approvals and deployment status.
- **Architecture:** Modular monolith; registry service; approval workflow; status engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (model events)
- **Security architecture:** RBAC, model governance, audit
- **Key advanced concepts:** Versioning, metrics, promotion gates
- **Why it is industrial:** Registry-grade governance with promotion gates

## JAVA-603 — ML Experiment Tracking & Comparison

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Experiments
- **Business problem:** Experiments must be tracked, compared and reproduced.
- **Core engineering problem:** Experiment tracking with run logging, metric comparison and reproducibility.
- **Architecture:** Modular monolith; run store; metric engine; compare service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (run events)
- **Security architecture:** RBAC, project scoping, audit
- **Key advanced concepts:** Run logging, comparisons, reproducibility
- **Why it is industrial:** Tracking-grade rigor with reproducibility

## JAVA-604 — Real-Time Model Serving & Drift Monitor

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Serving
- **Business problem:** Models must be served in real time with drift monitoring and shadow modes.
- **Core engineering problem:** Serving layer with model routing, drift detection and canary/shadow.
- **Architecture:** Modular monolith; serving engine; drift monitor; routing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, virtual threads
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (inference events)
- **Security architecture:** RBAC, model ACLs, audit
- **Key advanced concepts:** Drift detection, canary, shadow traffic
- **Why it is industrial:** Serving-grade platform with drift monitoring

## JAVA-605 — Model Fairness & Bias Audit Toolkit

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Fairness
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Models must be audited for bias and fairness with explainable metrics.
- **Core engineering problem:** Fairness audit toolkit with metric computation, slicing and reports.
- **Architecture:** Modular monolith; metric engine; slicer; report service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (audit events)
- **Security architecture:** RBAC, audit confidentiality, audit
- **Key advanced concepts:** Fairness metrics, slicing, reports
- **Why it is industrial:** Fairness-grade auditing with slicing

## JAVA-606 — ML Ops Deployment & Canary Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** MLOps / Deployment
- **Business problem:** Model deployments need canary releases, rollbacks and health gates.
- **Core engineering problem:** Deployment engine with canary stages, health checks and rollback.
- **Architecture:** Modular monolith; deployment workflow; health monitor; rollback service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (deploy events)
- **Security architecture:** RBAC, deployment approvals, audit
- **Key advanced concepts:** Canary stages, health gates, rollback
- **Why it is industrial:** MLOps-grade deployment with rollback

## JAVA-607 — Vector Search & Embedding Index Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Vector Search
- **Business problem:** Embeddings must be indexed and searched with metadata filtering.
- **Core engineering problem:** Vector index service with ANN search, metadata filters and updates.
- **Architecture:** Modular monolith; vector store; ANN engine; filter service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (pgvector), Redis 7
- **Messaging:** Kafka (embedding events)
- **Security architecture:** RBAC, index ACLs, audit
- **Key advanced concepts:** ANN search, metadata filters, incremental updates
- **Why it is industrial:** Vector-grade search with filtering

## JAVA-608 — Local RAG Pipeline & Document Q&A

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / RAG
- **Business problem:** Documents must ground LLM answers with retrieval and citation (fully local).
- **Core engineering problem:** RAG pipeline with chunking, embedding, retrieval and citation.
- **Architecture:** Modular monolith; chunker; embedding service; retriever; Ollama adapter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (pgvector), MinIO, Ollama (local)
- **Messaging:** Kafka (ingest jobs)
- **Security architecture:** RBAC, document ACLs in retrieval, audit
- **Key advanced concepts:** Chunking, retrieval, citations, grounding
- **Why it is industrial:** RAG-grade pipelines with citation grounding

## JAVA-609 — Entity Resolution & Deduplication

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Entity Resolution
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Duplicate entities across sources must be resolved with clustering and confidence.
- **Core engineering problem:** Entity resolution with blocking, pairwise scoring and cluster management.
- **Architecture:** Modular monolith; blocking engine; scorer; cluster service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (match events)
- **Security architecture:** RBAC, PII masking, audit
- **Key advanced concepts:** Blocking, scoring, clustering
- **Why it is industrial:** Resolution-grade matching with clustering

## JAVA-610 — Semantic Text Analytics & Classification

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Text Analytics
- **Business problem:** Unstructured text must be classified, extracted and routed.
- **Core engineering problem:** Text analytics pipeline with classification models, extraction and routing.
- **Architecture:** Modular monolith; text pipeline; classifier; extraction service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2, Ollama (local)
- **Messaging:** Kafka (text events)
- **Security architecture:** RBAC, PII redaction, audit
- **Key advanced concepts:** Classification, extraction, routing
- **Why it is industrial:** Text-grade analytics with PII-aware routing

## JAVA-611 — Time-Series Anomaly Detection Engine

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Anomaly Detection
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Time-series anomalies must be detected with seasonality and alert routing.
- **Core engineering problem:** Anomaly engine with seasonal models, scoring and alert routing.
- **Architecture:** Modular monolith; anomaly engine; seasonality service; alert router
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (metric streams)
- **Security architecture:** RBAC, alert routing, audit
- **Key advanced concepts:** Seasonality, scoring, alerts
- **Why it is industrial:** Anomaly-grade detection with seasonal models

## JAVA-612 — Forecasting Service with Seasonality

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Forecasting
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Forecasting must handle seasonality, holidays and hierarchies.
- **Core engineering problem:** Forecasting service with decomposition, holiday calendars and reconciliation.
- **Architecture:** Modular monolith; forecast engine; holiday store; reconciliation
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (forecast events)
- **Security architecture:** RBAC, forecast versioning, audit
- **Key advanced concepts:** Decomposition, holidays, hierarchy reconciliation
- **Why it is industrial:** Forecast-grade accuracy with reconciliation

## JAVA-613 — Recommendation Engine & Feedback Loop

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Recommendations
- **Business problem:** Recommendations must serve real time with feedback loops and exploration.
- **Core engineering problem:** Rec engine with candidate generation, ranking and exploration.
- **Architecture:** Modular monolith; candidate service; ranker; feedback pipeline
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (feedback events)
- **Security architecture:** RBAC, user data protection, audit
- **Key advanced concepts:** Candidates, ranking, exploration
- **Why it is industrial:** Recommendation-grade serving with feedback

## JAVA-614 — Churn Prediction & Retention Workbench

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Churn
- **Business problem:** Churn must be predicted with retention interventions and experiment tracking.
- **Core engineering problem:** Churn workbench with model scoring, interventions and A/B tracking.
- **Architecture:** Modular monolith; scoring service; intervention engine; experiment tracker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (event streams)
- **Security architecture:** RBAC, experiment governance, audit
- **Key advanced concepts:** Scoring, interventions, experiments
- **Why it is industrial:** Churn-grade analytics with intervention loops

## JAVA-615 — NLP Entity Extraction & Redaction

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / NLP
- **Business problem:** Entities must be extracted from documents with redaction and verification.
- **Core engineering problem:** NER pipeline with model extraction, confidence and redaction.
- **Architecture:** Modular monolith; NER pipeline; confidence engine; redaction service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Ollama (local)
- **Messaging:** Kafka (doc events)
- **Security architecture:** RBAC, PII redaction, audit
- **Key advanced concepts:** NER, confidence, redaction
- **Why it is industrial:** NLP-grade extraction with redaction

## JAVA-616 — Batch Feature Backfill & Snapshotting

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Features
- **Business problem:** Batch features must be backfilled and snapshotted with consistency.
- **Core engineering problem:** Backfill engine with snapshotting, versioning and consistency checks.
- **Architecture:** Modular monolith; backfill engine; snapshot store; consistency checker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (backfill events)
- **Security architecture:** RBAC, dataset ACLs, audit
- **Key advanced concepts:** Snapshotting, versioning, consistency
- **Why it is industrial:** Feature-grade backfills with consistency

## JAVA-617 — Training Data Versioning & Lineage

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Data
- **Business problem:** Training data must be versioned with lineage and provenance.
- **Core engineering problem:** Training data versioning with lineage tracking and provenance records.
- **Architecture:** Modular monolith; dataset store; lineage engine; provenance service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (dataset events)
- **Security architecture:** RBAC, provenance integrity, audit
- **Key advanced concepts:** Versioning, lineage, provenance
- **Why it is industrial:** Dataset-grade versioning with provenance

## JAVA-618 — Hyperparameter Optimization Scheduler

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / HPO
- **Business problem:** Hyperparameter optimization must schedule trials with early stopping.
- **Core engineering problem:** HPO scheduler with trial management, early stopping and results.
- **Architecture:** Modular monolith; trial scheduler; early-stopping engine; result store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (trial events)
- **Security architecture:** RBAC, project scoping, audit
- **Key advanced concepts:** Trial scheduling, early stopping, results
- **Why it is industrial:** HPO-grade scheduling with early stopping

## JAVA-619 — Model Explainability & SHAP-style Reports

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Explainability
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Model decisions must be explained with feature attributions.
- **Core engineering problem:** Explainability service with attribution computation and report generation.
- **Architecture:** Modular monolith; attribution engine; report service; explainer API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (explanation events)
- **Security architecture:** RBAC, model governance, audit
- **Key advanced concepts:** Attributions, reports, explanations
- **Why it is industrial:** Explainability-grade reports with attributions

## JAVA-620 — Streaming ML Inference Topology

- **Difficulty:** Omega (Tier 5)
- **Industry:** ML Platform / Streaming
- **Business problem:** Streaming inference topologies must be deployed with state and exactly-once.
- **Core engineering problem:** Streaming inference with stateful processing, windowing and exactly-once.
- **Architecture:** Modular monolith; topology engine; window service; state store
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (streams)
- **Security architecture:** RBAC, state isolation, audit
- **Key advanced concepts:** Stateful windows, exactly-once, topologies
- **Why it is industrial:** Streaming-grade inference with state

## JAVA-621 — Data Masking for Non-Production

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Privacy
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Non-production data must be masked with realism and referential integrity.
- **Core engineering problem:** Masking pipeline with realistic synthesis and referential integrity.
- **Architecture:** Modular monolith; masking engine; synthesis service; integrity checker
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (mask events)
- **Security architecture:** RBAC, export approvals, audit
- **Key advanced concepts:** Realistic masking, referential integrity
- **Why it is industrial:** Privacy-grade masking with integrity

## JAVA-622 — Test Data Management & Synthesis

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Test Data
- **Business problem:** Test data must be synthesized, versioned and refreshed per environment.
- **Core engineering problem:** Test data management with synthesis, versioning and refresh automation.
- **Architecture:** Modular monolith; synthesis engine; version store; refresh service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (TDM events)
- **Security architecture:** RBAC, environment scoping, audit
- **Key advanced concepts:** Synthesis, versioning, refresh
- **Why it is industrial:** TDM-grade management with refresh automation

## JAVA-623 — Data Retention & Lifecycle Policer

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Governance
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Data retention and lifecycle policies must be enforced automatically.
- **Core engineering problem:** Retention policer with lifecycle rules, tiering and destruction.
- **Architecture:** Modular monolith; policy engine; tiering service; destruction workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (lifecycle events)
- **Security architecture:** RBAC, destruction approvals, audit
- **Key advanced concepts:** Lifecycle rules, tiering, destruction
- **Why it is industrial:** Governance-grade retention with tiering

## JAVA-624 — Query Federation & Virtual Data Catalog

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Query
- **Business problem:** Queries must federate across sources with a virtual catalog.
- **Core engineering problem:** Federation engine with query planning, pushdown and caching.
- **Architecture:** Modular monolith; query planner; adapter framework; cache layer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (query events)
- **Security architecture:** RBAC, field-level ACLs, audit
- **Key advanced concepts:** Query planning, pushdown, caching
- **Why it is industrial:** Federation-grade querying with pushdown

## JAVA-625 — Real-Time Analytics with Materialized Views

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Analytics
- **Business problem:** Materialized views must be maintained with freshness and consistency.
- **Core engineering problem:** MV engine with incremental refresh, freshness SLAs and dependencies.
- **Architecture:** Modular monolith; MV service; refresh engine; dependency graph
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (refresh events)
- **Security architecture:** RBAC, dataset ACLs, audit
- **Key advanced concepts:** Incremental refresh, freshness, dependencies
- **Why it is industrial:** Analytics-grade MVs with freshness SLAs

## JAVA-626 — Metrics Aggregation & Rollup Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Metrics
- **Business problem:** Metrics must be aggregated, rolled up and stored with retention.
- **Core engineering problem:** Metrics engine with rollups, downsampling and retention tiers.
- **Architecture:** Modular monolith; aggregation engine; rollup service; retention
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (metric events)
- **Security architecture:** RBAC, metric ACLs, audit
- **Key advanced concepts:** Rollups, downsampling, retention
- **Why it is industrial:** Metrics-grade aggregation with rollups

## JAVA-627 — Data Warehousing ETL with Slowly Changing Dimensions

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Warehouse / ETL
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** ETL must handle slowly changing dimensions with history and audit.
- **Core engineering problem:** ETL orchestrator with SCD logic, dependencies and audit.
- **Architecture:** Modular monolith; ETL engine; SCD service; dependency scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (ETL events)
- **Security architecture:** RBAC, pipeline governance, audit
- **Key advanced concepts:** SCD logic, dependencies, audit
- **Why it is industrial:** ETL-grade orchestration with SCD integrity

## JAVA-628 — Data Mesh Product Registry

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Mesh
- **Business problem:** Data mesh products must be registered with ownership and contracts.
- **Core engineering problem:** Data product registry with ownership, contracts and discovery.
- **Architecture:** Modular monolith; product registry; contract service; discovery
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (product events)
- **Security architecture:** RBAC, domain ownership, audit
- **Key advanced concepts:** Ownership, contracts, discovery
- **Why it is industrial:** Mesh-grade registration with domain ownership

## JAVA-629 — Schema Evolution & Compatibility Guardian

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Schemas
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Schemas must evolve with compatibility guarantees and breaking-change detection.
- **Core engineering problem:** Schema evolution guardian with compatibility checks and change gates.
- **Architecture:** Modular monolith; schema registry; compatibility engine; gate service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (schema events)
- **Security architecture:** RBAC, change approvals, audit
- **Key advanced concepts:** Compatibility checks, gates, versions
- **Why it is industrial:** Schema-grade evolution with compatibility

## JAVA-630 — Data Lineage & Impact Analysis

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Lineage
- **Business problem:** Lineage must support impact analysis across pipelines and datasets.
- **Core engineering problem:** Lineage engine with impact analysis, drift detection and visualizations.
- **Architecture:** Modular monolith; lineage graph; impact analyzer; visualization API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext)
- **Messaging:** Kafka (lineage events)
- **Security architecture:** RBAC, graph ACLs, audit
- **Key advanced concepts:** Impact analysis, drift, visualizations
- **Why it is industrial:** Lineage-grade analytics with impact analysis

## JAVA-631 — Data Virtualization Security Layer

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Virtualization
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Data virtualization must apply security policies across heterogeneous sources.
- **Core engineering problem:** Virtualization layer with policy enforcement, masking and query routing.
- **Architecture:** Modular monolith; policy engine; masking service; query router
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (query events)
- **Security architecture:** RBAC, ABAC, audit
- **Key advanced concepts:** Policy enforcement, masking, routing
- **Why it is industrial:** Virtualization-grade security with ABAC

## JAVA-632 — Unstructured Document Extraction Pipeline

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Document Extraction
- **Business problem:** Unstructured documents must be extracted with structure and confidence.
- **Core engineering problem:** Document extraction pipeline with OCR-style processing, structure and confidence.
- **Architecture:** Modular monolith; extraction pipeline; structure engine; confidence scorer
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, Apache Tika
- **Data layer:** PostgreSQL 16, MinIO, OpenSearch 2
- **Messaging:** Kafka (doc jobs)
- **Security architecture:** RBAC, document ACLs, audit
- **Key advanced concepts:** Extraction, structure, confidence
- **Why it is industrial:** Extraction-grade pipelines with confidence

## JAVA-633 — OCR Post-Processing & Confidence Workbench

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / OCR QA
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** OCR output must be validated with confidence scoring and correction workflows.
- **Core engineering problem:** OCR post-processing with confidence scoring, corrections and QA queues.
- **Architecture:** Modular monolith; OCR pipeline; confidence engine; QA workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (OCR events)
- **Security architecture:** RBAC, QA sampling, audit
- **Key advanced concepts:** Confidence scoring, corrections, QA
- **Why it is industrial:** OCR-grade QA with correction workflows

## JAVA-634 — Table Extraction & Tabular Data Structuring

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Table Extraction
- **Secondary (public-sector) domain:** Government / Compliance / Public Infrastructure
- **Business problem:** Tabular data in documents must be structured and validated.
- **Core engineering problem:** Table extraction with structure recognition, validation and export.
- **Architecture:** Modular monolith; table pipeline; structure engine; validator
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (extract jobs)
- **Security architecture:** RBAC, document ACLs, audit
- **Key advanced concepts:** Structure recognition, validation, exports
- **Why it is industrial:** Extraction-grade table structuring with validation

## JAVA-635 — Graph Analytics & Path Query Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Graph
- **Business problem:** Graph analytics must support path queries, centrality and communities.
- **Core engineering problem:** Graph service with traversal queries, centrality and community detection.
- **Architecture:** Modular monolith; graph store; traversal engine; analytics library
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+graph ext)
- **Messaging:** Kafka (graph events)
- **Security architecture:** RBAC, graph ACLs, audit
- **Key advanced concepts:** Traversals, centrality, communities
- **Why it is industrial:** Graph-grade analytics with traversal performance

## JAVA-636 — Geospatial Analytics & Heatmap Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Geo
- **Business problem:** Geospatial analytics must power heatmaps, containment and proximity queries.
- **Core engineering problem:** Geo service with spatial indexing, heatmaps and containment queries.
- **Architecture:** Modular monolith; geo store; spatial index; heatmap engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext)
- **Messaging:** Kafka (geo events)
- **Security architecture:** RBAC, location privacy, audit
- **Key advanced concepts:** Spatial indexing, heatmaps, containment
- **Why it is industrial:** Geo-grade analytics with spatial indexing

## JAVA-637 — Stream-to-Lakehouse Compaction Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Lakehouse
- **Business problem:** Stream data must be compacted into lakehouse tables efficiently.
- **Core engineering problem:** Compaction service with merge-on-read logic, partitioning and freshness.
- **Architecture:** Modular monolith; compaction engine; partitioner; freshness monitor
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (stream data)
- **Security architecture:** RBAC, table ACLs, audit
- **Key advanced concepts:** Compaction, partitioning, freshness
- **Why it is industrial:** Lakehouse-grade compaction with partitioning

## JAVA-638 — Data Reliability & Freshness Monitor

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Reliability
- **Business problem:** Data freshness and reliability must be monitored with SLIs.
- **Core engineering problem:** Reliability monitor with freshness SLIs, error budgets and alerts.
- **Architecture:** Modular monolith; freshness checker; SLI engine; alert service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+TimescaleDB ext)
- **Messaging:** Kafka (health events)
- **Security architecture:** RBAC, pipeline scoping, audit
- **Key advanced concepts:** SLIs, error budgets, freshness
- **Why it is industrial:** Reliability-grade monitoring with SLIs

## JAVA-639 — Self-Serve Analytics Workbench

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Self-Serve
- **Business problem:** Business users need governed self-serve analytics without raw SQL access.
- **Core engineering problem:** Self-serve workbench with semantic layer, governed queries and sharing.
- **Architecture:** Modular monolith; semantic layer; query governor; sharing service
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (query events)
- **Security architecture:** RBAC, ABAC on datasets, audit
- **Key advanced concepts:** Semantic layer, governance, sharing
- **Why it is industrial:** Self-serve-grade analytics with governance

## JAVA-640 — Embedding Cache & Inference Cost Governor

- **Difficulty:** Omega (Tier 5)
- **Industry:** AI / Inference Ops
- **Business problem:** Embedding inference costs must be governed with caching and quotas.
- **Core engineering problem:** Inference governor with caching, quotas and cost attribution.
- **Architecture:** Modular monolith; cache layer; quota engine; cost attribution
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7, Ollama (local)
- **Messaging:** Kafka (inference events)
- **Security architecture:** RBAC, quota enforcement, audit
- **Key advanced concepts:** Caching, quotas, cost attribution
- **Why it is industrial:** Inference-grade governance with quotas

## JAVA-641 — Data Sampling & Statistical QA Service

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / QA
- **Business problem:** Data sampling must be statistically sound for QA and auditing.
- **Core engineering problem:** Sampling service with statistical methods, stratification and QA reports.
- **Architecture:** Modular monolith; sampling engine; stratification service; report builder
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (sample events)
- **Security architecture:** RBAC, sample confidentiality, audit
- **Key advanced concepts:** Statistical sampling, stratification, reports
- **Why it is industrial:** QA-grade sampling with statistical rigor

## JAVA-642 — Event Schema Registry & Validation

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Schemas
- **Business problem:** Event schemas must be registered, validated and versioned centrally.
- **Core engineering problem:** Event schema registry with validation, compatibility and versioning.
- **Architecture:** Modular monolith; schema store; validator; compatibility engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (schema events)
- **Security architecture:** RBAC, schema governance, audit
- **Key advanced concepts:** Validation, compatibility, versions
- **Why it is industrial:** Schema-grade registry with compatibility

## JAVA-643 — Data Product API & Access Billing

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Products
- **Business problem:** Data products must be served as APIs with access billing and quotas.
- **Core engineering problem:** Data product API layer with access control, billing and quotas.
- **Architecture:** Modular monolith; product API; access service; billing engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, Redis 7
- **Messaging:** Kafka (usage events)
- **Security architecture:** API keys, rate limits, billing audit
- **Key advanced concepts:** Access control, billing, quotas
- **Why it is industrial:** Product-grade API with usage billing

## JAVA-644 — Dark Data Discovery & Usage Metering

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Discovery
- **Business problem:** Dark data must be discovered, classified and its usage metered.
- **Core engineering problem:** Dark data discovery with classification, indexing and usage metering.
- **Architecture:** Modular monolith; discovery pipeline; classifier; usage meter
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2, MinIO
- **Messaging:** Kafka (discovery events)
- **Security architecture:** RBAC, classification ACLs, audit
- **Key advanced concepts:** Classification, indexing, metering
- **Why it is industrial:** Discovery-grade classification with metering

## JAVA-645 — Offline Data Sync for Field Devices

- **Difficulty:** Omega (Tier 5)
- **Industry:** Data Platform / Sync
- **Business problem:** Field devices need offline data sync with conflict resolution.
- **Core engineering problem:** Offline sync engine with CRDT-style merging, conflict resolution and queues.
- **Architecture:** Modular monolith; sync engine; conflict resolver; offline queue
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, SQLite (field mode)
- **Messaging:** Kafka (sync events)
- **Security architecture:** Device auth, data integrity, audit
- **Key advanced concepts:** CRDT-style merging, conflicts, queues
- **Why it is industrial:** Sync-grade offline support with conflict resolution
