from io import BytesIO

from pypdf import PdfReader
from pypdf.errors import PdfReadError


class PdfTextExtractor:
    def extract_text(self, pdf_content: bytes) -> str:
        if not pdf_content:
            raise ValueError("PDF content must not be empty")

        try:
            reader = PdfReader(BytesIO(pdf_content))

            if reader.is_encrypted:
                raise ValueError("Encrypted PDFs are not supported")

            extracted_pages = []

            for page_number, page in enumerate(reader.pages, start=1):
                page_text = (page.extract_text() or "").strip()

                if page_text:
                    extracted_pages.append(f"--- Page {page_number} ---\n{page_text}")

        except PdfReadError as exception:
            raise ValueError("The uploaded file is not a readable PDF") from exception

        extracted_text = "\n\n".join(extracted_pages).strip()

        if not extracted_text:
            raise ValueError("The PDF does not contain readable text")

        return extracted_text
