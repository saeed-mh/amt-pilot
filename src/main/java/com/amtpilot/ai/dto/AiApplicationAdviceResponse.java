package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiApplicationAdviceResponse(
        String readiness,
        String summary,
        @JsonProperty("requirement_assessments")
        List<AiRequirementAssessmentResponse> requirementAssessments,
        List<String> inconsistencies,
        @JsonProperty("next_steps") List<String> nextSteps,
        @JsonProperty("questions_for_user") List<String> questionsForUser,
        String disclaimer) {
}
