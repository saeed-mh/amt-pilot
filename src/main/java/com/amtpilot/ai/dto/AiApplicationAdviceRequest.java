package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiApplicationAdviceRequest(
        @JsonProperty("process_code") String processCode,
        @JsonProperty("process_title") String processTitle,
        String city,
        List<AiApplicationRequirementRequest> requirements,
        List<AiAnalyzedDocumentRequest> documents) {
}
