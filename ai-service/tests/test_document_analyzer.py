from unittest.mock import Mock, patch

import pytest

from app.config import Settings
from app.schemas import DocumentAnalysis
from app.services.document_analyzer import DocumentAnalyzer


def create_analyzer_with_fake_model() -> tuple[DocumentAnalyzer, Mock]:
    settings = Settings(
        google_api_key="test-api-key",
        google_model="test-model",
    )
    structured_model = Mock()

    with patch("app.services.document_analyzer.ChatGoogleGenerativeAI") as model_class:
        model_class.return_value.with_structured_output.return_value = structured_model
        analyzer = DocumentAnalyzer(settings)

    return analyzer, structured_model


def test_rejects_empty_document_without_calling_model() -> None:
    analyzer, structured_model = create_analyzer_with_fake_model()

    with pytest.raises(ValueError, match="Document text must not be empty"):
        analyzer.analyze_text("   ")

    structured_model.invoke.assert_not_called()


def test_returns_structured_document_analysis() -> None:
    analyzer, structured_model = create_analyzer_with_fake_model()
    expected = DocumentAnalysis(
        document_type="Registration certificate",
        primary_language="German",
        summary="A registration certificate for a Dortmund address.",
        extracted_fields=[],
        missing_or_unclear=[],
        warnings=[],
    )
    structured_model.invoke.return_value = expected

    result = analyzer.analyze_text("Meldebescheinigung für eine Adresse in Dortmund")

    assert result == expected
    structured_model.invoke.assert_called_once()
