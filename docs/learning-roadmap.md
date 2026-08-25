# AmtPilot learning roadmap

The project follows the specification's main architectural rule: build and test the deterministic backend first, then add AI as an adapter.

## Milestone 0 — Foundation

Learn how Spring Boot starts, how Maven manages dependencies, how Docker provides PostgreSQL, why Flyway owns schema changes, and how CI repeats the build on GitHub.

Deliverable: a reproducible build with health checks, API documentation, a versioned schema, and a PostgreSQL migration test.

## Milestone 1 — Core backend

Implement these as separate, reviewable increments:

1. User model and password-based registration/login with JWT access and refresh tokens.
2. User profile and onboarding fields.
3. Authority, official source, process, and requirement catalog for Dortmund.
4. Application aggregate, CRUD endpoints, ownership checks, and state transitions.
5. Consistent API response/error envelopes and trace IDs.

## Milestone 2 — Documents

Add validated PDF/JPG/PNG uploads, private storage abstraction, SHA-256 metadata, PDFBox text extraction, authorization, retention, and deletion.

## Milestone 3 — First AI adapter

Define provider-independent interfaces first. Add supported-process classification and structured document extraction with schema validation, evidence references, confidence thresholds, and mock-model contract tests.

## Milestone 4 — Workflow

Build deterministic tasks, requirements, completeness calculation, user-confirmed document matching, and the application state machine.

## Milestone 5 — Official-source retrieval

Add curated ingestion, chunk metadata, pgvector retrieval, freshness/checksum handling, citations, and safe failure when no official source supports an answer.

## Milestone 6 — Deadlines

Add deadline provenance, uncertainty confirmation, the scheduled reminder job, and in-app notifications.

## Milestones 7–9 — Drafting, hardening, and demo

Finish grounded draft generation, privacy controls, security and ownership tests, metrics, auditability, Dortmund seed data, documentation, and the end-to-end demo.

## Definition of a useful increment

Every feature must have a user story, an API or UI interaction, a failure mode, and at least one test. Each completed increment should be committed and pushed before the next one begins.
