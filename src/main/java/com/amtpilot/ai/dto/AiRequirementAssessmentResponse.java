package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRequirementAssessmentResponse(
        @JsonProperty("requirement_code") String requirementCode,
        String status,
        String explanation,
        @JsonProperty("supporting_documents") List<String> supportingDocuments) {
}
