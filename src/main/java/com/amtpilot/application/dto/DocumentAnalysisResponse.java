package com.amtpilot.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.amtpilot.ai.dto.AiExtractedFieldResponse;

public record DocumentAnalysisResponse(
        UUID id,
        UUID documentId,
        String originalFilename,
        String documentType,
        String primaryLanguage,
        String summary,
        List<AiExtractedFieldResponse> extractedFields,
        List<String> missingOrUnclear,
        List<String> warnings,
        Instant createdAt,
        Instant updatedAt) {
}