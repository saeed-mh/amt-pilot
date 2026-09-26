from unittest.mock import Mock

from fastapi.testclient import TestClient

from app.exceptions import DocumentAnalysisUnavailableError
from app.main import app
from app.routers.application_advice import (
    AI_UNAVAILABLE_MESSAGE,
    get_application_advisor,
)
from app.schemas import ApplicationAdvice
from app.services.application_advisor import ApplicationAdvisor


def request_body() -> dict:
    return {
        "process_code": "ADDRESS_REGISTRATION",
        "process_title": "Address Registration",
        "city": "Dortmund",
        "requirements": [
            {
                "code": "LANDLORD_CONFIRMATION",
                "title": "Landlord confirmation",
                "required": True,
                "completed": True,
                "official_source_url": "https://example.test/registration",
            }
        ],
        "documents": [],
    }


def test_returns_application_advice() -> None:
    advisor = Mock(spec=ApplicationAdvisor)
    advice = ApplicationAdvice(
        readiness="ACTION_REQUIRED",
        summary="More information is required.",
        requirement_assessments=[],
        inconsistencies=[],
        next_steps=["Upload the missing document."],
        questions_for_user=[],
        disclaimer="Guidance only; not legal advice.",
    )
    advisor.advise.return_value = advice
    app.dependency_overrides[get_application_advisor] = lambda: advisor

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/applications/advise",
                json=request_body(),
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 200
    assert response.json() == advice.model_dump()
    advisor.advise.assert_called_once()


def test_returns_service_unavailable_when_provider_fails() -> None:
    advisor = Mock(spec=ApplicationAdvisor)
    advisor.advise.side_effect = DocumentAnalysisUnavailableError(
        "provider unavailable"
    )
    app.dependency_overrides[get_application_advisor] = lambda: advisor

    try:
        with TestClient(app) as client:
            response = client.post(
                "/api/v1/applications/advise",
                json=request_body(),
            )
    finally:
        app.dependency_overrides.clear()

    assert response.status_code == 503
    assert response.json() == {"detail": AI_UNAVAILABLE_MESSAGE}
    assert response.headers["retry-after"] == "30"
