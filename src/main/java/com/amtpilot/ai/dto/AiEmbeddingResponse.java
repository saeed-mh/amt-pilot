package com.amtpilot.ai.dto;

import java.util.List;

public record AiEmbeddingResponse(
        String model,
        int dimension,
        List<List<Double>> embeddings) {
}
