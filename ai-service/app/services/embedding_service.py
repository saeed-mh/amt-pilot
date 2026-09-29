from math import sqrt

from langchain_google_genai import GoogleGenerativeAIEmbeddings

from app.config import Settings, get_settings
from app.exceptions import EmbeddingUnavailableError
from app.schemas import EmbeddingRequest, EmbeddingResponse


class EmbeddingService:
    def __init__(self, settings: Settings | None = None) -> None:
        self.settings = settings or get_settings()
        self.embeddings = GoogleGenerativeAIEmbeddings(
            model=self.settings.google_embedding_model,
            api_key=self.settings.google_api_key,
            output_dimensionality=self.settings.embedding_dimension,
        )

    def embed(self, request: EmbeddingRequest) -> EmbeddingResponse:
        texts = [text.strip() for text in request.texts]
        if any(not text for text in texts):
            raise ValueError("Embedding text must not be empty")

        try:
            if request.task_type == "RETRIEVAL_DOCUMENT":
                vectors = self.embeddings.embed_documents(
                    texts,
                    task_type=request.task_type,
                    output_dimensionality=self.settings.embedding_dimension,
                )
            else:
                vectors = [
                    self.embeddings.embed_query(
                        text,
                        task_type=request.task_type,
                        output_dimensionality=self.settings.embedding_dimension,
                    )
                    for text in texts
                ]
        except Exception as exception:
            raise EmbeddingUnavailableError(
                "Embedding provider is temporarily unavailable"
            ) from exception

        normalized = [self._normalize(vector) for vector in vectors]
        if any(len(vector) != self.settings.embedding_dimension for vector in normalized):
            raise EmbeddingUnavailableError(
                "Embedding provider returned an unexpected vector dimension"
            )

        return EmbeddingResponse(
            model=self.settings.google_embedding_model,
            dimension=self.settings.embedding_dimension,
            embeddings=normalized,
        )

    @staticmethod
    def _normalize(vector: list[float]) -> list[float]:
        magnitude = sqrt(sum(value * value for value in vector))
        if magnitude == 0:
            raise EmbeddingUnavailableError("Embedding provider returned a zero-length vector")
        return [value / magnitude for value in vector]
