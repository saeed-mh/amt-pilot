# AmtPilot

I am building AmtPilot as a learning and portfolio project. The goal is to help users understand German administrative processes, see their official requirements, and track their own applications. For now, the available sample data is focused on Dortmund.

> This is an educational project and does not provide legal advice.

## Current status

**Last updated: 24 September 2026**

The MVP is in progress. At the moment, it supports:

- User registration and login with JWT authentication
- Viewing and updating a user profile
- Browsing a seeded Dortmund catalog with 3 authorities, 8 processes, and 38 official requirements
- Creating, listing, viewing, updating, and deleting applications
- Preventing more than one unfinished application for the same user and process
- Application checklists with required and optional items and automatic completeness calculation
- Uploading, downloading, and deleting PDF documents with validation and ownership checks
- Automatically completing a checklist item when its PDF is uploaded and reopening it when the PDF is deleted
- Starting an AI analysis workflow with a visible `ANALYZING` application status
- A separate Python FastAPI AI service using LangChain and Gemini 3.8 Flash
- Structured document analysis with document type, language, summary, extracted fields and evidence, missing information, and warnings
- Validation, consistent error responses, and request trace IDs
- PostgreSQL, Flyway migrations, Swagger UI, and automated tests
- A Vue 3 frontend with authentication, profile editing, process search, application management, and interactive checklists
- English and German interface support with saved language selection
- A simple About page and footer that explain the project and its workflow

The Spring Boot backend has **98 passing tests**. The AI service has **5 passing tests** and passes Ruff checks. The frontend also passes its lint and production build checks.

The first real LLM call is now working with sample text. LangChain sends the text to Gemini and validates the structured response with Pydantic. PDF text extraction and the connection between the AI service, Spring Boot, and Vue are the next steps.

## Run locally

Requirements: Java 21 or newer, Docker Desktop, Python 3.11 or newer, and a Node.js version supported by `frontend/package.json`.

Create a root `.env` file from `.env.example` and configure the required secrets:

```env
JWT_SECRET=replace-with-a-secret-with-at-least-32-characters
GOOGLE_API_KEY=replace-with-your-google-ai-api-key
GOOGLE_MODEL=gemini-3.8-flash
```

Start PostgreSQL and the Spring Boot backend:

```bash
docker compose up -d postgres
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd spring-boot:run`. Uploaded files are stored in `./uploads` by default, or in the directory configured with `UPLOAD_DIR`.

Start the frontend in a second terminal:

```bash
cd frontend
npm install
npm run dev
```

On Windows PowerShell, use `npm.cmd` instead of `npm` if script execution is disabled.

Start the AI service in another terminal:

```bash
cd ai-service
python -m venv .venv
python -m pip install -e ".[dev]"
uvicorn app.main:app --reload --port 8001
```

Activate the virtual environment before installing or running the service. On Windows PowerShell, use `.venv\Scripts\Activate.ps1`. The Gemini free tier is suitable for development with sample or redacted documents; do not upload sensitive personal documents.

Useful links:

- API home: <http://localhost:8080/>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Backend health check: <http://localhost:8080/actuator/health>
- AI service health check: <http://localhost:8001/health>
- Frontend: <http://localhost:5173/>

Run the backend tests with:

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

Check the AI service with:

```bash
cd ai-service
pytest
ruff check app tests
```

## Learning notes

The development order and short explanations are available in [docs/learning-roadmap.md](docs/learning-roadmap.md).
