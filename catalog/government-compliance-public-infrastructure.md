# Government / Compliance / Public Infrastructure — Catalog

5 projects. Full details for every project below; the 13-field design summary matches the master spec (business problem, engineering problem, architecture, Java stack, database, messaging, security model, key concepts, industrial rationale).

## JAVA-696 — Criminal Evidence Chain-of-Custody Ledger

- **Difficulty:** Omega (Tier 5)
- **Industry:** Government / Justice
- **Business problem:** Forensic evidence must be tracked with unbreakable chain-of-custody from seizure to court.
- **Core engineering problem:** Evidence ledger with hash chains, custody transfers, tamper detection and court-ready exports.
- **Architecture:** Modular monolith; custody ledger; hash-chain service; transfer workflow; court export
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, MinIO SDK
- **Data layer:** PostgreSQL 16, MinIO
- **Messaging:** Kafka (custody events)
- **Security architecture:** RBAC, four-eyes transfers, hash-chain integrity, courtroom read-only roles
- **Key advanced concepts:** Hash chaining, custody state machines, tamper alarms, exports
- **Why it is industrial:** Court-defensible custody with cryptographic integrity

## JAVA-697 — Electronic Voting with Verifiable Audit

- **Difficulty:** Omega (Tier 5)
- **Industry:** Government / Elections
- **Business problem:** Electronic voting must be verifiable, auditable and resistant to tamper and coercion claims.
- **Core engineering problem:** Verifiable voting with ballot encryption sim, mix-net-style audit and independent tally verification.
- **Architecture:** Modular monolith; ballot engine; audit trail; tally verifier; observer API
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16
- **Messaging:** Kafka (vote events)
- **Security architecture:** Voter anonymity, ballot integrity hashes, observer read-only roles, audit
- **Key advanced concepts:** End-to-end verifiability, tally proofs, coercion-resistance design
- **Why it is industrial:** Election-grade verifiability with independent audit

## JAVA-698 — Disaster Early Warning & Alerting

- **Difficulty:** Omega (Tier 5)
- **Industry:** Government / Emergency Mgmt
- **Business problem:** Disaster warnings must reach citizens through multiple channels with geo-targeting and escalation.
- **Core engineering problem:** Early-warning hub with multi-channel fanout, geo-targeting, drill management and escalation.
- **Architecture:** Modular monolith; alert engine; geo-targeter; channel fanout; drill scheduler
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16 (+PostGIS ext), Redis 7
- **Messaging:** Kafka (alert events), MQTT (public channels)
- **Security architecture:** RBAC, emergency override roles, drill vs real separation, audit
- **Key advanced concepts:** Geo-targeting, channel fanout, drills, escalation
- **Why it is industrial:** Life-safety-grade alerting with multi-channel reliability

## JAVA-699 — Smart City Command & Control Aggregator

- **Difficulty:** Omega (Tier 5)
- **Industry:** Government / Smart City
- **Business problem:** City operations must be aggregated into a command-and-control view with cross-agency response.
- **Core engineering problem:** C2 aggregator with event correlation across agencies, resource tracking and unified dispatch.
- **Architecture:** Modular monolith; event bus; correlation engine; resource registry; dispatch workflow
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway, WebSocket
- **Data layer:** PostgreSQL 16 (+PostGIS ext), Redis 7
- **Messaging:** Kafka (city events)
- **Security architecture:** RBAC, agency isolation, incident confidentiality, audit
- **Key advanced concepts:** Cross-agency correlation, resource tracking, dispatch
- **Why it is industrial:** City-grade command with cross-agency correlation

## JAVA-700 — Digital ID & Civil Registry (CRVS)

- **Difficulty:** Omega (Tier 5)
- **Industry:** Government / Identity
- **Business problem:** Civil registries must record birth, marriage and death events immutably for a lifetime identity.
- **Core engineering problem:** CRVS with life-event state machines, document issuance, dedup and lineage integrity.
- **Architecture:** Modular monolith; registry engine; life-event workflows; document service; dedup engine
- **Java technology stack:** Spring Boot 3, Spring Security, Spring Data JPA, Flyway
- **Data layer:** PostgreSQL 16, OpenSearch 2
- **Messaging:** Kafka (life events)
- **Security architecture:** RBAC, registrar four-eyes, PII encryption, tamper-evident records
- **Key advanced concepts:** Life-event state machines, dedup, document issuance
- **Why it is industrial:** Registry-grade identity with lifetime event integrity
