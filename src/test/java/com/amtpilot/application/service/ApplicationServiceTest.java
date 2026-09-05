package com.amtpilot.application.service;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.enums.ApplicationStatus;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ApplicationRepository;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProcessDefinitionRepository processRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @Test
    void createsDraftApplicationForUser() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

        User user = new User(
                "student@example.com",
                "hashed-password");

        ProcessDefinition process = org.mockito.Mockito.mock(ProcessDefinition.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(processRepository.findById(processId))
                .thenReturn(Optional.of(process));

        when(process.isActive()).thenReturn(true);
        when(process.getId()).thenReturn(processId);
        when(process.getCode())
                .thenReturn("DO_ADDRESS_REGISTRATION");
        when(process.getTitle())
                .thenReturn("Address Registration");

        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CreateApplicationRequest request = new CreateApplicationRequest(processId);

        ApplicationResponse response = applicationService.create(userId, request);

        assertEquals(processId, response.processId());
        assertEquals(
                "DO_ADDRESS_REGISTRATION",
                response.processCode());
        assertEquals(
                "Address Registration",
                response.processTitle());
        assertEquals(ApplicationStatus.DRAFT, response.status());
        assertEquals(0, response.completeness());

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);

        verify(applicationRepository).save(captor.capture());

        Application savedApplication = captor.getValue();

        assertSame(user, savedApplication.getUser());
        assertSame(process, savedApplication.getProcess());
    }

    @Test
    void rejectsUnknownProcess() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

        User user = new User(
                "student@example.com",
                "hashed-password");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(processRepository.findById(processId))
                .thenReturn(Optional.empty());

        CreateApplicationRequest request = new CreateApplicationRequest(processId);

        ProcessNotFoundException exception = assertThrows(
                ProcessNotFoundException.class,
                () -> applicationService.create(userId, request));

        assertEquals(
                "Process not found: " + processId,
                exception.getMessage());

        verify(applicationRepository, never())
                .save(any(Application.class));
    }

    @Test
    void rejectsInactiveProcess() {
        UUID userId = UUID.randomUUID();
        UUID processId = UUID.randomUUID();

        User user = new User(
                "student@example.com",
                "hashed-password");

        ProcessDefinition process = org.mockito.Mockito.mock(ProcessDefinition.class);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(processRepository.findById(processId))
                .thenReturn(Optional.of(process));

        when(process.isActive()).thenReturn(false);

        CreateApplicationRequest request = new CreateApplicationRequest(processId);

        assertThrows(
                ProcessNotFoundException.class,
                () -> applicationService.create(userId, request));

        verify(applicationRepository, never())
                .save(any(Application.class));
    }
}