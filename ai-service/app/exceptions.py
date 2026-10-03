class DocumentAnalysisUnavailableError(RuntimeError):
    """Raised when the external AI model cannot complete an analysis."""


class EmbeddingUnavailableError(RuntimeError):
    """Raised when the external embedding model cannot create vectors."""


class OcrUnavailableError(RuntimeError):
    """Raised when a scanned PDF needs OCR but the OCR runtime is unavailable."""
