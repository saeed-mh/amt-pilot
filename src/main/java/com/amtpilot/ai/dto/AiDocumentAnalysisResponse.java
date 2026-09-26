package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiDocumentAnalysisResponse(
        @JsonProperty("document_type") String documentType,

        @JsonProperty("primary_language") String primaryLanguage,

        String summary,

        @JsonProperty("extracted_fields") List<AiExtractedFieldResponse> extractedFields,

        @JsonProperty("missing_or_unclear") List<String> missingOrUnclear,

        List<String> warnings) {
}