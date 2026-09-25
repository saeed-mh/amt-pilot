from unittest.mock import Mock, patch

import pytest
from pypdf.errors import PdfReadError

from app.services.pdf_text_extractor import PdfTextExtractor


def test_rejects_empty_pdf_content() -> None:
    extractor = PdfTextExtractor()

    with pytest.raises(ValueError, match="PDF content must not be empty"):
        extractor.extract_text(b"")


def test_extracts_text_and_preserves_page_numbers() -> None:
    first_page = Mock()
    first_page.extract_text.return_value = "First page text"

    empty_page = Mock()
    empty_page.extract_text.return_value = None

    third_page = Mock()
    third_page.extract_text.return_value = "Third page text"

    reader = Mock()
    reader.is_encrypted = False
    reader.pages = [first_page, empty_page, third_page]

    with patch(
        "app.services.pdf_text_extractor.PdfReader",
        return_value=reader,
    ):
        result = PdfTextExtractor().extract_text(b"fake-pdf-content")

    assert result == (
        "--- Page 1 ---\nFirst page text\n\n"
        "--- Page 3 ---\nThird page text"
    )


def test_rejects_encrypted_pdf() -> None:
    reader = Mock()
    reader.is_encrypted = True

    with patch(
        "app.services.pdf_text_extractor.PdfReader",
        return_value=reader,
    ), pytest.raises(ValueError, match="Encrypted PDFs are not supported"):
        PdfTextExtractor().extract_text(b"encrypted-pdf")


def test_rejects_unreadable_pdf() -> None:
    with patch(
        "app.services.pdf_text_extractor.PdfReader",
        side_effect=PdfReadError("Invalid PDF"),
    ), pytest.raises(
        ValueError,
        match="The uploaded file is not a readable PDF",
    ):
        PdfTextExtractor().extract_text(b"invalid-pdf")


def test_rejects_pdf_without_readable_text() -> None:
    page = Mock()
    page.extract_text.return_value = "   "

    reader = Mock()
    reader.is_encrypted = False
    reader.pages = [page]

    with patch(
        "app.services.pdf_text_extractor.PdfReader",
        return_value=reader,
    ), pytest.raises(
        ValueError,
        match="The PDF does not contain readable text",
    ):
        PdfTextExtractor().extract_text(b"image-only-pdf")
