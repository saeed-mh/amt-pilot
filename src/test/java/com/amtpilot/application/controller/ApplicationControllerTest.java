package com.amtpilot.application.controller;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.application.service.ApplicationService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.enums.ApplicationStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    @Test
    void createsApplicationForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());

        CreateApplicationRequest request = new CreateApplicationRequest(processId);

        ApplicationResponse application = new ApplicationResponse(
                applicationId,
                processId,
                "DO_ADDRESS_REGISTRATION",
                "Address Registration",
                ApplicationStatus.DRAFT,
                0,
                createdAt,
                createdAt);

        when(applicationService.create(userId, request))
                .thenReturn(application);

        ResponseEntity<ApiResponse<ApplicationResponse>> response = applicationController.createApplication(
                jwt,
                request,
                "trace-123");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data())
                .isEqualTo(application);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId())
                .isEqualTo("trace-123");

        verify(applicationService).create(userId, request);
    }
}