import os
import shutil
import subprocess
import sys
from dataclasses import dataclass
from io import BytesIO
from pathlib import Path
from tempfile import TemporaryDirectory

from pypdf import PdfReader
from pypdf.errors import PdfReadError

from app.exceptions import OcrUnavailableError


@dataclass(frozen=True)
class _PdfPage:
    text: str
    has_images: bool


class PdfTextExtractor:
    def __init__(
        self,
        *,
        ocr_enabled: bool = True,
        ocr_command: str = "ocrmypdf",
        ocr_languages: str = "deu+eng",
        ocr_tessdata_prefix: str | None = None,
        ocr_timeout_seconds: int = 120,
        ocr_max_pages: int = 20,
        min_text_characters: int = 20,
    ) -> None:
        self.ocr_enabled = ocr_enabled
        self.ocr_command = ocr_command
        self.ocr_languages = ocr_languages
        self.ocr_tessdata_prefix = ocr_tessdata_prefix
        self.ocr_timeout_seconds = ocr_timeout_seconds
        self.ocr_max_pages = ocr_max_pages
        self.min_text_characters = min_text_characters

    def extract_text(self, pdf_content: bytes) -> str:
        if not pdf_content:
            raise ValueError("PDF content must not be empty")

        native_pages = self._extract_pages(pdf_content)

        if not native_pages:
            raise ValueError("The PDF does not contain any pages")

        pages_needing_ocr = [
            page_number
            for page_number, page in enumerate(native_pages, start=1)
            if page.has_images and not self._has_readable_text(page.text)
        ]

        if not pages_needing_ocr:
            extracted_text = self._format_pages(native_pages)

            if not extracted_text:
                raise ValueError("The PDF does not contain readable text or scanned page images")

            return extracted_text

        if not self.ocr_enabled:
            extracted_text = self._format_pages(native_pages)

            if not extracted_text:
                raise ValueError("The PDF does not contain readable text and OCR is disabled")

            return extracted_text

        if len(native_pages) > self.ocr_max_pages:
            raise ValueError(
                f"Scanned PDFs must not contain more than {self.ocr_max_pages} pages"
            )

        ocr_content = self._run_ocr(pdf_content)
        ocr_pages = self._extract_pages(ocr_content)

        merged_pages = [
            native_page
            if self._has_readable_text(native_page.text)
            else _PdfPage(
                text=self._page_text_at(ocr_pages, page_index),
                has_images=native_page.has_images,
            )
            for page_index, native_page in enumerate(native_pages)
        ]

        return self._format_or_reject(merged_pages)

    def _extract_pages(self, pdf_content: bytes) -> list[_PdfPage]:
        try:
            reader = PdfReader(BytesIO(pdf_content))

            if reader.is_encrypted:
                raise ValueError("Encrypted PDFs are not supported")

            return [
                _PdfPage(
                    text=(page.extract_text() or "").strip(),
                    has_images=self._page_has_images(page),
                )
                for page in reader.pages
            ]

        except PdfReadError as exception:
            raise ValueError("The uploaded file is not a readable PDF") from exception

    def _run_ocr(self, pdf_content: bytes) -> bytes:
        executable = self._find_ocr_executable()

        if executable is None:
            raise OcrUnavailableError(
                "OCR is not installed. Install OCRmyPDF, Tesseract, and the German and English "
                "language data."
            )

        with TemporaryDirectory(prefix="amtpilot-ocr-") as temporary_directory:
            input_path = Path(temporary_directory) / "input.pdf"
            output_path = Path(temporary_directory) / "output.pdf"
            input_path.write_bytes(pdf_content)

            command = [
                executable,
                "--force-ocr",
                "--rotate-pages",
                "--deskew",
                "--jobs",
                "1",
                "--language",
                self.ocr_languages,
                "--output-type",
                "pdf",
                "--quiet",
                str(input_path),
                str(output_path),
            ]

            try:
                process_environment = os.environ.copy()

                if self.ocr_tessdata_prefix:
                    process_environment["TESSDATA_PREFIX"] = self.ocr_tessdata_prefix

                completed_process = subprocess.run(
                    command,
                    capture_output=True,
                    env=process_environment,
                    text=True,
                    timeout=self.ocr_timeout_seconds,
                    check=False,
                )
            except (OSError, subprocess.TimeoutExpired) as exception:
                raise OcrUnavailableError("OCR processing is temporarily unavailable") from exception

            if completed_process.returncode != 0 or not output_path.is_file():
                error_message = completed_process.stderr.lower()

                if any(
                    dependency in error_message
                    for dependency in ("tesseract", "ghostscript", "language", "not found")
                ):
                    raise OcrUnavailableError(
                        "OCR dependencies or German and English language data are unavailable"
                    )

                raise ValueError("The scanned PDF could not be processed with OCR")

            return output_path.read_bytes()

    def _find_ocr_executable(self) -> str | None:
        executable = shutil.which(self.ocr_command)

        if executable:
            return executable

        executable_name = "ocrmypdf.exe" if os.name == "nt" else "ocrmypdf"
        virtual_environment_executable = Path(sys.executable).with_name(executable_name)

        if virtual_environment_executable.is_file():
            return str(virtual_environment_executable)

        return None

    def _has_readable_text(self, page_text: str) -> bool:
        return sum(character.isalnum() for character in page_text) >= self.min_text_characters

    @staticmethod
    def _page_has_images(page: object) -> bool:
        try:
            return len(page.images) > 0  # type: ignore[attr-defined]
        except (AttributeError, KeyError, TypeError):
            return False

    @staticmethod
    def _page_text_at(pages: list[_PdfPage], page_index: int) -> str:
        return pages[page_index].text if page_index < len(pages) else ""

    @staticmethod
    def _format_pages(pages: list[_PdfPage]) -> str:
        return "\n\n".join(
            f"--- Page {page_number} ---\n{page.text}"
            for page_number, page in enumerate(pages, start=1)
            if page.text
        ).strip()

    def _format_or_reject(self, pages: list[_PdfPage]) -> str:
        extracted_text = self._format_pages(pages)

        if not extracted_text:
            raise ValueError("The PDF does not contain readable text, even after OCR")

        return extracted_text
