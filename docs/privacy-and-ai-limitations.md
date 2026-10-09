# Privacy and AI limitations

AmtPilot handles the kind of documents that can contain names, addresses, dates of birth, identification numbers, and other sensitive information. The repository is suitable for learning and portfolio review, but the current system should not be presented as a production document-processing service.

## Data flow

1. The browser sends an uploaded PDF to the authenticated Spring Boot API.
2. Spring Boot stores the PDF in the configured upload directory and its metadata in PostgreSQL.
3. When the user requests analysis, Spring Boot sends the PDF to the private FastAPI service.
4. FastAPI extracts text locally. Scanned pages first pass through local OCRmyPDF and Tesseract processing.
5. The extracted text, application requirements, user clarification, and retrieved official-guide context are sent to the configured Gemini model.
6. Structured analysis results return to Spring Boot and are stored for the user to view.

Deleting a document or application through the product should be used to remove test data when it is no longer needed. Operators must also define backup, log, and provider-retention policies before any real deployment.

## Rules for a public portfolio demo

- Accept synthetic demonstration files only.
- Display a clear warning not to upload real personal or identity documents.
- Use isolated demo credentials and a dedicated Gemini/Google Cloud project.
- Keep PostgreSQL and FastAPI private; expose only the frontend and Spring Boot entry point.
- Use HTTPS, strict CORS, rate limits, upload-size limits, and short data retention.
- Never put API keys, JWT secrets, documents, database dumps, or generated analysis reports in Git.
- Avoid logging document text, authorization headers, tokens, or model prompts containing personal data.
- Provide an obvious way to delete demo applications and their documents.

## Current AI limitations

- Model output is probabilistic and may misclassify a document or overlook a field.
- OCR quality depends on scan resolution, orientation, language, handwriting, and page layout.
- Source grounding reduces hallucination but does not guarantee that an answer is complete or legally current.
- Only Dortmund address registration currently has the full reviewed guide and vector-retrieval workflow.
- User answers personalize the next review but do not count as documentary evidence.
- Deterministic safeguards cover selected high-risk distinctions; they are not a complete fraud or authenticity system.
- AmtPilot does not submit an application, make a legal decision, or replace advice from the responsible authority.

Always verify guidance through the official links shown in the application.
