from typing import Literal

from pydantic import BaseModel, ConfigDict, Field


class ExtractedField(BaseModel):
    model_config = ConfigDict(extra="forbid")

    name: str = Field(description="A clear, machine-readable name for the extracted field.")
    value: str = Field(description="The value extracted from the document.")
    evidence: str = Field(
        description="A short exact passage from the document supporting the value."
    )
    page_number: int | None = Field(
        description="The one-based PDF page number containing the evidence, or null."
    )


class DocumentAnalysis(BaseModel):
    model_config = ConfigDict(extra="forbid")

    document_type: str = Field(description="The identified type of document.")
    primary_language: str = Field(description="The primary language used in the document.")
    summary: str = Field(description="A short and simple summary of the document.")
    extracted_fields: list[ExtractedField] = Field(
        description="Important fields found in the document."
    )
    missing_or_unclear: list[str] = Field(
        description="Important information that is missing or unclear."
    )
    warnings: list[str] = Field(
        description="Potential expiry, inconsistency, or document-quality problems."
    )


class ApplicationRequirement(BaseModel):
    model_config = ConfigDict(extra="forbid")

    code: str
    title: str
    required: bool
    completed: bool
    official_source_url: str | None = None


class AnalyzedDocument(BaseModel):
    model_config = ConfigDict(extra="forbid")

    original_filename: str
    requirement_code: str | None = None
    analysis: DocumentAnalysis


class ApplicationAdviceRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    process_code: str
    process_title: str
    city: str
    requirements: list[ApplicationRequirement]
    documents: list[AnalyzedDocument]


class RequirementAssessment(BaseModel):
    model_config = ConfigDict(extra="forbid")

    requirement_code: str
    status: Literal[
        "SATISFIED",
        "MISSING",
        "NEEDS_REVIEW",
        "NOT_APPLICABLE",
    ] = Field(description=("One of SATISFIED, MISSING, NEEDS_REVIEW, or NOT_APPLICABLE."))
    explanation: str
    supporting_documents: list[str]


class ApplicationAdvice(BaseModel):
    model_config = ConfigDict(extra="forbid")

    readiness: Literal[
        "READY_TO_SUBMIT",
        "ACTION_REQUIRED",
        "NEEDS_REVIEW",
    ] = Field(description="One of READY_TO_SUBMIT, ACTION_REQUIRED, or NEEDS_REVIEW.")
    summary: str
    requirement_assessments: list[RequirementAssessment]
    inconsistencies: list[str]
    next_steps: list[str]
    questions_for_user: list[str]
    disclaimer: str
