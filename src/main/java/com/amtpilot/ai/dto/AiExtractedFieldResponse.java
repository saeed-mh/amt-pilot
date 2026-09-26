package com.amtpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiExtractedFieldResponse(
        String name,
        String value,
        String evidence,
        @JsonProperty("page_number") Integer pageNumber) {
}