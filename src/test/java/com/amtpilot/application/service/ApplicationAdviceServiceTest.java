package com.amtpilot.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.ai.client.AiApplicationAdviceClient;
import com.amtpilot.ai.dto.AiApplicationAdviceRequest;
import com.amtpilot.ai.dto.AiApplicationAdviceResponse;
import com.amtpilot.ai.dto.AiExtractedFieldResponse;
import com.amtpilot.ai.dto.AiRequirementAssessmentResponse;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationAdvice;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ApplicationDocument;
import com.amtpilot.entity.ApplicationDocumentAnalysis;
import com.amtpilot.entity.OfficialSource;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.repository.ApplicationAdviceRepository;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationDocumentAnalysisRepository;
import com.amtpilot.repository.ApplicationRepository;

@ExtendWith(MockitoExtension.class)
class ApplicationAdviceServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationChecklistItemRepository checklistRepository;

    @Mock
    private ApplicationDocumentAnalysisRepository analysisRepository;

    @Mock
    private ApplicationAdviceRepository adviceRepository;

    @Mock
    private AiApplicationAdviceClient aiClient;

    private ApplicationAdviceService adviceService;

    @BeforeEach
    void setUp() {
        adviceService = new ApplicationAdviceService(
                applicationRepository,
                checklistRepository,
                analysisRepository,
                adviceRepository,
                aiClient);
    }

    @Test
    void buildsContextAndStoresApplicationAdvice() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();

        ProcessDefinition process = mock(ProcessDefinition.class);
        Application application = mock(Application.class);
        ApplicationChecklistItem checklistItem =
                mock(ApplicationChecklistItem.class);
        RequirementDefinition requirement =
                mock(RequirementDefinition.class);
        OfficialSource source = mock(OfficialSource.class);
        ApplicationDocumentAnalysis analysis =
                mock(ApplicationDocumentAnalysis.class);
        ApplicationDocument document =
                mock(ApplicationDocument.class);

        when(application.getProcess()).thenReturn(process);
        when(process.getCode()).thenReturn("ADDRESS_REGISTRATION");
        when(process.getTitle()).thenReturn("Address Registration");
        when(process.getCity()).thenReturn("Dortmund");

        when(checklistItem.getRequirement()).thenReturn(requirement);
        when(checklistItem.isCompleted()).thenReturn(true);
        when(requirement.getCode()).thenReturn("LANDLORD_CONFIRMATION");
        when(requirement.getTitle()).thenReturn("Landlord confirmation");
        when(requirement.isRequired()).thenReturn(true);
        when(requirement.getSource()).thenReturn(source);
        when(source.getUrl()).thenReturn("https://example.test/source");

        when(analysis.getDocument()).thenReturn(document);
        when(document.getOriginalFilename()).thenReturn("confirmation.pdf");
        when(document.getChecklistItem()).thenReturn(checklistItem);
        when(analysis.getDocumentType()).thenReturn("Confirmation");
        when(analysis.getPrimaryLanguage()).thenReturn("de");
        when(analysis.getSummary()).thenReturn("Registration confirmation");
        when(analysis.getExtractedFields()).thenReturn(List.of(
                new AiExtractedFieldResponse(
                        "full_name",
                        "Max Mustermann",
                        "Max Mustermann",
                        1)));
        when(analysis.getMissingOrUnclear()).thenReturn(List.of());
        when(analysis.getWarnings()).thenReturn(List.of());

        when(applicationRepository.findByIdAndUserId(
                applicationId,
                userId))
                .thenReturn(Optional.of(application));
        when(checklistRepository
                .findByApplicationIdOrderByRequirementTitleAsc(
                        applicationId))
                .thenReturn(List.of(checklistItem));
        when(analysisRepository
                .findByDocumentApplicationIdOrderByCreatedAtDesc(
                        applicationId))
                .thenReturn(List.of(analysis));

        AiApplicationAdviceResponse aiResponse =
                new AiApplicationAdviceResponse(
                        "READY_TO_SUBMIT",
                        "The application appears ready.",
                        List.of(
                                new AiRequirementAssessmentResponse(
                                        "LANDLORD_CONFIRMATION",
                                        "SATISFIED",
                                        "The confirmation is available.",
                                        List.of("confirmation.pdf"))),
                        List.of(),
                        List.of("Review and submit the application."),
                        List.of(),
                        "Guidance only; not legal advice.");

        when(aiClient.advise(any(
                AiApplicationAdviceRequest.class)))
                .thenReturn(aiResponse);
        when(adviceRepository.findByApplicationId(applicationId))
                .thenReturn(Optional.empty());
        when(adviceRepository.saveAndFlush(
                any(ApplicationAdvice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationAdvice result = adviceService.generate(
                userId,
                applicationId);

        assertThat(result.getReadiness())
                .isEqualTo("READY_TO_SUBMIT");
        assertThat(result.getNextSteps())
                .containsExactly(
                        "Review and submit the application.");

        ArgumentCaptor<AiApplicationAdviceRequest> requestCaptor =
                ArgumentCaptor.forClass(
                        AiApplicationAdviceRequest.class);

        verify(aiClient).advise(requestCaptor.capture());

        AiApplicationAdviceRequest request =
                requestCaptor.getValue();

        assertThat(request.processCode())
                .isEqualTo("ADDRESS_REGISTRATION");
        assertThat(request.requirements()).hasSize(1);
        assertThat(request.documents()).hasSize(1);
        assertThat(request.documents().getFirst().requirementCode())
                .isEqualTo("LANDLORD_CONFIRMATION");
    }
}
