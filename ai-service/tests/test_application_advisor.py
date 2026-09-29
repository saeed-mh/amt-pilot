from unittest.mock import Mock, patch

import pytest
from langchain_core.exceptions import ModelAPIError

from app.config import Settings
from app.exceptions import DocumentAnalysisUnavailableError
from app.schemas import (
    ApplicationAdvice,
    ApplicationAdviceDraft,
    ApplicationAdviceRequest,
    RequirementAssessment,
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
                "official_source_title": "Official registration guide",
                "official_source_url": "https://example.test/registration",
            }
        ],
        official_guide={
            "overview": "Register a new primary residence after moving.",
            "eligibility": "Residents aged 16 or older generally register themselves.",
            "steps": ["Collect the required documents.", "Complete the registration."],
            "deadline": "Register within two weeks after moving.",
            "fee": "The registration confirmation is free of charge.",
            "appointment_required": True,
            "appointment_information": "Book an appointment for an in-person visit.",
            "appointment_url": "https://example.test/appointments",
            "source_title": "Official registration guide",
            "source_url": "https://example.test/registration",
            "verified_at": "2026-09-27",
        },
        documents=[],
        user_answers={},
    )


def test_returns_structured_application_advice() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    draft = ApplicationAdviceDraft(
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
        official_guide_sections_used=["deadline"],
        disclaimer="Guidance only; not legal advice.",
    )
    structured_model.invoke.return_value = draft

    result = advisor.advise(application_request())

    assert result == ApplicationAdvice(
        readiness="ACTION_REQUIRED",
        summary="A valid identity document is still required.",
        requirement_assessments=[
            {
                "requirement_code": "IDENTITY_DOCUMENT",
                "status": "MISSING",
                "explanation": "No identity document was supplied.",
                "supporting_documents": [],
                "official_source_title": None,
                "official_source_url": None,
            }
        ],
        inconsistencies=[],
        next_steps=["Upload a valid identity document."],
        questions_for_user=[],
        official_source_references=[
            {
                "section": "deadline",
                "statements": ["Register within two weeks after moving."],
                "source_title": "Official registration guide",
                "source_url": "https://example.test/registration",
                "verified_at": "2026-09-27",
            }
        ],
        disclaimer="Guidance only; not legal advice.",
    )
    structured_model.invoke.assert_called_once()


def test_attaches_trusted_requirement_source_instead_of_model_generated_source() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    structured_model.invoke.return_value = ApplicationAdviceDraft(
        readiness="READY_TO_SUBMIT",
        summary="The requirement is satisfied.",
        requirement_assessments=[
            {
                "requirement_code": "LANDLORD_CONFIRMATION",
                "status": "SATISFIED",
                "explanation": "The confirmation was supplied.",
                "supporting_documents": ["confirmation.pdf"],
            }
        ],
        inconsistencies=[],
        next_steps=[],
        questions_for_user=[],
        official_guide_sections_used=["overview", "overview"],
        disclaimer="Guidance only; not legal advice.",
    )

    result = advisor.advise(application_request())

    assessment = result.requirement_assessments[0]
    assert assessment.official_source_title == "Official registration guide"
    assert assessment.official_source_url == "https://example.test/registration"
    assert len(result.official_source_references) == 1


def test_builds_source_references_only_from_retrieved_guide_chunks() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    request_data = application_request().model_dump()
    request_data["official_guide"] = None
    request_data["retrieved_guide_chunks"] = [
        {
            "section": "deadline",
            "content": "Register within two weeks after moving.",
            "source_title": "Official registration guide",
            "source_url": "https://example.test/registration",
            "verified_at": "2026-09-27",
        },
        {
            "section": "steps",
            "content": "Collect the required documents.",
            "source_title": "Official registration guide",
            "source_url": "https://example.test/registration",
            "verified_at": "2026-09-27",
        },
    ]
    structured_model.invoke.return_value = ApplicationAdviceDraft(
        readiness="ACTION_REQUIRED",
        summary="Complete the registration on time.",
        requirement_assessments=[],
        inconsistencies=[],
        next_steps=["Collect the required documents."],
        questions_for_user=[],
        official_guide_sections_used=["deadline"],
        disclaimer="Guidance only; not legal advice.",
    )

    result = advisor.advise(ApplicationAdviceRequest.model_validate(request_data))

    assert [reference.model_dump() for reference in result.official_source_references] == [
        {
            "section": "deadline",
            "statements": ["Register within two weeks after moving."],
            "source_title": "Official registration guide",
            "source_url": "https://example.test/registration",
            "verified_at": result.official_source_references[0].verified_at,
        }
    ]

    messages = structured_model.invoke.call_args.args[0]
    assert "retrieved_guide_chunks" in messages[1].content


def test_converts_model_failure_to_unavailable_error() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    structured_model.invoke.side_effect = ModelAPIError("provider unavailable")

    with pytest.raises(
        DocumentAnalysisUnavailableError,
        match="AI provider is temporarily unavailable",
    ):
        advisor.advise(application_request())


def test_supports_not_applicable_optional_requirements() -> None:
    assessment = RequirementAssessment(
        requirement_code="CIVIL_STATUS_DOCUMENTS",
        status="NOT_APPLICABLE",
        explanation="This optional requirement does not apply.",
        supporting_documents=[],
        official_source_title=None,
        official_source_url=None,
    )

    assert assessment.status == "NOT_APPLICABLE"


def test_invalid_sample_document_requires_action_and_clear_replacement_step() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    request_data = application_request().model_dump()
    request_data["documents"] = [
        {
            "original_filename": "sample_confirmation.pdf",
            "requirement_code": "LANDLORD_CONFIRMATION",
            "analysis": {
                "document_type": "Landlord confirmation",
                "primary_language": "de",
                "summary": "A sample confirmation.",
                "extracted_fields": [],
                "missing_or_unclear": [],
                "warnings": ["Synthetic test fixture; not an official document."],
            },
        }
    ]
    request = ApplicationAdviceRequest.model_validate(request_data)
    structured_model.invoke.return_value = ApplicationAdviceDraft(
        readiness="NEEDS_REVIEW",
        summary="The supplied document is synthetic.",
        requirement_assessments=[
            {
                "requirement_code": "LANDLORD_CONFIRMATION",
                "status": "NEEDS_REVIEW",
                "explanation": "The document is a sample.",
                "supporting_documents": ["sample_confirmation.pdf"],
            }
        ],
        inconsistencies=["The supplied document is synthetic."],
        next_steps=[
            "Provide a genuine landlord confirmation in sample_confirmation.pdf.",
            "Book an appointment after replacing the document.",
        ],
        questions_for_user=[],
        official_guide_sections_used=["steps"],
        disclaimer="Guidance only; not legal advice.",
    )

    result = advisor.advise(request)

    assert result.readiness == "ACTION_REQUIRED"
    assert result.next_steps == [
        ("Replace sample_confirmation.pdf with a valid, official Landlord confirmation."),
        "Book an appointment after replacing the document.",
    ]


def test_includes_saved_user_answers_in_model_context() -> None:
    advisor, structured_model = create_advisor_with_fake_model()
    request_data = application_request().model_dump()
    request_data["user_answers"] = {
        "Are civil status documents relevant?": "No, I am registering alone."
    }
    request = ApplicationAdviceRequest.model_validate(request_data)
    structured_model.invoke.return_value = ApplicationAdviceDraft(
        readiness="ACTION_REQUIRED",
        summary="More information is required.",
        requirement_assessments=[],
        inconsistencies=[],
        next_steps=[],
        questions_for_user=[],
        official_guide_sections_used=[],
        disclaimer="Guidance only; not legal advice.",
    )

    advisor.advise(request)

    messages = structured_model.invoke.call_args.args[0]
    assert "visibly acknowledge every relevant answer" in messages[0].content
    assert "Are civil status documents relevant?" in messages[1].content
    assert "No, I am registering alone." in messages[1].content
