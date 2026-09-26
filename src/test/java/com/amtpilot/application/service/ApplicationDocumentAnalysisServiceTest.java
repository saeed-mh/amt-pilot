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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.amtpilot.ai.client.AiDocumentAnalysisClient;
import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.ai.dto.AiExtractedFieldResponse;
import com.amtpilot.application.dto.DocumentAnalysisResponse;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.DocumentNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentAnalysisServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private ApplicationDocumentAnalysisRepository analysisRepository;

    @Mock
    private DocumentStorageService storageService;

    @Mock
    private AiDocumentAnalysisClient aiClient;

    private ApplicationDocumentAnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new ApplicationDocumentAnalysisService(
                applicationRepository,
                documentRepository,
                analysisRepository,
                storageService,
                aiClient);
    }

    @Test
    void analyzesAndStoresNewDocumentAnalysis() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(
                Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        Resource resource = new ByteArrayResource(
                "%PDF-1.7 sample".getBytes());

        AiDocumentAnalysisResponse aiResult = analysisResult();

        when(documentRepository.findByIdAndApplicationUserId(
                documentId,
                userId))
                .thenReturn(Optional.of(document));

        when(document.getApplication())
                .thenReturn(application);

        when(application.getId())
                .thenReturn(applicationId);

        when(document.getStoragePath())
                .thenReturn("stored-document.pdf");

        when(document.getOriginalFilename())
                .thenReturn("registration.pdf");

        when(storageService.load("stored-document.pdf"))
                .thenReturn(resource);

        when(aiClient.analyze(
                resource,
                "registration.pdf"))
                .thenReturn(aiResult);

        when(analysisRepository.findByDocumentId(documentId))
                .thenReturn(Optional.empty());

        when(analysisRepository.saveAndFlush(
                any(ApplicationDocumentAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationDocumentAnalysis result = analysisService.analyze(
                userId,
                applicationId,
                documentId);

        assertThat(result.getDocument())
                .isSameAs(document);

        assertThat(result.getDocumentType())
                .isEqualTo("Meldebestätigung");

        assertThat(result.getPrimaryLanguage())
                .isEqualTo("de");

        assertThat(result.getSummary())
                .isEqualTo("Residence registration document");

        assertThat(result.getExtractedFields())
                .hasSize(1);

        assertThat(result.getMissingOrUnclear())
                .containsExactly("Issue date is unclear");

        assertThat(result.getWarnings())
                .containsExactly("Sample document");

        ArgumentCaptor<ApplicationDocumentAnalysis> captor = ArgumentCaptor.forClass(
                ApplicationDocumentAnalysis.class);

        verify(analysisRepository)
                .saveAndFlush(captor.capture());

        assertThat(captor.getValue())
                .isSameAs(result);
    }

    @Test
    void reusesExistingDocumentAnalysis() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(
                Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        ApplicationDocumentAnalysis existingAnalysis = org.mockito.Mockito.mock(
                ApplicationDocumentAnalysis.class);

        when(documentRepository.findByIdAndApplicationUserId(
                documentId,
                userId))
                .thenReturn(Optional.of(document));

        when(document.getApplication())
                .thenReturn(application);

        when(application.getId())
                .thenReturn(applicationId);

        when(analysisRepository.findByDocumentId(documentId))
                .thenReturn(Optional.of(existingAnalysis));

        ApplicationDocumentAnalysis result = analysisService.analyze(
                userId,
                applicationId,
                documentId);

        assertThat(result)
                .isSameAs(existingAnalysis);

        verifyNoInteractions(
                storageService,
                aiClient);
    }

    @Test
    void rejectsDocumentFromAnotherApplication() {
        UUID userId = UUID.randomUUID();
        UUID requestedApplicationId = UUID.randomUUID();
        UUID actualApplicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(
                Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        when(documentRepository.findByIdAndApplicationUserId(
                documentId,
                userId))
                .thenReturn(Optional.of(document));

        when(document.getApplication())
                .thenReturn(application);

        when(application.getId())
                .thenReturn(actualApplicationId);

        assertThatThrownBy(() -> analysisService.analyze(
                userId,
                requestedApplicationId,
                documentId))
                .isInstanceOf(DocumentNotFoundException.class);

        verifyNoInteractions(
                storageService,
                aiClient,
                analysisRepository);
    }

    @Test
    void listsStoredAnalysesForOwnedApplication() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        Instant createdAt = Instant.parse(
                "2026-09-26T10:00:00Z");
        Instant updatedAt = Instant.parse(
                "2026-09-26T10:05:00Z");

        Application application = org.mockito.Mockito.mock(
                Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        ApplicationDocumentAnalysis analysis = org.mockito.Mockito.mock(
                ApplicationDocumentAnalysis.class);

        AiExtractedFieldResponse extractedField =
                new AiExtractedFieldResponse(
                        "full_name",
                        "Max Mustermann",
                        "Name: Max Mustermann",
                        1);

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(analysisRepository
                .findByDocumentApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(analysis));

        when(analysis.getId())
                .thenReturn(analysisId);

        when(analysis.getDocument())
                .thenReturn(document);

        when(document.getId())
                .thenReturn(documentId);

        when(document.getOriginalFilename())
                .thenReturn("registration.pdf");

        when(analysis.getDocumentType())
                .thenReturn("Registration confirmation");

        when(analysis.getPrimaryLanguage())
                .thenReturn("de");

        when(analysis.getSummary())
                .thenReturn("Residence registration document");

        when(analysis.getExtractedFields())
                .thenReturn(List.of(extractedField));

        when(analysis.getMissingOrUnclear())
                .thenReturn(List.of());

        when(analysis.getWarnings())
                .thenReturn(List.of("Sample document"));

        when(analysis.getCreatedAt())
                .thenReturn(createdAt);

        when(analysis.getUpdatedAt())
                .thenReturn(updatedAt);

        List<DocumentAnalysisResponse> result =
                analysisService.listForApplication(
                        userId,
                        applicationId);

        assertThat(result).hasSize(1);

        DocumentAnalysisResponse response = result.get(0);

        assertThat(response.id()).isEqualTo(analysisId);
        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.originalFilename())
                .isEqualTo("registration.pdf");
        assertThat(response.documentType())
                .isEqualTo("Registration confirmation");
        assertThat(response.extractedFields())
                .containsExactly(extractedField);
        assertThat(response.missingOrUnclear()).isEmpty();
        assertThat(response.warnings())
                .containsExactly("Sample document");
        assertThat(response.createdAt()).isEqualTo(createdAt);
        assertThat(response.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void rejectsListingAnalysesForApplicationNotOwnedByUser() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> analysisService.listForApplication(
                        userId,
                        applicationId))
                .isInstanceOf(ApplicationNotFoundException.class);

        verifyNoInteractions(analysisRepository);
    }

    private AiDocumentAnalysisResponse analysisResult() {
        return new AiDocumentAnalysisResponse(
                "Meldebestätigung",
                "de",
                "Residence registration document",
                List.of(
                        new AiExtractedFieldResponse(
                                "full_name",
                                "Max Mustermann",
                                "Name: Max Mustermann",
                                1)),
                List.of("Issue date is unclear"),
                List.of("Sample document"));
    }
}
