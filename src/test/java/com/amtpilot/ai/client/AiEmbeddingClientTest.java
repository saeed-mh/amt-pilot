package com.amtpilot.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiEmbeddingResponse;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

class AiEmbeddingClientTest {

    private MockRestServiceServer server;
    private AiEmbeddingClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new AiEmbeddingClient(builder, "http://localhost:8001");
    }

    @Test
    void sendsTextsAndMapsEmbeddings() {
        server.expect(requestTo("http://localhost:8001/api/v1/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "texts": ["Official deadline"],
                          "task_type": "RETRIEVAL_QUERY"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "model": "gemini-embedding-001",
                          "dimension": 2,
                          "embeddings": [[0.6, 0.8]]
                        }
                        """, MediaType.APPLICATION_JSON));

        AiEmbeddingResponse response = client.embed(
                List.of("Official deadline"),
                "RETRIEVAL_QUERY");

        assertThat(response.model()).isEqualTo("gemini-embedding-001");
        assertThat(response.embeddings()).containsExactly(List.of(0.6, 0.8));
        server.verify();
    }

    @Test
    void convertsServerFailureToUnavailableException() {
        server.expect(requestTo("http://localhost:8001/api/v1/embeddings"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.embed(
                List.of("Official deadline"),
                "RETRIEVAL_QUERY"))
                .isInstanceOf(AiServiceUnavailableException.class);

        server.verify();
    }
}
