# AmtPilot

I am building AmtPilot as a learning and portfolio project. The goal is to help users understand German administrative processes, see their official requirements, and track their own applications. For now, the available sample data is focused on Dortmund.

> This is an educational project and does not provide legal advice.

## Current status

**Last updated: 8 September 2026**

The MVP is in progress. At the moment, it supports:

- User registration and login with JWT authentication
- Viewing and updating a user profile
- Browsing authorities, processes, and official requirements
- Creating, listing, viewing, and updating applications
- Application checklists with automatic completeness calculation
- Uploading and listing PDF documents with validation and ownership checks
- Validation, consistent error responses, and request trace IDs
- PostgreSQL, Flyway migrations, Swagger UI, and automated tests
- A Vue 3 frontend with signup, login, JWT persistence, a protected dashboard, and logout

The backend test suite has **79 passing tests**, including unit, controller, migration, and repository integration tests. The frontend also passes its lint and production build checks.

Next, I plan to connect protected frontend pages to the existing APIs, finish document downloading, add PDF text extraction, and then start the first AI feature.

## Run locally

Requirements: Java 21 or newer, Docker Desktop, and a Node.js version supported by `frontend/package.json`.

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd spring-boot:run`. Set `JWT_SECRET` to a value with at least 32 characters before starting the application. Uploaded files are stored in `./uploads` by default, or in the directory configured with `UPLOAD_DIR`.

Start the frontend in a second terminal:

```bash
cd frontend
npm install
npm run dev
```

On Windows PowerShell, use `npm.cmd` instead of `npm` if script execution is disabled.

Useful links:

- API home: <http://localhost:8080/>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health check: <http://localhost:8080/actuator/health>
- Frontend: <http://localhost:5173/>

Run the tests with:

```bash
./mvnw test
```

The PostgreSQL integration tests require Docker to be running.

Check the frontend with:

```bash
cd frontend
npm run lint
npm run build
```

## Learning notes

The development order and short explanations are available in [docs/learning-roadmap.md](docs/learning-roadmap.md).
