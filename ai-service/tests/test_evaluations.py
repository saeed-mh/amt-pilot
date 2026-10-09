from pathlib import Path

from app.schemas import ApplicationAdvice
from evals.dataset import load_dataset
from evals.scorer import build_report, evaluate_case

DATASET_PATH = (
    Path(__file__).resolve().parents[1] / "evals" / "datasets" / "address_registration.json"
)


def _expected_advice(case) -> ApplicationAdvice:
    references = []
    for section in case.expected.required_reference_sections:
        chunk = next(
            chunk for chunk in case.request.retrieved_guide_chunks if chunk.section == section
        )
        references.append(
            {
                "section": chunk.section,
                "statements": [chunk.content],
                "source_title": chunk.source_title,
                "source_url": chunk.source_url,
                "verified_at": chunk.verified_at,
            }
        )

    summary = " ".join(
        ["Expected synthetic evaluation result.", *case.expected.required_guidance_terms]
    )
    readiness = case.expected.accepted_readiness[0]
    return ApplicationAdvice(
        readiness=readiness,
        summary=summary,
        requirement_assessments=[
            {
                "requirement_code": code,
                "status": status,
                "explanation": f"Expected {status} result.",
                "supporting_documents": [],
                "official_source_title": "Wohnsitz in Dortmund anmelden",
                "official_source_url": ("https://www.dortmund.de/services/wohnsitzanmeldung.html"),
            }
            for code, accepted_statuses in case.expected.requirement_statuses.items()
            for status in [
                (accepted_statuses[0] if isinstance(accepted_statuses, list) else accepted_statuses)
            ]
        ],
        inconsistencies=[],
        next_steps=(
            []
            if readiness == "READY_TO_SUBMIT"
            else ["Take the corrective action described in the review."]
        ),
        questions_for_user=[],
        official_source_references=references,
        disclaimer="This result is guidance and not legal advice.",
    )


def test_address_registration_dataset_contains_six_synthetic_cases() -> None:
    dataset, cases = load_dataset(DATASET_PATH)

    assert dataset.version == "1.0.0"
    assert {case.id for case in cases} == {
        "complete_application",
        "registration_confirmation_is_not_landlord_confirmation",
        "missing_identity_document",
        "unreadable_identity_scan",
        "conflicting_names",
        "user_claim_is_not_document_evidence",
    }


def test_dataset_loader_sets_completed_requirements_per_case() -> None:
    _, cases = load_dataset(DATASET_PATH)
    missing_identity_case = next(case for case in cases if case.id == "missing_identity_document")
    completion_by_code = {
        requirement.code: requirement.completed
        for requirement in missing_identity_case.request.requirements
    }

    assert completion_by_code == {
        "IDENTITY_DOCUMENTS": False,
        "LANDLORD_CONFIRMATION": True,
        "CIVIL_STATUS_DOCUMENTS": False,
    }


def test_scorer_accepts_expected_results_for_every_case() -> None:
    _, cases = load_dataset(DATASET_PATH)

    results = [evaluate_case(case, _expected_advice(case)) for case in cases]

    assert all(result.passed for result in results)
    assert all(result.score == 1.0 for result in results)


def test_scorer_rejects_unsafe_ready_decision() -> None:
    _, cases = load_dataset(DATASET_PATH)
    case = next(case for case in cases if case.id == "missing_identity_document")
    advice_data = _expected_advice(case).model_dump()
    advice_data["readiness"] = "READY_TO_SUBMIT"

    result = evaluate_case(case, ApplicationAdvice.model_validate(advice_data))

    assert not result.passed
    assert any(check.metric == "safety" and not check.passed for check in result.checks)


def test_scorer_rejects_citation_not_found_in_retrieved_context() -> None:
    _, cases = load_dataset(DATASET_PATH)
    case = cases[0]
    advice_data = _expected_advice(case).model_dump()
    advice_data["official_source_references"][0]["statements"] = ["Invented official statement."]

    result = evaluate_case(case, ApplicationAdvice.model_validate(advice_data))

    assert not result.passed
    assert any(
        check.name == "citations match retrieved official context" and not check.passed
        for check in result.checks
    )


def test_report_exposes_metric_scores() -> None:
    dataset, cases = load_dataset(DATASET_PATH)
    results = [evaluate_case(case, _expected_advice(case)) for case in cases]

    report = build_report(
        dataset_name=dataset.name,
        dataset_version=dataset.version,
        model="test-model",
        results=results,
    )

    assert report.overall_score == 1.0
    assert report.passed_cases == 6
    assert report.metrics["requirements"].score == 1.0
    assert report.metrics["grounding"].score == 1.0
    assert report.metrics["safety"].score == 1.0
