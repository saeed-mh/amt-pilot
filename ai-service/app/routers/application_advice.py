from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status

from app.exceptions import DocumentAnalysisUnavailableError
from app.schemas import ApplicationAdvice, ApplicationAdviceRequest
from app.services.application_advisor import ApplicationAdvisor

AI_UNAVAILABLE_MESSAGE = (
    "AI application advice is temporarily unavailable. Please try again shortly."
)

router = APIRouter(
    prefix="/api/v1/applications",
    tags=["application advice"],
)


def get_application_advisor() -> ApplicationAdvisor:
    return ApplicationAdvisor()


@router.post("/advise", response_model=ApplicationAdvice)
def advise_application(
    request: ApplicationAdviceRequest,
    advisor: Annotated[
        ApplicationAdvisor,
        Depends(get_application_advisor),
    ],
) -> ApplicationAdvice:
    try:
        return advisor.advise(request)
    except DocumentAnalysisUnavailableError as exception:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=AI_UNAVAILABLE_MESSAGE,
            headers={"Retry-After": "30"},
        ) from exception
