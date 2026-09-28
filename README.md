# AmtPilot

AmtPilot is a full-stack portfolio project that helps users understand German administrative processes, prepare the required documents, and track their applications. The current catalog uses Dortmund as its first real-world example.

> AmtPilot is an educational project and does not provide legal advice.

## Current status

**Last updated: 28 September 2026**

### Key features

- Secure registration and JWT-based authentication with user-specific data access
- Searchable Dortmund catalog containing authorities, administrative processes, requirements, and official sources
- Application management with interactive checklists and automatic completeness calculation
- Validated PDF upload, preview, download, and deletion with automatic checklist synchronization
- AI-powered analysis of uploaded PDFs, including document classification, structured field extraction, evidence, missing information, and warnings
- Grounded cross-document AI review that compares uploaded documents with process requirements and verified official guidance, then returns readiness assessments, citations, and practical next steps
- Human-in-the-loop clarification: users can add context, save their answers, and request an answer-aware reanalysis without treating their statements as document evidence
- Responsive Vue 3 interface with English and German language support
- Automated backend, AI-service, and frontend quality checks

### AI workflow

```text
PDF upload -> structured document analysis -> grounded application review -> user clarification -> answer-aware reanalysis
```

Spring Boot manages users, applications, documents, trusted process guidance, and the asynchronous analysis workflow. A separate Python FastAPI service uses LangChain, Google Gemini, Pydantic, and `pypdf` to produce validated structured results. The review is grounded with verified official information and preserves source citations.

Users can provide clarifying context and trigger another analysis. Their answers influence personalized guidance but remain untrusted context: they cannot replace uploaded evidence or override official information. Temporary provider failures are handled without losing the application state.

The current extractor supports text-based PDFs. OCR for scanned image-only documents is planned.

### Technology

- **Backend:** Java, Spring Boot, Spring Security, JPA
- **AI service:** Python, FastAPI, LangChain, Google Gemini, Pydantic
- **Frontend:** Vue 3, Vue Router, Vite
- **Data and infrastructure:** PostgreSQL, Flyway, Docker Compose
- **Quality:** JUnit, Mockito, Testcontainers, Pytest, Ruff, ESLint

## Run locally

Requirements: Java 21 or newer, Docker Desktop, Python 3.11 or newer, and a Node.js version supported by `frontend/package.json`.

Create a root `.env` file from `.env.example` and configure the required secrets:

```env
JWT_SECRET=replace-with-a-secret-with-at-least-32-characters
GOOGLE_API_KEY=replace-with-your-google-ai-api-key
GOOGLE_MODEL=gemini-3.5-flash-lite
AI_SERVICE_URL=http://localhost:8001
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
```

Activate the virtual environment. On Windows PowerShell, use `.venv\Scripts\Activate.ps1`. Then install and run the service:

```bash
python -m pip install -e ".[dev]"
uvicorn app.main:app --reload --port 8001
```

Use sample or properly redacted documents during development. Do not upload sensitive personal documents.

### Local URLs

- Frontend: <http://localhost:5173/>
- Backend API: <http://localhost:8080/>
- Backend Swagger UI: <http://localhost:8080/swagger-ui.html>
- Backend health check: <http://localhost:8080/actuator/health>
- AI service Swagger UI: <http://localhost:8001/docs>
- AI service health check: <http://localhost:8001/health>

## Development checks

Backend:

```bash
./mvnw test
```

PostgreSQL integration tests require Docker to be running.

Frontend:

```bash
cd frontend
npm run lint
npm run build
```

AI service:

```bash
cd ai-service
pytest
ruff check app tests
```
