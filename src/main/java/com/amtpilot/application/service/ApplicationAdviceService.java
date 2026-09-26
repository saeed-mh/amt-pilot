package com.amtpilot.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.amtpilot.ai.client.AiApplicationAdviceClient;
import com.amtpilot.ai.dto.AiAnalyzedDocumentRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.dto.AiApplicationRequirementRequest;
import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.application.dto.ApplicationAdviceResponse;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationAdvice;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.repository.ApplicationAdviceRepository;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationRepository;

@Service
public class ApplicationAdviceService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationChecklistItemRepository checklistRepository;
    private final ApplicationDocumentAnalysisRepository analysisRepository;
    private final ApplicationAdviceRepository adviceRepository;
    private final AiApplicationAdviceClient aiClient;

    public ApplicationAdviceService(
            ApplicationRepository applicationRepository,
            ApplicationChecklistItemRepository checklistRepository,
            ApplicationDocumentAnalysisRepository analysisRepository,
            ApplicationAdviceRepository adviceRepository,
            AiApplicationAdviceClient aiClient) {

        this.applicationRepository = applicationRepository;
        this.checklistRepository = checklistRepository;
        this.analysisRepository = analysisRepository;
        this.adviceRepository = adviceRepository;
        this.aiClient = aiClient;
    }

    public ApplicationAdvice generate(
            UUID userId,
            UUID applicationId) {

        Application application = ownedApplication(
                userId,
                applicationId);

        List<ApplicationChecklistItem> checklistItems = checklistRepository
                .findByApplicationIdOrderByRequirementTitleAsc(
                        applicationId);

        List<ApplicationDocumentAnalysis> analyses = analysisRepository
                .findByDocumentApplicationIdOrderByCreatedAtDesc(
                        applicationId);

        AiApplicationAdviceResponse result = aiClient.advise(
                toAiRequest(
                        application,
                        checklistItems,
                        analyses));

        return adviceRepository
                .findByApplicationId(applicationId)
                .map(existingAdvice -> {
                    existingAdvice.update(result);
                    return adviceRepository.saveAndFlush(
                            existingAdvice);
                })
                .orElseGet(() -> adviceRepository.saveAndFlush(
                        new ApplicationAdvice(
                                application,
                                result)));
    }

    public Optional<ApplicationAdviceResponse> getForApplication(
            UUID userId,
            UUID applicationId) {

        ownedApplication(userId, applicationId);

        return adviceRepository
                .findByApplicationId(applicationId)
                .map(this::toResponse);
    }

    private Application ownedApplication(
            UUID userId,
            UUID applicationId) {

        return applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));
    }

    private AiApplicationAdviceRequest toAiRequest(
            Application application,
            List<ApplicationChecklistItem> checklistItems,
            List<ApplicationDocumentAnalysis> analyses) {

        ProcessDefinition process = application.getProcess();

        List<AiApplicationRequirementRequest> requirements = checklistItems
                .stream()
                .map(item -> new AiApplicationRequirementRequest(
                        item.getRequirement().getCode(),
                        item.getRequirement().getTitle(),
                        item.getRequirement().isRequired(),
                        item.isCompleted(),
                        item.getRequirement().getSource().getUrl()))
                .toList();

        List<AiAnalyzedDocumentRequest> documents = analyses
                .stream()
                .map(this::toAnalyzedDocument)
                .toList();

        return new AiApplicationAdviceRequest(
                process.getCode(),
                process.getTitle(),
                process.getCity(),
                requirements,
                documents);
    }

    private AiAnalyzedDocumentRequest toAnalyzedDocument(
            ApplicationDocumentAnalysis analysis) {

        ApplicationDocument document = analysis.getDocument();
        String requirementCode = document.getChecklistItem() == null
                ? null
                : document.getChecklistItem()
                        .getRequirement()
                        .getCode();

        AiDocumentAnalysisResponse documentAnalysis =
                new AiDocumentAnalysisResponse(
                        analysis.getDocumentType(),
                        analysis.getPrimaryLanguage(),
                        analysis.getSummary(),
                        analysis.getExtractedFields(),
                        analysis.getMissingOrUnclear(),
                        analysis.getWarnings());

        return new AiAnalyzedDocumentRequest(
                document.getOriginalFilename(),
                requirementCode,
                documentAnalysis);
    }

    private ApplicationAdviceResponse toResponse(
            ApplicationAdvice advice) {

        return new ApplicationAdviceResponse(
                advice.getId(),
                advice.getReadiness(),
                advice.getSummary(),
                advice.getRequirementAssessments(),
                advice.getInconsistencies(),
                advice.getNextSteps(),
                advice.getQuestionsForUser(),
                advice.getDisclaimer(),
                advice.getCreatedAt(),
                advice.getUpdatedAt());
    }
}
