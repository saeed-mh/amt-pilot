from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status

from app.exceptions import EmbeddingUnavailableError
from app.schemas import EmbeddingRequest, EmbeddingResponse
from app.services.embedding_service import EmbeddingService

EMBEDDING_UNAVAILABLE_MESSAGE = (
    "AI embeddings are temporarily unavailable. Please try again shortly."
)

router = APIRouter(
    prefix="/api/v1/embeddings",
    tags=["embeddings"],
)


def get_embedding_service() -> EmbeddingService:
    return EmbeddingService()


@router.post("", response_model=EmbeddingResponse)
def create_embeddings(
    request: EmbeddingRequest,
    service: Annotated[
        EmbeddingService,
        Depends(get_embedding_service),
    ],
) -> EmbeddingResponse:
    try:
        return service.embed(request)
    except ValueError as exception:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_CONTENT,
            detail=str(exception),
        ) from exception
    except EmbeddingUnavailableError as exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=EMBEDDING_UNAVAILABLE_MESSAGE,
            headers={"Retry-After": "30"},
        ) from exception
