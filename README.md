# AmtPilot

I am building AmtPilot as a learning and portfolio project. The goal is to help users understand German administrative processes, see their official requirements, and track their own applications. For now, the available sample data is focused on Dortmund.

> This is an educational project and does not provide legal advice.

## Current status

**Last updated: 6 September 2026**

The backend MVP is in progress. At the moment, it supports:

- User registration and login with JWT authentication
- Viewing and updating a user profile
- Browsing authorities, processes, and official requirements
- Creating, listing, viewing, and updating applications
- Validation, consistent error responses, and request trace IDs
- PostgreSQL, Flyway migrations, Swagger UI, and automated tests

The current test suite has **52 passing tests**, including unit, controller, migration, and repository integration tests.

Next, I plan to add better application progress tracking, more curated process data, and later a simple frontend. The LLM feature will be added after the main workflow is stable.

## Run locally

Requirements: Java 21 or newer and Docker Desktop.

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd spring-boot:run`. Set `JWT_SECRET` to a value with at least 32 characters before starting the application.

Useful links:

- API home: <http://localhost:8080/>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health check: <http://localhost:8080/actuator/health>

Run the tests with:

```bash
./mvnw test
```

The PostgreSQL integration tests require Docker to be running.

## Learning notes

The development order and short explanations are available in [docs/learning-roadmap.md](docs/learning-roadmap.md).
