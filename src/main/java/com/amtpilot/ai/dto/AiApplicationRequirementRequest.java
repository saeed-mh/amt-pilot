package com.amtpilot.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiApplicationRequirementRequest(
        String code,
        String title,
        boolean required,
        boolean completed,
        @JsonProperty("official_source_url") String officialSourceUrl) {
}
