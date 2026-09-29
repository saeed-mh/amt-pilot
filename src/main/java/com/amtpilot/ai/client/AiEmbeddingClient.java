package com.amtpilot.ai.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiEmbeddingRequest;
import com.amtpilot.ai.dto.AiEmbeddingResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

@Component
public class AiEmbeddingClient {

    private final RestClient restClient;

    public AiEmbeddingClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.base-url}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public AiEmbeddingResponse embed(
            List<String> texts,
            String taskType) {

        AiEmbeddingResponse response;

        try {
            response = restClient
                    .post()
                    .uri("/api/v1/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AiEmbeddingRequest(texts, taskType))
                    .retrieve()
                    .body(AiEmbeddingResponse.class);
        } catch (HttpServerErrorException
                | ResourceAccessException exception) {

            throw new AiServiceUnavailableException(
                    "AI embedding service is temporarily unavailable",
                    exception);
        }

        if (response == null) {
            throw new AiServiceUnavailableException(
                    "AI embedding service returned an empty response");
        }

        return response;
    }
}
