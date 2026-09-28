package com.amtpilot.application.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.amtpilot.ai.client.AiApplicationAdviceClient;
import com.amtpilot.ai.dto.AiAnalyzedDocumentRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.dto.AiApplicationRequirementRequest;
import com.amtpilot.ai.dto.AiDocumentAnalysisResponse;
import com.amtpilot.ai.dto.AiOfficialProcessGuideRequest;
import com.amtpilot.application.dto.ApplicationAdviceResponse;
import com.amtpilot.application.dto.UpdateAdviceAnswersRequest;
import com.amtpilot.application.exception.ApplicationAdviceNotFoundException;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.InvalidAdviceAnswersException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationAdvice;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.ProcessGuide;
import com.amtpilot.repository.ApplicationAdviceRepository;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationRepository;
import com.amtpilot.repository.ProcessGuideRepository;

@Service
public class ApplicationAdviceService {

    private static final String ADDITIONAL_CONTEXT_KEY =
            "_additional_context";

    private final ApplicationRepository applicationRepository;
    private final ApplicationChecklistItemRepository checklistRepository;
    private final ApplicationDocumentAnalysisRepository analysisRepository;
    private final ApplicationAdviceRepository adviceRepository;
    private final ProcessGuideRepository processGuideRepository;
    private final AiApplicationAdviceClient aiClient;

    public ApplicationAdviceService(
            ApplicationRepository applicationRepository,
            ApplicationChecklistItemRepository checklistRepository,
            ApplicationDocumentAnalysisRepository analysisRepository,
            ApplicationAdviceRepository adviceRepository,
            ProcessGuideRepository processGuideRepository,
            AiApplicationAdviceClient aiClient) {

        this.applicationRepository = applicationRepository;
        this.checklistRepository = checklistRepository;
        this.analysisRepository = analysisRepository;
        this.adviceRepository = adviceRepository;
        this.processGuideRepository = processGuideRepository;
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

        Optional<ApplicationAdvice> existingAdvice = adviceRepository
                .findByApplicationId(applicationId);

        Map<String, String> userAnswers = existingAdvice
                .map(ApplicationAdvice::getUserAnswers)
                .orElseGet(Map::of);

        AiApplicationAdviceResponse result = aiClient.advise(
                toAiRequest(
                        application,
                        checklistItems,
                        analyses,
                        userAnswers));

        return existingAdvice
                .map(storedAdvice -> {
                    storedAdvice.update(result);
                    return adviceRepository.saveAndFlush(
                            storedAdvice);
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

    public ApplicationAdviceResponse updateAnswers(
            UUID userId,
            UUID applicationId,
            UpdateAdviceAnswersRequest request) {

        ownedApplication(userId, applicationId);

        ApplicationAdvice advice = adviceRepository
                .findByApplicationId(applicationId)
                .orElseThrow(
                        () -> new ApplicationAdviceNotFoundException(
                                applicationId));

        boolean containsUnsupportedAnswer = request.answers()
                .keySet()
                .stream()
                .anyMatch(key -> !ADDITIONAL_CONTEXT_KEY.equals(key)
                        && !advice.getQuestionsForUser().contains(key));

        if (containsUnsupportedAnswer) {
            throw new InvalidAdviceAnswersException();
        }

        Map<String, String> normalizedAnswers = new LinkedHashMap<>();
        request.answers().forEach(
                (question, answer) -> normalizedAnswers.put(
                        question,
                        answer.trim()));

        advice.replaceUserAnswers(normalizedAnswers);

        return toResponse(
                adviceRepository.saveAndFlush(advice));
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
            List<ApplicationDocumentAnalysis> analyses,
            Map<String, String> userAnswers) {

        ProcessDefinition process = application.getProcess();

        List<AiApplicationRequirementRequest> requirements = checklistItems
                .stream()
                .map(item -> new AiApplicationRequirementRequest(
                        item.getRequirement().getCode(),
                        item.getRequirement().getTitle(),
                        item.getRequirement().isRequired(),
                        item.isCompleted(),
                        item.getRequirement().getSource().getTitle(),
                        item.getRequirement().getSource().getUrl()))
                .toList();

        List<AiAnalyzedDocumentRequest> documents = analyses
                .stream()
                .map(this::toAnalyzedDocument)
                .toList();

        AiOfficialProcessGuideRequest officialGuide =
                processGuideRepository.findByProcessId(process.getId())
                        .map(this::toOfficialGuide)
                        .orElse(null);

        return new AiApplicationAdviceRequest(
                process.getCode(),
                process.getTitle(),
                process.getCity(),
                officialGuide,
                requirements,
                documents,
                userAnswers);
    }

    private AiOfficialProcessGuideRequest toOfficialGuide(
            ProcessGuide guide) {

        return new AiOfficialProcessGuideRequest(
                guide.getOverviewEn(),
                guide.getEligibilityEn(),
                List.copyOf(guide.getStepsEn()),
                guide.getDeadlineEn(),
                guide.getFeeEn(),
                guide.isAppointmentRequired(),
                guide.getAppointmentInformationEn(),
                guide.getAppointmentUrl(),
                guide.getSourceTitle(),
                guide.getSourceUrl(),
                guide.getVerifiedAt());
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
                advice.getUserAnswers(),
                advice.getOfficialSourceReferences(),
                advice.getDisclaimer(),
                advice.getCreatedAt(),
                advice.getUpdatedAt());
    }
}
