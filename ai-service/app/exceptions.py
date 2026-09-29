class DocumentAnalysisUnavailableError(RuntimeError):
    """Raised when the external AI model cannot complete an analysis."""


class EmbeddingUnavailableError(RuntimeError):
    """Raised when the external embedding model cannot create vectors."""
