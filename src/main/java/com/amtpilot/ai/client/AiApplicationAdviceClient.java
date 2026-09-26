package com.amtpilot.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiApplicationAdviceRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

@Component
public class AiApplicationAdviceClient {

    private final RestClient restClient;

    public AiApplicationAdviceClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.base-url}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public AiApplicationAdviceResponse advise(
            AiApplicationAdviceRequest request) {

        AiApplicationAdviceResponse response;

        try {
            response = restClient
                    .post()
                    .uri("/api/v1/applications/advise")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(AiApplicationAdviceResponse.class);

        } catch (HttpServerErrorException
                | ResourceAccessException exception) {

            throw new AiServiceUnavailableException(
                    "AI application advice is temporarily unavailable",
                    exception);
        }

        if (response == null) {
            throw new AiServiceUnavailableException(
                    "AI application advice service returned an empty response");
        }

        return response;
    }
}
