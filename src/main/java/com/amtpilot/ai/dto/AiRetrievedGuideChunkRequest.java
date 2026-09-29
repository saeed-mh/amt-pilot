package com.amtpilot.ai.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRetrievedGuideChunkRequest(
        String section,
        String content,
        @JsonProperty("source_title") String sourceTitle,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("verified_at") LocalDate verifiedAt) {
}
