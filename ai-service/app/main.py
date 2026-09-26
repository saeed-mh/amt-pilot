from fastapi import FastAPI

from app.routers.application_advice import router as application_advice_router
from app.routers.document_analysis import router as document_analysis_router

app = FastAPI(
    title="AmtPilot AI Service",
    version="0.1.0",
)

app.include_router(document_analysis_router)
app.include_router(application_advice_router)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}
