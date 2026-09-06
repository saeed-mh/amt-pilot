package com.amtpilot.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.amtpilot.application.dto.DocumentResponse;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.ChecklistItemNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationChecklistItemRepository checklistRepository;

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private DocumentStorageService storageService;

    private ApplicationDocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new ApplicationDocumentService(
                applicationRepository,
                checklistRepository,
                documentRepository,
                storageService);
    }

    @Test
    void uploadsDocumentForOwnedApplication() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID checklistItemId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(Application.class);

        ApplicationChecklistItem checklistItem = org.mockito.Mockito.mock(
                ApplicationChecklistItem.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "C:\\fakepath\\passport.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(checklistRepository
                .findByIdAndApplicationUserId(
                        checklistItemId,
                        userId))
                .thenReturn(Optional.of(checklistItem));

        when(application.getId())
                .thenReturn(applicationId);

        when(checklistItem.getId())
                .thenReturn(checklistItemId);

        when(checklistItem.getApplication())
                .thenReturn(application);

        when(storageService.store(file))
                .thenReturn("stored-document.pdf");

        when(documentRepository.saveAndFlush(
                any(ApplicationDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse result = documentService.upload(
                userId,
                applicationId,
                checklistItemId,
                file);

        assertThat(result.applicationId())
                .isEqualTo(applicationId);

        assertThat(result.checklistItemId())
                .isEqualTo(checklistItemId);

        assertThat(result.originalFilename())
                .isEqualTo("passport.pdf");

        assertThat(result.contentType())
                .isEqualTo("application/pdf");

        ArgumentCaptor<ApplicationDocument> captor = ArgumentCaptor.forClass(
                ApplicationDocument.class);

        verify(documentRepository)
                .saveAndFlush(captor.capture());

        ApplicationDocument savedDocument = captor.getValue();

        assertThat(savedDocument.getStoragePath())
                .isEqualTo("stored-document.pdf");

        assertThat(savedDocument.getApplication())
                .isSameAs(application);

        assertThat(savedDocument.getChecklistItem())
                .isSameAs(checklistItem);
    }

    @Test
    void listsDocumentsForOwnedApplication() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-09-07T10:00:00Z");

        Application application = org.mockito.Mockito.mock(Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(document));

        when(application.getId())
                .thenReturn(applicationId);

        when(document.getId())
                .thenReturn(documentId);

        when(document.getApplication())
                .thenReturn(application);

        when(document.getOriginalFilename())
                .thenReturn("passport.pdf");

        when(document.getContentType())
                .thenReturn("application/pdf");

        when(document.getSizeBytes())
                .thenReturn(1024L);

        when(document.getCreatedAt())
                .thenReturn(createdAt);

        List<DocumentResponse> result = documentService.list(
                userId,
                applicationId);

        assertThat(result).hasSize(1);

        assertThat(result.get(0).id())
                .isEqualTo(documentId);

        assertThat(result.get(0).originalFilename())
                .isEqualTo("passport.pdf");

        assertThat(result.get(0).createdAt())
                .isEqualTo(createdAt);
    }

    @Test
    void rejectsUploadForApplicationNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.upload(
                userId,
                applicationId,
                null,
                file))
                .isInstanceOf(
                        ApplicationNotFoundException.class);

        verifyNoInteractions(
                checklistRepository,
                documentRepository,
                storageService);
    }

    @Test
    void rejectsChecklistItemFromAnotherApplication() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID checklistItemId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(Application.class);

        Application anotherApplication = org.mockito.Mockito.mock(Application.class);

        ApplicationChecklistItem checklistItem = org.mockito.Mockito.mock(
                ApplicationChecklistItem.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(checklistRepository
                .findByIdAndApplicationUserId(
                        checklistItemId,
                        userId))
                .thenReturn(Optional.of(checklistItem));

        when(checklistItem.getApplication())
                .thenReturn(anotherApplication);

        when(anotherApplication.getId())
                .thenReturn(UUID.randomUUID());

        assertThatThrownBy(() -> documentService.upload(
                userId,
                applicationId,
                checklistItemId,
                file))
                .isInstanceOf(
                        ChecklistItemNotFoundException.class);

        verifyNoInteractions(
                documentRepository,
                storageService);
    }

    @Test
    void deletesStoredFileWhenDatabaseSaveFails() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(Application.class);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "passport.pdf",
                "application/pdf",
                "%PDF-1.7".getBytes());

        IllegalStateException databaseFailure = new IllegalStateException(
                "Database failure");

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(storageService.store(file))
                .thenReturn("stored-document.pdf");

        when(documentRepository.saveAndFlush(
                any(ApplicationDocument.class)))
                .thenThrow(databaseFailure);

        assertThatThrownBy(() -> documentService.upload(
                userId,
                applicationId,
                null,
                file))
                .isSameAs(databaseFailure);

        verify(storageService)
                .delete("stored-document.pdf");
    }
}