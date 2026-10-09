from collections import Counter, defaultdict
from datetime import UTC, datetime

from app.schemas import ApplicationAdvice
from evals.models import (
    CaseEvaluation,
    EvaluationCase,
    EvaluationCheck,
    EvaluationReport,
    MetricName,
    MetricScore,
)


def evaluate_case(
    case: EvaluationCase,
    advice: ApplicationAdvice,
) -> CaseEvaluation:
    checks: list[EvaluationCheck] = []

    def check(metric: MetricName, name: str, passed: bool, details: str) -> None:
        checks.append(
            EvaluationCheck(
                metric=metric,
                name=name,
                passed=passed,
                details=details,
            )
        )

    expected = case.expected
    check(
        "readiness",
        "accepted readiness",
        advice.readiness in expected.accepted_readiness,
        (f"expected one of {expected.accepted_readiness}; received {advice.readiness}"),
    )
    unsafe_ready_decision = expected.must_not_be_ready and advice.readiness == "READY_TO_SUBMIT"
    check(
        "safety",
        "unsafe ready-to-submit decision",
        not unsafe_ready_decision,
        (
            "the readiness decision respects the case safety constraint"
            if not unsafe_ready_decision
            else "a problematic application was incorrectly marked ready"
        ),
    )

    assessments_by_code = {
        assessment.requirement_code: assessment for assessment in advice.requirement_assessments
    }
    assessment_counts = Counter(
        assessment.requirement_code for assessment in advice.requirement_assessments
    )
    check(
        "requirements",
        "one assessment per expected requirement",
        all(assessment_counts[code] == 1 for code in expected.requirement_statuses),
        f"assessment counts: {dict(assessment_counts)}",
    )

    for code, expected_status in expected.requirement_statuses.items():
        accepted_statuses = (
            expected_status if isinstance(expected_status, list) else [expected_status]
        )
        assessment = assessments_by_code.get(code)
        actual_status = assessment.status if assessment else None
        check(
            "requirements",
            f"{code} classification",
            actual_status in accepted_statuses,
            f"expected one of {accepted_statuses}; received {actual_status}",
        )

    needs_review_count = sum(
        assessment.status == "NEEDS_REVIEW" for assessment in advice.requirement_assessments
    )
    check(
        "requirements",
        "minimum requirements needing review",
        needs_review_count >= expected.minimum_needs_review_requirements,
        (
            f"expected at least {expected.minimum_needs_review_requirements}; "
            f"received {needs_review_count}"
        ),
    )

    references = advice.official_source_references
    check(
        "grounding",
        "minimum grounded references",
        len(references) >= expected.minimum_grounded_references,
        (f"expected at least {expected.minimum_grounded_references}; received {len(references)}"),
    )

    actual_sections = {reference.section for reference in references}
    for section in expected.required_reference_sections:
        check(
            "grounding",
            f"official {section} section cited",
            section in actual_sections,
            f"cited sections: {sorted(actual_sections)}",
        )

    trusted_statements = {
        (
            chunk.section,
            chunk.content,
            chunk.source_title,
            chunk.source_url,
            chunk.verified_at,
        )
        for chunk in case.request.retrieved_guide_chunks
    }
    grounded_statements = [
        (
            reference.section,
            statement,
            reference.source_title,
            reference.source_url,
            reference.verified_at,
        )
        for reference in references
        for statement in reference.statements
    ]
    all_statements_are_grounded = all(
        statement in trusted_statements for statement in grounded_statements
    )
    check(
        "grounding",
        "citations match retrieved official context",
        all_statements_are_grounded,
        (
            "all cited statements came from retrieved guide chunks"
            if all_statements_are_grounded
            else "at least one cited statement was not present in retrieved guide chunks"
        ),
    )

    guidance_text = " ".join(
        [
            advice.summary,
            *advice.next_steps,
            *advice.inconsistencies,
            *(assessment.explanation for assessment in advice.requirement_assessments),
        ]
    ).casefold()
    for term in expected.required_guidance_terms:
        check(
            "guidance",
            f"guidance includes '{term}'",
            term.casefold() in guidance_text,
            "searched the summary, assessments, inconsistencies, and next steps",
        )

    if advice.readiness != "READY_TO_SUBMIT":
        check(
            "guidance",
            "non-ready result has an actionable next step",
            bool(advice.next_steps),
            f"received {len(advice.next_steps)} next steps",
        )

    passed_checks = sum(result.passed for result in checks)
    score = passed_checks / len(checks) if checks else 0.0
    return CaseEvaluation(
        case_id=case.id,
        description=case.description,
        score=score,
        passed=all(result.passed for result in checks),
        actual_readiness=advice.readiness,
        checks=checks,
        advice=advice,
    )


def evaluate_error(case: EvaluationCase, error: Exception) -> CaseEvaluation:
    return CaseEvaluation(
        case_id=case.id,
        description=case.description,
        score=0.0,
        passed=False,
        checks=[
            EvaluationCheck(
                metric="safety",
                name="evaluation completed",
                passed=False,
                details=str(error),
            )
        ],
        error=f"{type(error).__name__}: {error}",
    )


def build_report(
    *,
    dataset_name: str,
    dataset_version: str,
    model: str,
    results: list[CaseEvaluation],
) -> EvaluationReport:
    metric_checks = defaultdict(list)
    for result in results:
        for check in result.checks:
            metric_checks[check.metric].append(check.passed)

    metrics = {}
    for metric in ("readiness", "requirements", "grounding", "guidance", "safety"):
        checks = metric_checks[metric]
        passed = sum(checks)
        metrics[metric] = MetricScore(
            passed=passed,
            total=len(checks),
            score=passed / len(checks) if checks else 0.0,
        )

    return EvaluationReport(
        dataset=dataset_name,
        dataset_version=dataset_version,
        model=model,
        generated_at=datetime.now(UTC),
        overall_score=(sum(result.score for result in results) / len(results) if results else 0.0),
        passed_cases=sum(result.passed for result in results),
        total_cases=len(results),
        metrics=metrics,
        cases=results,
    )
