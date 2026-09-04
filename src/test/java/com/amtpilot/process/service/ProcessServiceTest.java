package com.amtpilot.process.service;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.entity.Authority;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.repository.ProcessDefinitionRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private ProcessDefinitionRepository processDefinitionRepository;

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

        List<ProcessResponse> result =
                processService.getProcessesByCity("Dortmund");

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

        List<ProcessResponse> result =
                processService.getProcessesByCity("Dortmund");

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
}