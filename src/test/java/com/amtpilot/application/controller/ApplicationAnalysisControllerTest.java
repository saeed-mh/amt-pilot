package com.amtpilot.application.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.amtpilot.ai.dto.AiExtractedFieldResponse;
import com.amtpilot.application.dto.ApplicationAdviceResponse;
import com.amtpilot.application.dto.DocumentAnalysisResponse;
import com.amtpilot.application.service.ApplicationAdviceService;
import com.amtpilot.application.service.ApplicationDocumentAnalysisService;
import com.amtpilot.common.web.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationAnalysisControllerTest {

    @Mock
    private ApplicationDocumentAnalysisService analysisService;

    @Mock
    private ApplicationAdviceService adviceService;

    @InjectMocks
    private ApplicationAnalysisController analysisController;

    @Test
    void returnsStoredAnalysesForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject())
                .thenReturn(userId.toString());

        DocumentAnalysisResponse analysis = new DocumentAnalysisResponse(
                analysisId,
                documentId,
                "registration.pdf",
                "Registration confirmation",
                "de",
                "Residence registration document",
                List.of(
                        new AiExtractedFieldResponse(
                                "full_name",
                                "Max Mustermann",
                                "Name: Max Mustermann",
                                1)),
                List.of(),
                List.of("Sample document"),
                Instant.parse("2026-09-26T10:00:00Z"),
                Instant.parse("2026-09-26T10:05:00Z"));

        when(analysisService.listForApplication(
                userId,
                applicationId))
                .thenReturn(List.of(analysis));

        ResponseEntity<ApiResponse<List<DocumentAnalysisResponse>>> response = analysisController.getAnalyses(
                jwt,
                applicationId,
                "trace-analysis");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data())
                .containsExactly(analysis);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId())
                .isEqualTo("trace-analysis");

        verify(analysisService).listForApplication(
                userId,
                applicationId);
    }

    @Test
    void returnsStoredApplicationAdviceForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID adviceId = UUID.randomUUID();
        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject())
                .thenReturn(userId.toString());

        ApplicationAdviceResponse advice =
                new ApplicationAdviceResponse(
                        adviceId,
                        "ACTION_REQUIRED",
                        "One required document is missing.",
                        List.of(),
                        List.of(),
                        List.of("Upload the missing document."),
                        List.of(),
                        "Guidance only; not legal advice.",
                        Instant.parse("2026-09-26T11:00:00Z"),
                        Instant.parse("2026-09-26T11:05:00Z"));

        when(adviceService.getForApplication(
                userId,
                applicationId))
                .thenReturn(java.util.Optional.of(advice));

        ResponseEntity<ApiResponse<ApplicationAdviceResponse>> response =
                analysisController.getAdvice(
                        jwt,
                        applicationId,
                        "trace-advice");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data())
                .isEqualTo(advice);
        assertThat(response.getBody().traceId())
                .isEqualTo("trace-advice");

        verify(adviceService).getForApplication(
                userId,
                applicationId);
    }
}
