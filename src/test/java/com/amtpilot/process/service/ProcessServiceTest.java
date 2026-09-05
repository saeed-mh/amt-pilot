package com.amtpilot.process.service;

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
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.process.dto.RequirementResponse;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.RequirementDefinitionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

        @Mock
        private ProcessDefinitionRepository processDefinitionRepository;

        @Mock
        private RequirementDefinitionRepository requirementRepository;

        @InjectMocks
        private ProcessService processService;

        @Test
        void returnsProcessesForCity() {
                UUID processId = UUID.randomUUID();
                UUID authorityId = UUID.randomUUID();

                Authority authority = mock(Authority.class);
                ProcessDefinition process = mock(ProcessDefinition.class);

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
                                                "Dortmund Immigration Office"));

                verify(processDefinitionRepository)
                                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(
                                                "Dortmund");
        }

        @Test
        void handlesProcessWithoutAuthority() {
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
                                                null));
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