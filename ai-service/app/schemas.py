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
