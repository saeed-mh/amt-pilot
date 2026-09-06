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

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.application.dto.UpdateApplicationRequest;
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

        @Test
        void returnsApplicationsForAuthenticatedUser() {
                UUID userId = UUID.randomUUID();
                UUID processId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();

                Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");

                Jwt jwt = mock(Jwt.class);
                when(jwt.getSubject()).thenReturn(userId.toString());

                ApplicationResponse application = new ApplicationResponse(
                                applicationId,
                                processId,
                                "DO_ADDRESS_REGISTRATION",
                                "Address Registration",
                                ApplicationStatus.DRAFT,
                                0,
                                createdAt,
                                createdAt);

                when(applicationService.getApplicationsForUser(userId))
                                .thenReturn(List.of(application));

                ResponseEntity<ApiResponse<List<ApplicationResponse>>> response = applicationController.getApplications(
                                jwt,
                                "trace-456");

                assertThat(response.getStatusCode())
                                .isEqualTo(HttpStatus.OK);

                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().success()).isTrue();
                assertThat(response.getBody().data())
                                .containsExactly(application);
                assertThat(response.getBody().error()).isNull();
                assertThat(response.getBody().traceId())
                                .isEqualTo("trace-456");

                verify(applicationService)
                                .getApplicationsForUser(userId);
        }

        @Test
        void updatesApplicationForAuthenticatedUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID processId = UUID.randomUUID();

                Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");
                Instant updatedAt = Instant.parse("2026-09-06T10:00:00Z");

                Jwt jwt = mock(Jwt.class);
                when(jwt.getSubject()).thenReturn(userId.toString());

                UpdateApplicationRequest request = new UpdateApplicationRequest(
                                ApplicationStatus.ACTION_REQUIRED,
                                40);

                ApplicationResponse updatedApplication = new ApplicationResponse(
                                applicationId,
                                processId,
                                "ADDRESS_REGISTRATION",
                                "Address Registration",
                                ApplicationStatus.ACTION_REQUIRED,
                                40,
                                createdAt,
                                updatedAt);

                when(applicationService.update(
                                userId,
                                applicationId,
                                request))
                                .thenReturn(updatedApplication);

                ResponseEntity<ApiResponse<ApplicationResponse>> response = applicationController.updateApplication(
                                jwt,
                                applicationId,
                                request,
                                "trace-789");

                assertThat(response.getStatusCode())
                                .isEqualTo(HttpStatus.OK);

                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().success()).isTrue();
                assertThat(response.getBody().data())
                                .isEqualTo(updatedApplication);
                assertThat(response.getBody().error()).isNull();
                assertThat(response.getBody().traceId())
                                .isEqualTo("trace-789");

                verify(applicationService).update(
                                userId,
                                applicationId,
                                request);
        }
}