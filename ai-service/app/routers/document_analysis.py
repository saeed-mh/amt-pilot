from typing import Annotated

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status

from app.exceptions import DocumentAnalysisUnavailableError
from app.schemas import DocumentAnalysis
from app.services.document_analyzer import DocumentAnalyzer
from app.services.pdf_text_extractor import PdfTextExtractor

MAX_PDF_SIZE_BYTES = 10 * 1024 * 1024
AI_UNAVAILABLE_MESSAGE = (
    "AI analysis is temporarily unavailable. Please try again shortly."
)

router = APIRouter(
    prefix="/api/v1/documents",
    tags=["document analysis"],
)


def get_pdf_text_extractor() -> PdfTextExtractor:
    return PdfTextExtractor()


def get_document_analyzer() -> DocumentAnalyzer:
    return DocumentAnalyzer()


@router.post("/analyze", response_model=DocumentAnalysis)
async def analyze_document(
    file: Annotated[
        UploadFile,
        File(description="The PDF document to analyze."),
    ],
    extractor: Annotated[
        PdfTextExtractor,
        Depends(get_pdf_text_extractor),
    ],
    analyzer: Annotated[
        DocumentAnalyzer,
        Depends(get_document_analyzer),
    ],
) -> DocumentAnalysis:
    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Only PDF documents are supported",
        )

    pdf_content = await file.read()

    if len(pdf_content) > MAX_PDF_SIZE_BYTES:
        raise HTTPException(
            status_code=status.HTTP_413_CONTENT_TOO_LARGE,
            detail="The PDF must not be larger than 10 MB",
        )

    try:
        document_text = extractor.extract_text(pdf_content)
    except ValueError as exception:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
            detail=str(exception),
        ) from exception

    try:
        return analyzer.analyze_text(document_text)
    except DocumentAnalysisUnavailableError as exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=AI_UNAVAILABLE_MESSAGE,
            headers={"Retry-After": "30"},
        ) from exception
