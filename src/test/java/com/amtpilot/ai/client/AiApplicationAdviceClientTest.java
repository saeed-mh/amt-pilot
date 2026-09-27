package com.amtpilot.ai.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.amtpilot.ai.dto.AiApplicationAdviceRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.dto.AiOfficialProcessGuideRequest;
import com.amtpilot.ai.exception.AiServiceUnavailableException;

class AiApplicationAdviceClientTest {

    private MockRestServiceServer server;
    private AiApplicationAdviceClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer
                .bindTo(builder)
                .build();
        client = new AiApplicationAdviceClient(
                builder,
                "http://localhost:8001");
    }

    @Test
    void sendsContextAndMapsStructuredAdvice() {
        server.expect(requestTo(
                "http://localhost:8001/api/v1/applications/advise"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "process_code": "ADDRESS_REGISTRATION",
                          "process_title": "Address Registration",
                          "city": "Dortmund",
                          "official_guide": {
                            "overview": "Register your new residence.",
                            "eligibility": "Residents can register.",
                            "steps": ["Collect documents."],
                            "deadline": "Within two weeks.",
                            "fee": "Free of charge.",
                            "appointment_required": true,
                            "appointment_information": "Book an appointment.",
                            "appointment_url": "https://example.test/appointment",
                            "source_title": "Official registration guide",
                            "source_url": "https://example.test/guide",
                            "verified_at": "2026-09-27"
                          },
                          "requirements": [],
                          "documents": []
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "readiness": "ACTION_REQUIRED",
                          "summary": "A document is missing.",
                          "requirement_assessments": [],
                          "inconsistencies": [],
                          "next_steps": ["Upload the missing document."],
                          "questions_for_user": [],
                          "official_source_references": [{
                            "section": "deadline",
                            "statements": ["Within two weeks."],
                            "source_title": "Official registration guide",
                            "source_url": "https://example.test/guide",
                            "verified_at": "2026-09-27"
                          }],
                          "disclaimer": "Guidance only; not legal advice."
                        }
                        """, MediaType.APPLICATION_JSON));

        AiApplicationAdviceResponse response = client.advise(
                new AiApplicationAdviceRequest(
                        "ADDRESS_REGISTRATION",
                        "Address Registration",
                        "Dortmund",
                        new AiOfficialProcessGuideRequest(
                                "Register your new residence.",
                                "Residents can register.",
                                List.of("Collect documents."),
                                "Within two weeks.",
                                "Free of charge.",
                                true,
                                "Book an appointment.",
                                "https://example.test/appointment",
                                "Official registration guide",
                                "https://example.test/guide",
                                LocalDate.of(2026, 9, 27)),
                        List.of(),
                        List.of()));

        assertThat(response.readiness())
                .isEqualTo("ACTION_REQUIRED");
        assertThat(response.nextSteps())
                .containsExactly("Upload the missing document.");
        assertThat(response.officialSourceReferences())
                .hasSize(1);
        assertThat(response.officialSourceReferences().getFirst().sourceTitle())
                .isEqualTo("Official registration guide");
        assertThat(response.officialSourceReferences().getFirst().sourceUrl())
                .isEqualTo("https://example.test/guide");
        assertThat(response.officialSourceReferences().getFirst().verifiedAt())
                .isEqualTo(LocalDate.of(2026, 9, 27));

        server.verify();
    }

    @Test
    void convertsServerFailureToUnavailableException() {
        server.expect(requestTo(
                "http://localhost:8001/api/v1/applications/advise"))
                .andRespond(withStatus(
                        HttpStatus.SERVICE_UNAVAILABLE));

        AiApplicationAdviceRequest request =
                new AiApplicationAdviceRequest(
                        "ADDRESS_REGISTRATION",
                        "Address Registration",
                        "Dortmund",
                        null,
                        List.of(),
                        List.of());

        assertThatThrownBy(() -> client.advise(request))
                .isInstanceOf(
                        AiServiceUnavailableException.class)
                .hasCauseInstanceOf(
                        HttpServerErrorException.class);

        server.verify();
    }
}
