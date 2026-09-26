package com.amtpilot.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.enums.ApplicationStatus;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationAnalysisWorkerTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationDocumentRepository documentRepository;

    @Mock
    private ApplicationDocumentAnalysisService documentAnalysisService;

    @Mock
    private ApplicationAdviceService adviceService;

    private ApplicationAnalysisWorker worker;

    @BeforeEach
    void setUp() {
        worker = new ApplicationAnalysisWorker(
                applicationRepository,
                documentRepository,
                documentAnalysisService,
                adviceService);
    }

    @Test
    void marksCompleteAnalysisAsReadyToSubmit() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        Application application = analyzingApplication(100);
        ApplicationDocument document = document(documentId);
        ApplicationDocumentAnalysis analysis = analysis(List.of());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(document));

        when(documentAnalysisService.analyze(
                userId,
                applicationId,
                documentId))
                .thenReturn(analysis);

        worker.analyze(userId, applicationId);

        assertThat(application.getStatus())
                .isEqualTo(ApplicationStatus.READY_TO_SUBMIT);

        verify(adviceService).generate(
                userId,
                applicationId);

        verify(applicationRepository)
                .saveAndFlush(application);
    }

    @Test
    void marksMissingAiInformationAsActionRequired() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        Application application = analyzingApplication(100);
        ApplicationDocument document = document(documentId);
        ApplicationDocumentAnalysis analysis = analysis(
                List.of("Issue date is missing"));

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(document));

        when(documentAnalysisService.analyze(
                userId,
                applicationId,
                documentId))
                .thenReturn(analysis);

        worker.analyze(userId, applicationId);

        assertThat(application.getStatus())
                .isEqualTo(ApplicationStatus.ACTION_REQUIRED);

        verify(adviceService).generate(
                userId,
                applicationId);
    }

    @Test
    void marksIncompleteChecklistAsActionRequired() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        Application application = analyzingApplication(50);
        ApplicationDocument document = document(documentId);
        ApplicationDocumentAnalysis analysis = analysis(List.of());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(document));

        when(documentAnalysisService.analyze(
                userId,
                applicationId,
                documentId))
                .thenReturn(analysis);

        worker.analyze(userId, applicationId);

        assertThat(application.getStatus())
                .isEqualTo(ApplicationStatus.ACTION_REQUIRED);

        verify(adviceService).generate(
                userId,
                applicationId);

        verify(applicationRepository)
                .saveAndFlush(application);
    }

    @Test
    void marksApplicationAsActionRequiredWhenNoDocumentsExist() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        Application application = analyzingApplication(100);

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of());

        worker.analyze(userId, applicationId);

        assertThat(application.getStatus())
                .isEqualTo(ApplicationStatus.ACTION_REQUIRED);

        verifyNoInteractions(documentAnalysisService);

        verify(adviceService).generate(
                userId,
                applicationId);

        verify(applicationRepository)
                .saveAndFlush(application);
    }

    @Test
    void marksApplicationAsNeedsReviewWhenAnalysisFails() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        Application application = analyzingApplication(100);
        ApplicationDocument document = document(documentId);

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));

        when(documentRepository
                .findByApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(document));

        when(documentAnalysisService.analyze(
                userId,
                applicationId,
                documentId))
                .thenThrow(new IllegalStateException(
                        "AI analysis failed"));

        worker.analyze(userId, applicationId);

        assertThat(application.getStatus())
                .isEqualTo(ApplicationStatus.NEEDS_REVIEW);

        verifyNoInteractions(adviceService);
    }

    private Application analyzingApplication(int completeness) {
        User user = org.mockito.Mockito.mock(User.class);
        ProcessDefinition process = org.mockito.Mockito.mock(
                ProcessDefinition.class);
        Application application = new Application(user, process);

        application.updateCompleteness(completeness);
        application.changeStatus(ApplicationStatus.ANALYZING);

        return application;
    }

    private ApplicationDocument document(UUID documentId) {
        ApplicationDocument document = org.mockito.Mockito.mock(
                ApplicationDocument.class);

        when(document.getId())
                .thenReturn(documentId);

        return document;
    }

    private ApplicationDocumentAnalysis analysis(
            List<String> missingOrUnclear) {

        ApplicationDocumentAnalysis analysis = org.mockito.Mockito.mock(
                ApplicationDocumentAnalysis.class);

        when(analysis.getMissingOrUnclear())
                .thenReturn(missingOrUnclear);

        when(analysis.getWarnings())
                .thenReturn(List.of());

        return analysis;
    }
}
