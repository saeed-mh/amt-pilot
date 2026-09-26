package com.amtpilot.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.ai.client.AiDocumentAnalysisClient;
import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.application.dto.DocumentAnalysisResponse;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.DocumentNotFoundException;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;
import com.amtpilot.repository.ApplicationRepository;

@Service
public class ApplicationDocumentAnalysisService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationDocumentRepository documentRepository;
    private final ApplicationDocumentAnalysisRepository analysisRepository;
    private final DocumentStorageService storageService;
    private final AiDocumentAnalysisClient aiClient;

    public ApplicationDocumentAnalysisService(
            ApplicationRepository applicationRepository,
            ApplicationDocumentRepository documentRepository,
            ApplicationDocumentAnalysisRepository analysisRepository,
            DocumentStorageService storageService,
            AiDocumentAnalysisClient aiClient) {

        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.analysisRepository = analysisRepository;
        this.storageService = storageService;
        this.aiClient = aiClient;
    }

    public ApplicationDocumentAnalysis analyze(
            UUID userId,
            UUID applicationId,
            UUID documentId) {

        ApplicationDocument document = documentRepository
                .findByIdAndApplicationUserId(
                        documentId,
                        userId)
                .filter(item -> item.getApplication()
                        .getId()
                        .equals(applicationId))
                .orElseThrow(
                        () -> new DocumentNotFoundException(
                                documentId));

        var existingAnalysis = analysisRepository
                .findByDocumentId(documentId);

        if (existingAnalysis.isPresent()) {
            return existingAnalysis.get();
        }

        Resource resource = storageService.load(
                document.getStoragePath());

        AiDocumentAnalysisResponse result = aiClient.analyze(
                resource,
                document.getOriginalFilename());

        return analysisRepository.saveAndFlush(
                new ApplicationDocumentAnalysis(
                        document,
                        result));
    }

    @Transactional(readOnly = true)
    public List<DocumentAnalysisResponse> listForApplication(
            UUID userId,
            UUID applicationId) {

        applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));

        return analysisRepository
                .findByDocumentApplicationIdOrderByCreatedAtDesc(
                        applicationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DocumentAnalysisResponse toResponse(
            ApplicationDocumentAnalysis analysis) {

        ApplicationDocument document = analysis.getDocument();

        return new DocumentAnalysisResponse(
                analysis.getId(),
                document.getId(),
                document.getOriginalFilename(),
                analysis.getDocumentType(),
                analysis.getPrimaryLanguage(),
                analysis.getSummary(),
                analysis.getExtractedFields(),
                analysis.getMissingOrUnclear(),
                analysis.getWarnings(),
                analysis.getCreatedAt(),
                analysis.getUpdatedAt());
    }
}
