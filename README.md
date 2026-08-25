# AmtPilot

AmtPilot is a portfolio-quality Spring Boot backend that helps users understand and track selected German administrative processes. The MVP is intentionally limited to Dortmund and only returns definitive requirements when they are backed by curated official sources.

> AmtPilot is an educational project and does not provide legal advice.

## Current milestone

Milestone 0 establishes the project foundation:

- Spring Boot modular-monolith skeleton
- Spring Security with secure-by-default routes
- PostgreSQL with pgvector
- Flyway-owned schema
- Actuator health probes
- OpenAPI/Swagger UI
- Testcontainers migration test
- GitHub Actions CI

Business features such as accounts, supported processes, and applications are implemented in Milestone 1. AI is intentionally deferred until the deterministic domain and API work without it.

## Prerequisites

- Java 21 or newer
- Docker Desktop with Docker Compose

## Run locally

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

On Windows PowerShell, use `./mvnw.cmd spring-boot:run`.

Useful URLs:

- Health: <http://localhost:8080/actuator/health>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Stop the database with:

```bash
docker compose down
```

The named database volume is preserved. Use `docker compose down --volumes` only when you intentionally want to delete local database data.

## Test

```bash
./mvnw verify
```

The fast unit test always runs. The PostgreSQL migration integration test runs when Docker is available and is skipped otherwise.

## Configuration

Local defaults are development-only and can be overridden with environment variables:

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/amtpilot` |
| `DB_USERNAME` | `amtpilot` |
| `DB_PASSWORD` | `amtpilot` |

Production secrets must come from environment variables or a secret manager and must never be committed.

## Learning roadmap

See [docs/learning-roadmap.md](docs/learning-roadmap.md) for the implementation order and the reason behind each milestone.
