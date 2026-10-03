# AmtPilot

AmtPilot is a full-stack portfolio project that helps users understand German administrative processes, prepare the required documents, and track their applications. The current catalog uses Dortmund as its first real-world example.

> AmtPilot is an educational project and does not provide legal advice.

## Current status

**Last updated: 3 October 2026**

### Key features

- Secure registration and JWT-based authentication with user-specific data access
- Searchable Dortmund catalog containing authorities, administrative processes, requirements, and official sources
- Application management with interactive checklists and automatic completeness calculation
- Validated PDF upload, preview, download, and deletion with automatic checklist synchronization
- Hybrid PDF extraction with automatic local German/English OCR fallback for scanned pages
- AI-powered analysis of uploaded PDFs, including document classification, structured field extraction, evidence, missing information, and warnings
- Grounded RAG review using Gemini embeddings and PostgreSQL `pgvector` to retrieve the most relevant official guidance before assessing uploaded documents
- Deterministic validation safeguards for important document distinctions, such as recognizing that a `Meldebestätigung` or `Anmeldebestätigung` cannot replace the required `Wohnungsgeberbestätigung`
- Source-backed readiness assessments showing satisfied, missing, or unclear requirements with citations and practical next steps
- Human-in-the-loop clarification: users can add context, save their answers, and request an answer-aware reanalysis without treating their statements as document evidence
- Responsive Vue 3 interface with English and German language support
- Automated backend, AI-service, and frontend quality checks

### AI workflow

```text
Official guide -> chunks -> Gemini embeddings -> pgvector index
PDF upload -> native text or OCR -> structured extraction -> semantic retrieval -> grounded review -> cited guidance
```

Spring Boot manages users, applications, documents, trusted process guidance, vector storage, and the asynchronous analysis workflow. On the first analysis, official guidance is split into searchable chunks, embedded with Gemini, and stored in PostgreSQL. For each review, AmtPilot embeds the application context and retrieves the five closest official chunks using cosine similarity.

A separate Python FastAPI service uses LangChain, Google Gemini, Pydantic, and `pypdf` for structured document extraction, embeddings, and the final application review. Only the retrieved official context is sent to the review model, while Spring Boot attaches deterministic source citations to the result. Critical validation rules also correct unsafe model classifications before results reach the user. The first RAG corpus covers Dortmund Address Registration.

Users can provide clarifying context and trigger another analysis. Their answers influence personalized guidance but remain untrusted context: they cannot replace uploaded evidence or override official information. Temporary provider failures are handled without losing the application state.

Text-based pages keep their original text. Scanned pages are processed locally with OCRmyPDF and Tesseract before entering the same structured AI-analysis pipeline.

### Technology

- **Backend:** Java, Spring Boot, Spring Security, JPA
- **AI service:** Python, FastAPI, LangChain, Google Gemini, Pydantic
- **Frontend:** Vue 3, Vue Router, Vite
- **Data and infrastructure:** PostgreSQL, pgvector, Flyway, Docker Compose
- **Quality:** JUnit, Mockito, Testcontainers, Pytest, Ruff, ESLint

## Run locally

Requirements: Java 21 or newer, Docker Desktop, Python 3.11 or newer, Tesseract 5 with German and English language data, and a Node.js version supported by `frontend/package.json`.

Create a root `.env` file from `.env.example` and configure the required secrets:

```env
JWT_SECRET=replace-with-a-secret-with-at-least-32-characters
GOOGLE_API_KEY=replace-with-your-google-ai-api-key
GOOGLE_MODEL=gemini-3.5-flash-lite
GOOGLE_EMBEDDING_MODEL=gemini-embedding-001
EMBEDDING_DIMENSION=768
AI_RETRIEVAL_LIMIT=5
OCR_ENABLED=true
OCR_LANGUAGES=deu+eng
AI_SERVICE_URL=http://localhost:8000
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
uvicorn app.main:app --reload --port 8000
```

`pip install` provides OCRmyPDF. Tesseract is a separate system dependency. On Windows, install it with `winget install --id UB-Mannheim.TesseractOCR --exact` and ensure both `eng` and `deu` trained-data files are available. If they are stored outside Tesseract's default folder, set `OCR_TESSDATA_PREFIX` to the complete tessdata directory, including its `configs` folder.

Use sample or properly redacted documents during development. Do not upload sensitive personal documents.

### Local URLs

- Frontend: <http://localhost:5173/>
- Backend API: <http://localhost:8080/>
- Backend Swagger UI: <http://localhost:8080/swagger-ui.html>
- Backend health check: <http://localhost:8080/actuator/health>
- AI service Swagger UI: <http://localhost:8000/docs>
- AI service health check: <http://localhost:8000/health>

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
