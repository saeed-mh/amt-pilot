package com.amtpilot.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

class AiDocumentAnalysisClientTest {

    private MockRestServiceServer server;
    private AiDocumentAnalysisClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();

        server = MockRestServiceServer
                .bindTo(restClientBuilder)
                .build();

        client = new AiDocumentAnalysisClient(
                restClientBuilder,
                "http://localhost:8001");
    }

    @Test
    void sendsPdfAndMapsStructuredAnalysis() {
        String responseBody = """
                {
                  "document_type": "Meldebest\u00e4tigung",
                  "primary_language": "de",
                  "summary": "A sample registration document.",
                  "extracted_fields": [
                    {
                      "name": "full_name",
                      "value": "Max Mustermann",
                      "evidence": "Max Mustermann",
                      "page_number": 1
                    }
                  ],
                  "missing_or_unclear": [],
                  "warnings": ["Sample document"]
                }
                """;

        server.expect(requestTo(
                "http://localhost:8001/api/v1/documents/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(
                        containsString(
                                "filename=\"registration.pdf\"")))
                .andExpect(content().string(
                        containsString("%PDF-test")))
                .andRespond(withSuccess(
                        responseBody,
                        MediaType.APPLICATION_JSON));

        ByteArrayResource document = new ByteArrayResource(
                "%PDF-test".getBytes(StandardCharsets.UTF_8));

        AiDocumentAnalysisResponse result = client.analyze(
                document,
                "registration.pdf");

        assertThat(result.documentType())
                .isEqualTo("Meldebest\u00e4tigung");
        assertThat(result.primaryLanguage()).isEqualTo("de");
        assertThat(result.extractedFields()).hasSize(1);
        assertThat(result.extractedFields().getFirst().name())
                .isEqualTo("full_name");
        assertThat(result.extractedFields().getFirst().pageNumber())
                .isEqualTo(1);
        assertThat(result.warnings())
                .containsExactly("Sample document");

        server.verify();
    }

    @Test
    void convertsServerFailureToUnavailableException() {
        server.expect(requestTo(
                "http://localhost:8001/api/v1/documents/analyze"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(
                        HttpStatus.SERVICE_UNAVAILABLE));

        ByteArrayResource document = new ByteArrayResource(
                "%PDF-test".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(
                () -> client.analyze(
                        document,
                        "registration.pdf"))
                .isInstanceOf(
                        AiServiceUnavailableException.class)
                .hasMessage(
                        "AI analysis service is temporarily unavailable")
                .hasCauseInstanceOf(
                        HttpServerErrorException.class);

        server.verify();
    }
}