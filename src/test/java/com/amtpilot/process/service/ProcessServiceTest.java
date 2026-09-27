package com.amtpilot.process.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.entity.Authority;
import com.amtpilot.entity.OfficialSource;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.ProcessGuide;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.process.dto.ProcessGuideResponse;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.process.dto.RequirementResponse;
import com.amtpilot.process.exception.ProcessGuideNotFoundException;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.ProcessGuideRepository;
import com.amtpilot.repository.RequirementDefinitionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

        @Mock
        private ProcessDefinitionRepository processDefinitionRepository;

        @Mock
        private RequirementDefinitionRepository requirementRepository;

        @Mock
        private ProcessGuideRepository processGuideRepository;

        @InjectMocks
        private ProcessService processService;

        @Test
        void returnsProcessesForCityAndMarksAvailableGuide() {
                UUID processId = UUID.randomUUID();
                UUID authorityId = UUID.randomUUID();

                Authority authority = mock(Authority.class);
                ProcessDefinition process = mock(ProcessDefinition.class);
                ProcessGuide guide = mock(ProcessGuide.class);

                when(processDefinitionRepository
                                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc("Dortmund"))
                                .thenReturn(List.of(process));

                when(process.getId()).thenReturn(processId);
                when(process.getCode())
                                .thenReturn("DO_STUDENT_RESIDENCE_EXTENSION");
                when(process.getTitle())
                                .thenReturn("Student Residence Permit Extension");
                when(process.getCity()).thenReturn("Dortmund");
                when(process.getDomain()).thenReturn("IMMIGRATION");
                when(process.getVersion()).thenReturn(1);
                when(process.getAuthority()).thenReturn(authority);
                when(authority.getId()).thenReturn(authorityId);
                when(authority.getName())
                                .thenReturn("Dortmund Immigration Office");
                when(processGuideRepository.findByProcessIdIn(List.of(processId)))
                                .thenReturn(List.of(guide));
                when(guide.getProcess()).thenReturn(process);

                List<ProcessResponse> result = processService.getProcessesByCity("Dortmund");

                assertThat(result).containsExactly(
                                new ProcessResponse(
                                                processId,
                                                "DO_STUDENT_RESIDENCE_EXTENSION",
                                                "Student Residence Permit Extension",
                                                "Dortmund",
                                                "IMMIGRATION",
                                                1,
                                                authorityId,
                                                "Dortmund Immigration Office",
                                                true));

                verify(processDefinitionRepository)
                                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(
                                                "Dortmund");
        }

        @Test
        void handlesProcessWithoutAuthorityOrGuide() {
                UUID processId = UUID.randomUUID();
                ProcessDefinition process = mock(ProcessDefinition.class);

                when(processDefinitionRepository
                                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc("Dortmund"))
                                .thenReturn(List.of(process));

                when(process.getId()).thenReturn(processId);
                when(process.getCode()).thenReturn("DO_GENERAL_PROCESS");
                when(process.getTitle()).thenReturn("General Process");
                when(process.getCity()).thenReturn("Dortmund");
                when(process.getDomain()).thenReturn("GENERAL");
                when(process.getVersion()).thenReturn(1);
                when(process.getAuthority()).thenReturn(null);
                when(processGuideRepository.findByProcessIdIn(List.of(processId)))
                                .thenReturn(List.of());

                List<ProcessResponse> result = processService.getProcessesByCity("Dortmund");

                assertThat(result).containsExactly(
                                new ProcessResponse(
                                                processId,
                                                "DO_GENERAL_PROCESS",
                                                "General Process",
                                                "Dortmund",
                                                "GENERAL",
                                                1,
                                                null,
                                                null,
                                                false));
        }

        @Test
        void returnsRequirementsForProcess() {
                UUID processId = UUID.randomUUID();
                UUID requirementId = UUID.randomUUID();
                UUID sourceId = UUID.randomUUID();

                ProcessDefinition process = mock(ProcessDefinition.class);
                RequirementDefinition requirement = mock(RequirementDefinition.class);
                OfficialSource source = mock(OfficialSource.class);

                when(processDefinitionRepository.findById(processId))
                                .thenReturn(Optional.of(process));
                when(process.isActive()).thenReturn(true);

                when(requirementRepository
                                .findByProcessIdOrderByTitleAsc(processId))
                                .thenReturn(List.of(requirement));

                when(requirement.getId()).thenReturn(requirementId);
                when(requirement.getCode()).thenReturn("PASSPORT");
                when(requirement.getTitle()).thenReturn("Valid passport");
                when(requirement.isRequired()).thenReturn(true);
                when(requirement.getVersion()).thenReturn(1);
                when(requirement.getSource()).thenReturn(source);

                when(source.getId()).thenReturn(sourceId);
                when(source.getTitle())
                                .thenReturn("Dortmund Address Registration");
                when(source.getUrl())
                                .thenReturn("https://www.dortmund.de");

                List<RequirementResponse> result = processService.getRequirements(processId);

                assertThat(result).containsExactly(
                                new RequirementResponse(
                                                requirementId,
                                                "PASSPORT",
                                                "Valid passport",
                                                true,
                                                1,
                                                sourceId,
                                                "Dortmund Address Registration",
                                                "https://www.dortmund.de"));

                verify(processDefinitionRepository).findById(processId);
                verify(requirementRepository)
                                .findByProcessIdOrderByTitleAsc(processId);
        }

        @Test
        void returnsLocalizedGermanGuide() {
                UUID processId = UUID.randomUUID();
                ProcessDefinition process = mock(ProcessDefinition.class);
                ProcessGuide guide = mock(ProcessGuide.class);
                LocalDate verifiedAt = LocalDate.of(2026, 9, 27);

                when(processDefinitionRepository.findById(processId))
                                .thenReturn(Optional.of(process));
                when(process.isActive()).thenReturn(true);
                when(processGuideRepository.findByProcessId(processId))
                                .thenReturn(Optional.of(guide));
                when(guide.getOverviewDe()).thenReturn("Wohnsitz anmelden.");
                when(guide.getEligibilityDe()).thenReturn("Zuständigkeit prüfen.");
                when(guide.getStepsDe()).thenReturn(List.of("Unterlagen vorbereiten."));
                when(guide.getDeadlineDe()).thenReturn("Innerhalb von zwei Wochen.");
                when(guide.getFeeDe()).thenReturn("Gebührenfrei.");
                when(guide.isAppointmentRequired()).thenReturn(true);
                when(guide.getAppointmentInformationDe())
                                .thenReturn("Für persönliche Vorsprache Termin buchen.");
                when(guide.getAppointmentUrl()).thenReturn("https://example.com/appointment");
                when(guide.getSourceTitle()).thenReturn("Offizielle Quelle");
                when(guide.getSourceUrl()).thenReturn("https://example.com/source");
                when(guide.getVerifiedAt()).thenReturn(verifiedAt);

                ProcessGuideResponse result = processService.getGuide(processId, "de");

                assertThat(result).isEqualTo(
                                new ProcessGuideResponse(
                                                processId,
                                                "Wohnsitz anmelden.",
                                                "Zuständigkeit prüfen.",
                                                List.of("Unterlagen vorbereiten."),
                                                "Innerhalb von zwei Wochen.",
                                                "Gebührenfrei.",
                                                true,
                                                "Für persönliche Vorsprache Termin buchen.",
                                                "https://example.com/appointment",
                                                "Offizielle Quelle",
                                                "https://example.com/source",
                                                verifiedAt));
        }

        @Test
        void rejectsMissingGuide() {
                UUID processId = UUID.randomUUID();
                ProcessDefinition process = mock(ProcessDefinition.class);

                when(processDefinitionRepository.findById(processId))
                                .thenReturn(Optional.of(process));
                when(process.isActive()).thenReturn(true);
                when(processGuideRepository.findByProcessId(processId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> processService.getGuide(processId, "en"))
                                .isInstanceOf(ProcessGuideNotFoundException.class)
                                .hasMessage(
                                                "Process guide not found for process: "
                                                                + processId);
        }

        @Test
        void rejectsUnknownProcessWhenGettingRequirements() {
                UUID processId = UUID.randomUUID();

                when(processDefinitionRepository.findById(processId))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(
                                () -> processService.getRequirements(processId))
                                .isInstanceOf(ProcessNotFoundException.class)
                                .hasMessage("Process not found: " + processId);

                verifyNoInteractions(requirementRepository);
        }
}