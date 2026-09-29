package com.amtpilot.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiEmbeddingRequest(
        List<String> texts,
        @JsonProperty("task_type") String taskType) {
}
