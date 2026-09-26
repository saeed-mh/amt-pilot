package com.amtpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiAnalyzedDocumentRequest(
        @JsonProperty("original_filename") String originalFilename,
        @JsonProperty("requirement_code") String requirementCode,
        AiDocumentAnalysisResponse analysis) {
}
