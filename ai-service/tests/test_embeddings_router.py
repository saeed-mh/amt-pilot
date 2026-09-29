from unittest.mock import Mock

from fastapi.testclient import TestClient

from app.exceptions import EmbeddingUnavailableError
from app.main import app
from app.routers.embeddings import (
    EMBEDDING_UNAVAILABLE_MESSAGE,
    get_embedding_service,
)
from app.schemas import EmbeddingResponse
from app.services.embedding_service import EmbeddingService


def test_returns_embeddings() -> None:
    service = Mock(spec=EmbeddingService)
    service.embed.return_value = EmbeddingResponse(
        model="test-model",
        dimension=3,
        embeddings=[[1.0, 0.0, 0.0]],
    )
    app.dependency_overrides[get_embedding_service] = lambda: service

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/embeddings",
                json={
                    "texts": ["Registration deadline"],
                    "task_type": "RETRIEVAL_QUERY",
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == {
        "model": "test-model",
        "dimension": 3,
        "embeddings": [[1.0, 0.0, 0.0]],
    }
    service.embed.assert_called_once()


def test_returns_service_unavailable_when_provider_fails() -> None:
    service = Mock(spec=EmbeddingService)
    service.embed.side_effect = EmbeddingUnavailableError("provider unavailable")
    app.dependency_overrides[get_embedding_service] = lambda: service

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/embeddings",
                json={
                    "texts": ["Registration deadline"],
                    "task_type": "RETRIEVAL_QUERY",
                },
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 503
    assert response.json() == {"detail": EMBEDDING_UNAVAILABLE_MESSAGE}
    assert response.headers["retry-after"] == "30"
