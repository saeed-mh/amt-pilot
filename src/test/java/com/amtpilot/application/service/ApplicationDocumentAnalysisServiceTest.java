package com.amtpilot.application.service;

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
import com.amtpilot.application.exception.DocumentNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentAnalysisServiceTest {

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
    void updatesExistingDocumentAnalysis() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        Application application = org.mockito.Mockito.mock(
                Application.class);

        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        ApplicationDocumentAnalysis existingAnalysis = org.mockito.Mockito.mock(
                ApplicationDocumentAnalysis.class);

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
                .thenReturn(Optional.of(existingAnalysis));

        when(analysisRepository.saveAndFlush(existingAnalysis))
                .thenReturn(existingAnalysis);

        ApplicationDocumentAnalysis result = analysisService.analyze(
                userId,
                applicationId,
                documentId);

        assertThat(result)
                .isSameAs(existingAnalysis);

        verify(existingAnalysis)
                .update(aiResult);

        verify(analysisRepository)
                .saveAndFlush(existingAnalysis);
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