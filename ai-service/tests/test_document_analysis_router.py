from unittest.mock import Mock

from fastapi.testclient import TestClient

from app.exceptions import DocumentAnalysisUnavailableError
from app.main import app
from app.routers.document_analysis import (
    AI_UNAVAILABLE_MESSAGE,
    MAX_PDF_SIZE_BYTES,
    get_document_analyzer,
    get_pdf_text_extractor,
)
from app.schemas import DocumentAnalysis
from app.services.document_analyzer import DocumentAnalyzer
from app.services.pdf_text_extractor import PdfTextExtractor


def test_analyzes_uploaded_pdf() -> None:
    extractor = Mock(spec=PdfTextExtractor)
    extractor.extract_text.return_value = "--- Page 1 ---\nName: Max Mustermann"

    expected_analysis = DocumentAnalysis(
        document_type="Registration certificate",
        primary_language="German",
        summary="A registration certificate for Max Mustermann.",
        extracted_fields=[
            {
                "name": "name",
                "value": "Max Mustermann",
                "evidence": "Name: Max Mustermann",
                "page_number": 1,
            }
        ],
        missing_or_unclear=[],
        warnings=[],
    )

    analyzer = Mock(spec=DocumentAnalyzer)
    analyzer.analyze_text.return_value = expected_analysis

    app.dependency_overrides[get_pdf_text_extractor] = lambda: extractor
    app.dependency_overrides[get_document_analyzer] = lambda: analyzer

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/documents/analyze",
                files={
                    "file": (
                        "registration.pdf",
                        b"fake-pdf-content",
                        "application/pdf",
                    )
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == expected_analysis.model_dump()
    extractor.extract_text.assert_called_once_with(b"fake-pdf-content")
    analyzer.analyze_text.assert_called_once_with(
        "--- Page 1 ---\nName: Max Mustermann"
    )


def test_rejects_non_pdf_upload() -> None:
    extractor = Mock(spec=PdfTextExtractor)
    analyzer = Mock(spec=DocumentAnalyzer)

    app.dependency_overrides[get_pdf_text_extractor] = lambda: extractor
    app.dependency_overrides[get_document_analyzer] = lambda: analyzer

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/documents/analyze",
                files={
                    "file": (
                        "notes.txt",
                        b"This is not a PDF",
                        "text/plain",
                    )
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 415
    assert response.json() == {"detail": "Only PDF documents are supported"}
    extractor.extract_text.assert_not_called()
    analyzer.analyze_text.assert_not_called()


def test_rejects_pdf_without_readable_text() -> None:
    extractor = Mock(spec=PdfTextExtractor)
    extractor.extract_text.side_effect = ValueError(
        "The PDF does not contain readable text"
    )

    analyzer = Mock(spec=DocumentAnalyzer)

    app.dependency_overrides[get_pdf_text_extractor] = lambda: extractor
    app.dependency_overrides[get_document_analyzer] = lambda: analyzer

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/documents/analyze",
                files={
                    "file": (
                        "scanned-document.pdf",
                        b"image-only-pdf",
                        "application/pdf",
                    )
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 422
    assert response.json() == {"detail": "The PDF does not contain readable text"}
    extractor.extract_text.assert_called_once_with(b"image-only-pdf")
    analyzer.analyze_text.assert_not_called()


def test_rejects_pdf_larger_than_limit() -> None:
    extractor = Mock(spec=PdfTextExtractor)
    analyzer = Mock(spec=DocumentAnalyzer)

    app.dependency_overrides[get_pdf_text_extractor] = lambda: extractor
    app.dependency_overrides[get_document_analyzer] = lambda: analyzer

    oversized_content = b"x" * (MAX_PDF_SIZE_BYTES + 1)

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/documents/analyze",
                files={
                    "file": (
                        "large-document.pdf",
                        oversized_content,
                        "application/pdf",
                    )
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 413
    assert response.json() == {"detail": "The PDF must not be larger than 10 MB"}
    extractor.extract_text.assert_not_called()
    analyzer.analyze_text.assert_not_called()


def test_returns_service_unavailable_when_ai_provider_fails() -> None:
    extractor = Mock(spec=PdfTextExtractor)
    extractor.extract_text.return_value = "--- Page 1 ---\nDocument text"

    analyzer = Mock(spec=DocumentAnalyzer)
    analyzer.analyze_text.side_effect = DocumentAnalysisUnavailableError(
        "provider unavailable"
    )

    app.dependency_overrides[get_pdf_text_extractor] = lambda: extractor
    app.dependency_overrides[get_document_analyzer] = lambda: analyzer

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/documents/analyze",
                files={
                    "file": (
                        "document.pdf",
                        b"fake-pdf-content",
                        "application/pdf",
                    )
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 503
    assert response.json() == {"detail": AI_UNAVAILABLE_MESSAGE}
    assert response.headers["retry-after"] == "30"
    extractor.extract_text.assert_called_once_with(b"fake-pdf-content")
    analyzer.analyze_text.assert_called_once_with(
        "--- Page 1 ---\nDocument text"
    )
