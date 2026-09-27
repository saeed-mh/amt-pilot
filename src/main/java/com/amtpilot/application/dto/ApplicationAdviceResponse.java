package com.amtpilot.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.amtpilot.ai.dto.AiOfficialSourceReferenceResponse;
import com.amtpilot.ai.dto.AiRequirementAssessmentResponse;

public record ApplicationAdviceResponse(
        UUID id,
        String readiness,
        String summary,
        List<AiRequirementAssessmentResponse> requirementAssessments,
        List<String> inconsistencies,
        List<String> nextSteps,
        List<String> questionsForUser,
        List<AiOfficialSourceReferenceResponse> officialSourceReferences,
        String disclaimer,
        Instant createdAt,
        Instant updatedAt) {
}
