from math import sqrt
from unittest.mock import Mock, patch

import pytest

from app.config import Settings
from app.exceptions import EmbeddingUnavailableError
from app.schemas import EmbeddingRequest
from app.services.embedding_service import EmbeddingService


def create_service_with_fake_embeddings() -> tuple[EmbeddingService, Mock]:
    settings = Settings(
        google_api_key="test-api-key",
        google_embedding_model="test-embedding-model",
        embedding_dimension=3,
    )
    embeddings = Mock()

    with patch("app.services.embedding_service.GoogleGenerativeAIEmbeddings") as embeddings_class:
        embeddings_class.return_value = embeddings
        service = EmbeddingService(settings)

    return service, embeddings


def test_embeds_and_normalizes_retrieval_documents() -> None:
    service, embeddings = create_service_with_fake_embeddings()
    embeddings.embed_documents.return_value = [[3.0, 4.0, 0.0]]

    result = service.embed(
        EmbeddingRequest(
            texts=["Official registration deadline"],
            task_type="RETRIEVAL_DOCUMENT",
        )
    )

    assert result.model == "test-embedding-model"
    assert result.dimension == 3
    assert result.embeddings == [[0.6, 0.8, 0.0]]
    embeddings.embed_documents.assert_called_once_with(
        ["Official registration deadline"],
        task_type="RETRIEVAL_DOCUMENT",
        output_dimensionality=3,
    )


def test_embeds_and_normalizes_retrieval_queries() -> None:
    service, embeddings = create_service_with_fake_embeddings()
    embeddings.embed_query.return_value = [1.0, 1.0, 0.0]

    result = service.embed(
        EmbeddingRequest(
            texts=["When must I register?"],
            task_type="RETRIEVAL_QUERY",
        )
    )

    expected = 1 / sqrt(2)
    assert result.embeddings[0] == pytest.approx([expected, expected, 0.0])
    embeddings.embed_query.assert_called_once_with(
        "When must I register?",
        task_type="RETRIEVAL_QUERY",
        output_dimensionality=3,
    )


def test_rejects_blank_text() -> None:
    service, embeddings = create_service_with_fake_embeddings()

    with pytest.raises(ValueError, match="must not be empty"):
        service.embed(
            EmbeddingRequest(
                texts=["   "],
                task_type="RETRIEVAL_QUERY",
            )
        )

    embeddings.embed_query.assert_not_called()


def test_converts_provider_failure_to_unavailable_error() -> None:
    service, embeddings = create_service_with_fake_embeddings()
    embeddings.embed_query.side_effect = RuntimeError("provider unavailable")

    with pytest.raises(
        EmbeddingUnavailableError,
        match="temporarily unavailable",
    ):
        service.embed(
            EmbeddingRequest(
                texts=["registration"],
                task_type="RETRIEVAL_QUERY",
            )
        )
