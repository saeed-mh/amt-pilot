package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AiRequirementAssessmentResponse(
        @JsonAlias("requirement_code") String requirementCode,
        String status,
        String explanation,
        @JsonAlias("supporting_documents") List<String> supportingDocuments,
        @JsonAlias("official_source_title") String officialSourceTitle,
        @JsonAlias("official_source_url") String officialSourceUrl) {
}
