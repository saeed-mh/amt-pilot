package com.amtpilot.ai.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiApplicationAdviceRequest(
        @JsonProperty("process_code") String processCode,
        @JsonProperty("process_title") String processTitle,
        String city,
        @JsonProperty("official_guide") AiOfficialProcessGuideRequest officialGuide,
        @JsonProperty("retrieved_guide_chunks")
        List<AiRetrievedGuideChunkRequest> retrievedGuideChunks,
        List<AiApplicationRequirementRequest> requirements,
        List<AiAnalyzedDocumentRequest> documents,
        @JsonProperty("user_answers") Map<String, String> userAnswers) {
}
