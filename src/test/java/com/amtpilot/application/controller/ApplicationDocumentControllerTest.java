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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;

import com.amtpilot.application.dto.DocumentResponse;
import com.amtpilot.application.service.ApplicationDocumentService;
import com.amtpilot.common.web.ApiResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentControllerTest {

    @Mock
    private ApplicationDocumentService documentService;

    @InjectMocks
    private ApplicationDocumentController documentController;

    @Test
    void uploadsDocumentForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID checklistItemId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                "%PDF-1.4 test".getBytes());

        DocumentResponse document = new DocumentResponse(
                documentId,
                applicationId,
                checklistItemId,
                "passport.pdf",
                "application/pdf",
                file.getSize(),
                Instant.parse("2026-09-07T10:00:00Z"));

        when(documentService.upload(
                userId,
                applicationId,
                checklistItemId,
                file))
                .thenReturn(document);

        ResponseEntity<ApiResponse<DocumentResponse>> response =
                documentController.uploadDocument(
                        jwt,
                        applicationId,
                        checklistItemId,
                        file,
                        "trace-upload");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo(document);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId())
                .isEqualTo("trace-upload");

        verify(documentService).upload(
                userId,
                applicationId,
                checklistItemId,
                file);
    }

    @Test
    void returnsDocumentsForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Jwt jwt = mock(Jwt.class);
        when(jwt.getSubject()).thenReturn(userId.toString());

        DocumentResponse document = new DocumentResponse(
                documentId,
                applicationId,
                null,
                "registration.pdf",
                "application/pdf",
                2048,
                Instant.parse("2026-09-07T10:00:00Z"));

        when(documentService.list(userId, applicationId))
                .thenReturn(List.of(document));

        ResponseEntity<ApiResponse<List<DocumentResponse>>> response =
                documentController.getDocuments(
                        jwt,
                        applicationId,
                        "trace-list");

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data())
                .containsExactly(document);
        assertThat(response.getBody().error()).isNull();
        assertThat(response.getBody().traceId())
                .isEqualTo("trace-list");

        verify(documentService).list(userId, applicationId);
    }
}