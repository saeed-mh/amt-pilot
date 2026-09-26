package com.amtpilot.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

@Component
public class AiDocumentAnalysisClient {

    private final RestClient restClient;

    public AiDocumentAnalysisClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.base-url}") String baseUrl) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public AiDocumentAnalysisResponse analyze(
            Resource document,
            String originalFilename) {

        HttpHeaders documentHeaders = new HttpHeaders();
        documentHeaders.setContentType(
                MediaType.APPLICATION_PDF);
        documentHeaders.setContentDispositionFormData(
                "file",
                originalFilename);

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();

        requestBody.add(
                "file",
                new HttpEntity<>(
                        document,
                        documentHeaders));

        AiDocumentAnalysisResponse response;

        try {
            response = restClient
                    .post()
                    .uri("/api/v1/documents/analyze")
                    .contentType(
                            MediaType.MULTIPART_FORM_DATA)
                    .body(requestBody)
                    .retrieve()
                    .body(AiDocumentAnalysisResponse.class);

        } catch (HttpServerErrorException
                | ResourceAccessException exception) {

            throw new AiServiceUnavailableException(
                    "AI analysis service is temporarily unavailable",
                    exception);
        }

        if (response == null) {
            throw new AiServiceUnavailableException(
                    "AI analysis service returned an empty response");
        }

        return response;
    }
}