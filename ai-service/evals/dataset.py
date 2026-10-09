from pathlib import Path

from app.schemas import ApplicationAdviceRequest
from evals.models import EvaluationCase, EvaluationCaseDefinition, EvaluationDataset


def load_dataset(path: Path) -> tuple[EvaluationDataset, list[EvaluationCase]]:
    dataset = EvaluationDataset.model_validate_json(path.read_text(encoding="utf-8"))
    cases = [_build_case(dataset, definition) for definition in dataset.cases]
    return dataset, cases


def _build_case(
    dataset: EvaluationDataset,
    definition: EvaluationCaseDefinition,
) -> EvaluationCase:
    request_data = dataset.base_request.model_dump()
    completed_codes = set(definition.completed_requirement_codes)

    for requirement in request_data["requirements"]:
        requirement["completed"] = requirement["code"] in completed_codes

    request_data["documents"] = [document.model_dump() for document in definition.documents]
    request_data["user_answers"] = definition.user_answers

    return EvaluationCase(
        id=definition.id,
        description=definition.description,
        request=ApplicationAdviceRequest.model_validate(request_data),
        expected=definition.expected,
    )
