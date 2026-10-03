import subprocess
from pathlib import Path
from unittest.mock import Mock, patch

import pytest
from pypdf.errors import PdfReadError

from app.exceptions import OcrUnavailableError
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
        result = PdfTextExtractor(min_text_characters=1).extract_text(b"fake-pdf-content")

    assert result == ("--- Page 1 ---\nFirst page text\n\n--- Page 3 ---\nThird page text")


def test_rejects_encrypted_pdf() -> None:
    reader = Mock()
    reader.is_encrypted = True

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            return_value=reader,
        ),
        pytest.raises(ValueError, match="Encrypted PDFs are not supported"),
    ):
        PdfTextExtractor().extract_text(b"encrypted-pdf")


def test_rejects_unreadable_pdf() -> None:
    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            side_effect=PdfReadError("Invalid PDF"),
        ),
        pytest.raises(
            ValueError,
            match="The uploaded file is not a readable PDF",
        ),
    ):
        PdfTextExtractor().extract_text(b"invalid-pdf")


def test_uses_ocr_for_image_only_pdf() -> None:
    scanned_page = Mock()
    scanned_page.extract_text.return_value = "   "
    scanned_page.images = [Mock()]

    native_reader = Mock()
    native_reader.is_encrypted = False
    native_reader.pages = [scanned_page]

    ocr_page = Mock()
    ocr_page.extract_text.return_value = "Name: Max Mustermann"

    ocr_reader = Mock()
    ocr_reader.is_encrypted = False
    ocr_reader.pages = [ocr_page]

    def complete_ocr(command: list[str], **_: object) -> subprocess.CompletedProcess[str]:
        Path(command[-1]).write_bytes(b"ocr-pdf")
        return subprocess.CompletedProcess(command, 0, "", "")

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            side_effect=[native_reader, ocr_reader],
        ),
        patch.object(PdfTextExtractor, "_find_ocr_executable", return_value="ocrmypdf"),
        patch(
            "app.services.pdf_text_extractor.subprocess.run",
            side_effect=complete_ocr,
        ) as run_ocr,
    ):
        result = PdfTextExtractor(
            min_text_characters=1,
            ocr_tessdata_prefix="C:/ocr/tessdata",
        ).extract_text(b"image-only-pdf")

    assert result == "--- Page 1 ---\nName: Max Mustermann"
    command = run_ocr.call_args.args[0]
    assert "--force-ocr" in command
    assert command[command.index("--language") + 1] == "deu+eng"
    assert run_ocr.call_args.kwargs["env"]["TESSDATA_PREFIX"] == "C:/ocr/tessdata"


def test_merges_native_text_with_ocr_text_by_page() -> None:
    native_text_page = Mock()
    native_text_page.extract_text.return_value = "Native text must remain unchanged on this page."

    scanned_page = Mock()
    scanned_page.extract_text.return_value = ""
    scanned_page.images = [Mock()]

    native_reader = Mock()
    native_reader.is_encrypted = False
    native_reader.pages = [native_text_page, scanned_page]

    replaced_native_page = Mock()
    replaced_native_page.extract_text.return_value = "OCR text that must not replace native text."

    ocr_scanned_page = Mock()
    ocr_scanned_page.extract_text.return_value = "Scanned page recovered by OCR."

    ocr_reader = Mock()
    ocr_reader.is_encrypted = False
    ocr_reader.pages = [replaced_native_page, ocr_scanned_page]

    def complete_ocr(command: list[str], **_: object) -> subprocess.CompletedProcess[str]:
        Path(command[-1]).write_bytes(b"ocr-pdf")
        return subprocess.CompletedProcess(command, 0, "", "")

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            side_effect=[native_reader, ocr_reader],
        ),
        patch.object(PdfTextExtractor, "_find_ocr_executable", return_value="ocrmypdf"),
        patch(
            "app.services.pdf_text_extractor.subprocess.run",
            side_effect=complete_ocr,
        ),
    ):
        result = PdfTextExtractor().extract_text(b"mixed-pdf")

    assert result == (
        "--- Page 1 ---\nNative text must remain unchanged on this page.\n\n"
        "--- Page 2 ---\nScanned page recovered by OCR."
    )


def test_reports_when_ocr_is_not_installed() -> None:
    page = Mock()
    page.extract_text.return_value = ""
    page.images = [Mock()]

    reader = Mock()
    reader.is_encrypted = False
    reader.pages = [page]

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            return_value=reader,
        ),
        patch.object(PdfTextExtractor, "_find_ocr_executable", return_value=None),
        pytest.raises(OcrUnavailableError, match="OCR is not installed"),
    ):
        PdfTextExtractor().extract_text(b"image-only-pdf")


def test_rejects_image_only_pdf_when_ocr_is_disabled() -> None:
    page = Mock()
    page.extract_text.return_value = ""
    page.images = [Mock()]

    reader = Mock()
    reader.is_encrypted = False
    reader.pages = [page]

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            return_value=reader,
        ),
        pytest.raises(ValueError, match="OCR is disabled"),
    ):
        PdfTextExtractor(ocr_enabled=False).extract_text(b"image-only-pdf")


def test_rejects_scanned_pdf_over_ocr_page_limit() -> None:
    pages = []

    for _ in range(2):
        page = Mock()
        page.extract_text.return_value = ""
        page.images = [Mock()]
        pages.append(page)

    reader = Mock()
    reader.is_encrypted = False
    reader.pages = pages

    with (
        patch(
            "app.services.pdf_text_extractor.PdfReader",
            return_value=reader,
        ),
        pytest.raises(ValueError, match="must not contain more than 1 pages"),
    ):
        PdfTextExtractor(ocr_max_pages=1).extract_text(b"large-scanned-pdf")
