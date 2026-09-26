package com.amtpilot.application.service;

import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.amtpilot.ai.client.AiDocumentAnalysisClient;
import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.application.exception.DocumentNotFoundException;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationDocumentRepository;

@Service
public class ApplicationDocumentAnalysisService {

    private final ApplicationDocumentRepository documentRepository;
    private final ApplicationDocumentAnalysisRepository analysisRepository;
    private final DocumentStorageService storageService;
    private final AiDocumentAnalysisClient aiClient;

    public ApplicationDocumentAnalysisService(
            ApplicationDocumentRepository documentRepository,
            ApplicationDocumentAnalysisRepository analysisRepository,
            DocumentStorageService storageService,
            AiDocumentAnalysisClient aiClient) {

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

        Resource resource = storageService.load(
                document.getStoragePath());

        AiDocumentAnalysisResponse result = aiClient.analyze(
                resource,
                document.getOriginalFilename());

        return analysisRepository
                .findByDocumentId(documentId)
                .map(existingAnalysis -> {
                    existingAnalysis.update(result);

                    return analysisRepository.saveAndFlush(
                            existingAnalysis);
                })
                .orElseGet(() -> analysisRepository.saveAndFlush(
                        new ApplicationDocumentAnalysis(
                                document,
                                result)));
    }
}