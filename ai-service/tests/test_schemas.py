import pytest
from pydantic import ValidationError

from app.schemas import DocumentAnalysis


def test_creates_structured_document_analysis() -> None:
    analysis = DocumentAnalysis(
        document_type="Passport",
        primary_language="German",
        summary="A valid passport document.",
        extracted_fields=[
            {
                "name": "expiry_date",
                "value": "2030-05-12",
                "evidence": "Gültig bis 12.05.2030",
                "page_number": 1,
            }
        ],
        missing_or_unclear=[],
        warnings=[],
    )

    assert analysis.document_type == "Passport"
    assert analysis.extracted_fields[0].name == "expiry_date"
    assert analysis.extracted_fields[0].page_number == 1


def test_rejects_unexpected_analysis_fields() -> None:
    with pytest.raises(ValidationError):
        DocumentAnalysis(
            document_type="Passport",
            primary_language="German",
            summary="A valid passport document.",
            extracted_fields=[],
            missing_or_unclear=[],
            warnings=[],
            unexpected_field="not allowed",
        )
