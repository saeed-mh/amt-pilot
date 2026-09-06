# AmtPilot

I am building AmtPilot as a learning and portfolio project. The goal is to help users understand German administrative processes, see their official requirements, and track their own applications. For now, the available sample data is focused on Dortmund.

> This is an educational project and does not provide legal advice.

## Current status

**Last updated: 7 September 2026**

The backend MVP is in progress. At the moment, it supports:

- User registration and login with JWT authentication
- Viewing and updating a user profile
- Browsing authorities, processes, and official requirements
- Creating, listing, viewing, and updating applications
- Application checklists with automatic completeness calculation
- Local PDF storage with validation and ownership-safe document services
- Validation, consistent error responses, and request trace IDs
- PostgreSQL, Flyway migrations, Swagger UI, and automated tests

The current test suite has **77 passing tests**, including unit, controller, migration, and repository integration tests.

Next, I plan to expose the document upload and download endpoints, add PDF text extraction, and then start the first AI feature. A simple frontend will come after the main backend workflow is ready.

## Run locally

Requirements: Java 21 or newer and Docker Desktop.

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd spring-boot:run`. Set `JWT_SECRET` to a value with at least 32 characters before starting the application. Uploaded files are stored in `./uploads` by default, or in the directory configured with `UPLOAD_DIR`.

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
