from datetime import datetime
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

from app.schemas import (
    AnalyzedDocument,
    ApplicationAdvice,
    ApplicationAdviceRequest,
    GuideSection,
)

Readiness = Literal["READY_TO_SUBMIT", "ACTION_REQUIRED", "NEEDS_REVIEW"]
RequirementStatus = Literal[
    "SATISFIED",
    "MISSING",
    "NEEDS_REVIEW",
    "NOT_APPLICABLE",
]
MetricName = Literal["readiness", "requirements", "grounding", "guidance", "safety"]


class ExpectedOutcome(BaseModel):
    model_config = ConfigDict(extra="forbid")

    accepted_readiness: list[Readiness] = Field(min_length=1)
    requirement_statuses: dict[str, RequirementStatus | list[RequirementStatus]]
    minimum_needs_review_requirements: int = Field(default=0, ge=0)
    minimum_grounded_references: int = Field(default=1, ge=0)
    required_reference_sections: list[GuideSection] = Field(default_factory=list)
    required_guidance_terms: list[str] = Field(default_factory=list)
    must_not_be_ready: bool = False


class EvaluationCaseDefinition(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: str = Field(pattern=r"^[a-z0-9_]+$")
    description: str
    completed_requirement_codes: list[str] = Field(default_factory=list)
    documents: list[AnalyzedDocument] = Field(default_factory=list)
    user_answers: dict[str, str] = Field(default_factory=dict)
    expected: ExpectedOutcome


class EvaluationDataset(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str
    version: str
    description: str
    base_request: ApplicationAdviceRequest
    cases: list[EvaluationCaseDefinition] = Field(min_length=1)


class EvaluationCase(BaseModel):
    model_config = ConfigDict(extra="forbid")

    id: str
    description: str
    request: ApplicationAdviceRequest
    expected: ExpectedOutcome


class EvaluationCheck(BaseModel):
    model_config = ConfigDict(extra="forbid")

    metric: MetricName
    name: str
    passed: bool
    details: str


class MetricScore(BaseModel):
    model_config = ConfigDict(extra="forbid")

    passed: int
    total: int
    score: float


class CaseEvaluation(BaseModel):
    model_config = ConfigDict(extra="forbid")

    case_id: str
    description: str
    score: float
    passed: bool
    actual_readiness: Readiness | None = None
    checks: list[EvaluationCheck]
    advice: ApplicationAdvice | None = None
    error: str | None = None


class EvaluationReport(BaseModel):
    model_config = ConfigDict(extra="forbid")

    dataset: str
    dataset_version: str
    model: str
    generated_at: datetime
    overall_score: float
    passed_cases: int
    total_cases: int
    metrics: dict[MetricName, MetricScore]
    cases: list[CaseEvaluation]
