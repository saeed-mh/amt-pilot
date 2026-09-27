package com.amtpilot.application.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationAdvice;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.enums.ApplicationStatus;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

@Service
public class ApplicationAnalysisWorker {

    private static final Logger log = LoggerFactory.getLogger(
            ApplicationAnalysisWorker.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final ApplicationDocumentAnalysisService documentAnalysisService;
    private final ApplicationAdviceService adviceService;

    public ApplicationAnalysisWorker(
            ApplicationRepository applicationRepository,
            ApplicationDocumentRepository documentRepository,
            ApplicationDocumentAnalysisService documentAnalysisService,
            ApplicationAdviceService adviceService) {

        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.documentAnalysisService = documentAnalysisService;
        this.adviceService = adviceService;
    }

    @Async
    public void analyze(
            UUID userId,
            UUID applicationId) {

        try {
            Application application = applicationRepository
                    .findByIdAndUserId(applicationId, userId)
                    .orElseThrow(
                            () -> new ApplicationNotFoundException(
                                    applicationId));

            List<ApplicationDocument> documents = documentRepository
                    .findByApplicationIdOrderByCreatedAtDesc(
                            applicationId);

            boolean actionRequired = documents.isEmpty()
                    || application.getCompleteness() < 100;

            for (ApplicationDocument document : documents) {
                ApplicationDocumentAnalysis analysis = documentAnalysisService.analyze(
                        userId,
                        applicationId,
                        document.getId());

                if (!analysis.getMissingOrUnclear().isEmpty()) {
                    actionRequired = true;
                }

                if (!analysis.getWarnings().isEmpty()) {
                    actionRequired = true;
                }
            }

            ApplicationAdvice advice = adviceService.generate(
                    userId,
                    applicationId);

            ApplicationStatus finalStatus = statusFromAdvice(
                    advice.getReadiness());

            if (actionRequired
                    && finalStatus == ApplicationStatus.READY_TO_SUBMIT) {
                finalStatus = ApplicationStatus.ACTION_REQUIRED;
            }

            finishAnalysis(
                    userId,
                    applicationId,
                    finalStatus);

        } catch (RuntimeException exception) {
            log.error(
                    "AI analysis failed for application {}",
                    applicationId,
                    exception);

            finishAnalysis(
                    userId,
                    applicationId,
                    ApplicationStatus.NEEDS_REVIEW);
        }
    }

    private ApplicationStatus statusFromAdvice(String readiness) {
        return switch (readiness) {
            case "READY_TO_SUBMIT" -> ApplicationStatus.READY_TO_SUBMIT;
            case "ACTION_REQUIRED" -> ApplicationStatus.ACTION_REQUIRED;
            default -> ApplicationStatus.NEEDS_REVIEW;
        };
    }

    private void finishAnalysis(
            UUID userId,
            UUID applicationId,
            ApplicationStatus finalStatus) {

        applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .filter(application -> application.getStatus() == ApplicationStatus.ANALYZING)
                .ifPresent(application -> {
                    application.changeStatus(finalStatus);
                    applicationRepository.saveAndFlush(application);
                });
    }
}
