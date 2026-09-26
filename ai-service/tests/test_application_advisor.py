from unittest.mock import Mock, patch

import pytest
from langchain_core.exceptions import ModelAPIError

from app.config import Settings
from app.exceptions import DocumentAnalysisUnavailableError
from app.schemas import (
    ApplicationAdvice,
    ApplicationAdviceRequest,
)
from app.services.application_advisor import ApplicationAdvisor


def create_advisor_with_fake_model() -> tuple[ApplicationAdvisor, Mock]:
    settings = Settings(
        google_api_key="test-api-key",
        google_model="test-model",
    )
    structured_model = Mock()

    with patch("app.services.application_advisor.ChatGoogleGenerativeAI") as model_class:
        model_class.return_value.with_structured_output.return_value = structured_model
        advisor = ApplicationAdvisor(settings)

    return advisor, structured_model


def application_request() -> ApplicationAdviceRequest:
    return ApplicationAdviceRequest(
        process_code="ADDRESS_REGISTRATION",
        process_title="Address Registration",
        city="Dortmund",
        requirements=[
            {
                "code": "LANDLORD_CONFIRMATION",
                "title": "Landlord confirmation",
                "required": True,
                "completed": True,
                "official_source_url": "https://example.test/registration",
            }
        ],
        documents=[],
    )


def test_returns_structured_application_advice() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    expected = ApplicationAdvice(
        readiness="ACTION_REQUIRED",
        summary="A valid identity document is still required.",
        requirement_assessments=[
            {
                "requirement_code": "IDENTITY_DOCUMENT",
                "status": "MISSING",
                "explanation": "No identity document was supplied.",
                "supporting_documents": [],
            }
        ],
        inconsistencies=[],
        next_steps=["Upload a valid identity document."],
        questions_for_user=[],
        disclaimer="Guidance only; not legal advice.",
    )
    structured_model.invoke.return_value = expected

    result = advisor.advise(application_request())

    assert result == expected
    structured_model.invoke.assert_called_once()


def test_converts_model_failure_to_unavailable_error() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    structured_model.invoke.side_effect = ModelAPIError("provider unavailable")

    with pytest.raises(
        DocumentAnalysisUnavailableError,
        match="AI provider is temporarily unavailable",
    ):
        advisor.advise(application_request())
