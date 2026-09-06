package com.amtpilot.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.ChecklistItemResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.application.dto.UpdateApplicationRequest;
import com.amtpilot.application.dto.UpdateChecklistItemRequest;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.application.exception.ChecklistItemNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.enums.ApplicationStatus;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationRepository;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.RequirementDefinitionRepository;
import com.amtpilot.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

        @Mock
        private RequirementDefinitionRepository requirementRepository;

        @Mock
        private ApplicationChecklistItemRepository checklistRepository;

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

                RequirementDefinition passport = org.mockito.Mockito.mock(RequirementDefinition.class);

                RequirementDefinition registrationForm = org.mockito.Mockito.mock(RequirementDefinition.class);

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

                when(requirementRepository
                                .findByProcessIdOrderByTitleAsc(processId))
                                .thenReturn(List.of(
                                                passport,
                                                registrationForm));

                when(applicationRepository
                                .saveAndFlush(any(Application.class)))
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

                ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);

                verify(applicationRepository)
                                .saveAndFlush(applicationCaptor.capture());

                Application savedApplication = applicationCaptor.getValue();

                assertSame(user, savedApplication.getUser());
                assertSame(process, savedApplication.getProcess());

                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<ApplicationChecklistItem>> checklistCaptor = ArgumentCaptor.forClass(List.class);

                verify(checklistRepository)
                                .saveAll(checklistCaptor.capture());

                List<ApplicationChecklistItem> checklistItems = checklistCaptor.getValue();

                assertEquals(2, checklistItems.size());

                assertSame(
                                savedApplication,
                                checklistItems.get(0).getApplication());
                assertSame(
                                passport,
                                checklistItems.get(0).getRequirement());
                assertFalse(checklistItems.get(0).isCompleted());

                assertSame(
                                savedApplication,
                                checklistItems.get(1).getApplication());
                assertSame(
                                registrationForm,
                                checklistItems.get(1).getRequirement());
                assertFalse(checklistItems.get(1).isCompleted());
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
                                .saveAndFlush(any(Application.class));
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

                                .saveAndFlush(any(Application.class));
        }

        @Test
        void returnsApplicationsForUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID processId = UUID.randomUUID();

                Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");
                Instant updatedAt = Instant.parse("2026-09-05T11:00:00Z");

                Application application = org.mockito.Mockito.mock(Application.class);

                ProcessDefinition process = org.mockito.Mockito.mock(ProcessDefinition.class);

                when(applicationRepository
                                .findByUserIdOrderByCreatedAtDesc(userId))
                                .thenReturn(List.of(application));

                when(application.getId()).thenReturn(applicationId);
                when(application.getProcess()).thenReturn(process);
                when(application.getStatus())
                                .thenReturn(ApplicationStatus.ACTION_REQUIRED);
                when(application.getCompleteness()).thenReturn((short) 50);
                when(application.getCreatedAt()).thenReturn(createdAt);
                when(application.getUpdatedAt()).thenReturn(updatedAt);

                when(process.getId()).thenReturn(processId);
                when(process.getCode())
                                .thenReturn("DO_ADDRESS_REGISTRATION");
                when(process.getTitle())
                                .thenReturn("Address Registration");

                List<ApplicationResponse> result = applicationService.getApplicationsForUser(userId);

                ApplicationResponse expected = new ApplicationResponse(
                                applicationId,
                                processId,
                                "DO_ADDRESS_REGISTRATION",
                                "Address Registration",
                                ApplicationStatus.ACTION_REQUIRED,
                                50,
                                createdAt,
                                updatedAt);

                assertEquals(List.of(expected), result);

                verify(applicationRepository)
                                .findByUserIdOrderByCreatedAtDesc(userId);
        }

        @Test
        void returnsApplicationOwnedByUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID processId = UUID.randomUUID();

                Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");

                Application application = org.mockito.Mockito.mock(Application.class);

                ProcessDefinition process = org.mockito.Mockito.mock(ProcessDefinition.class);

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.of(application));

                when(application.getId()).thenReturn(applicationId);
                when(application.getProcess()).thenReturn(process);
                when(application.getStatus())
                                .thenReturn(ApplicationStatus.DRAFT);
                when(application.getCompleteness()).thenReturn((short) 0);
                when(application.getCreatedAt()).thenReturn(createdAt);
                when(application.getUpdatedAt()).thenReturn(createdAt);

                when(process.getId()).thenReturn(processId);
                when(process.getCode())
                                .thenReturn("DO_ADDRESS_REGISTRATION");
                when(process.getTitle())
                                .thenReturn("Address Registration");

                ApplicationResponse result = applicationService.getApplicationForUser(
                                userId,
                                applicationId);

                assertEquals(applicationId, result.id());
                assertEquals(processId, result.processId());
                assertEquals(
                                "DO_ADDRESS_REGISTRATION",
                                result.processCode());
                assertEquals(ApplicationStatus.DRAFT, result.status());

                verify(applicationRepository)
                                .findByIdAndUserId(applicationId, userId);
        }

        @Test
        void rejectsApplicationNotOwnedByUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.empty());

                ApplicationNotFoundException exception = assertThrows(
                                ApplicationNotFoundException.class,
                                () -> applicationService.getApplicationForUser(
                                                userId,
                                                applicationId));

                assertEquals(
                                "Application not found: " + applicationId,
                                exception.getMessage());

                verify(applicationRepository)
                                .findByIdAndUserId(applicationId, userId);
        }

        @Test
        void updatesOwnedApplication() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID processId = UUID.randomUUID();

                Instant createdAt = Instant.parse("2026-09-05T10:00:00Z");
                Instant updatedAt = Instant.parse("2026-09-06T10:00:00Z");

                Application application = org.mockito.Mockito.mock(Application.class);
                ProcessDefinition process = org.mockito.Mockito.mock(ProcessDefinition.class);

                UpdateApplicationRequest request = new UpdateApplicationRequest(
                                ApplicationStatus.ACTION_REQUIRED);

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.of(application));

                when(applicationRepository.saveAndFlush(application))
                                .thenReturn(application);

                when(application.getId()).thenReturn(applicationId);
                when(application.getProcess()).thenReturn(process);
                when(application.getStatus())
                                .thenReturn(ApplicationStatus.ACTION_REQUIRED);
                when(application.getCompleteness()).thenReturn((short) 40);
                when(application.getCreatedAt()).thenReturn(createdAt);
                when(application.getUpdatedAt()).thenReturn(updatedAt);

                when(process.getId()).thenReturn(processId);
                when(process.getCode()).thenReturn("ADDRESS_REGISTRATION");
                when(process.getTitle()).thenReturn("Address Registration");

                ApplicationResponse result = applicationService.update(
                                userId,
                                applicationId,
                                request);

                assertEquals(
                                ApplicationStatus.ACTION_REQUIRED,
                                result.status());
                assertEquals(40, result.completeness());

                verify(application)
                                .changeStatus(ApplicationStatus.ACTION_REQUIRED);
                verify(applicationRepository).saveAndFlush(application);
        }

        @Test
        void rejectsUpdatingApplicationNotOwnedByUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();

                UpdateApplicationRequest request = new UpdateApplicationRequest(
                                ApplicationStatus.ACTION_REQUIRED);

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ApplicationNotFoundException.class,
                                () -> applicationService.update(
                                                userId,
                                                applicationId,
                                                request));

                verify(applicationRepository, never())
                                .saveAndFlush(any(Application.class));
        }

        @Test
        void returnsChecklistForOwnedApplication() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID checklistItemId = UUID.randomUUID();
                UUID requirementId = UUID.randomUUID();

                Application application = org.mockito.Mockito.mock(Application.class);

                ApplicationChecklistItem checklistItem = org.mockito.Mockito.mock(
                                ApplicationChecklistItem.class);

                RequirementDefinition requirement = org.mockito.Mockito.mock(
                                RequirementDefinition.class);

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.of(application));

                when(checklistRepository
                                .findByApplicationIdOrderByRequirementTitleAsc(
                                                applicationId))
                                .thenReturn(List.of(checklistItem));

                when(checklistItem.getId())
                                .thenReturn(checklistItemId);
                when(checklistItem.getRequirement())
                                .thenReturn(requirement);
                when(checklistItem.isCompleted())
                                .thenReturn(true);

                when(requirement.getId()).thenReturn(requirementId);
                when(requirement.getCode()).thenReturn("PASSPORT");
                when(requirement.getTitle()).thenReturn("Passport");
                when(requirement.isRequired()).thenReturn(true);

                List<ChecklistItemResponse> result = applicationService.getChecklistForUser(
                                userId,
                                applicationId);

                ChecklistItemResponse expected = new ChecklistItemResponse(
                                checklistItemId,
                                requirementId,
                                "PASSPORT",
                                "Passport",
                                true,
                                true);

                assertEquals(List.of(expected), result);

                verify(applicationRepository)
                                .findByIdAndUserId(applicationId, userId);

                verify(checklistRepository)
                                .findByApplicationIdOrderByRequirementTitleAsc(
                                                applicationId);
        }

        @Test
        void rejectsChecklistForApplicationNotOwnedByUser() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();

                when(applicationRepository.findByIdAndUserId(
                                applicationId,
                                userId))
                                .thenReturn(Optional.empty());

                assertThrows(
                                ApplicationNotFoundException.class,
                                () -> applicationService.getChecklistForUser(
                                                userId,
                                                applicationId));

                verify(checklistRepository, never())
                                .findByApplicationIdOrderByRequirementTitleAsc(
                                                applicationId);
        }

        @Test
        void updatesChecklistItemAndRecalculatesCompleteness() {
                UUID userId = UUID.randomUUID();
                UUID applicationId = UUID.randomUUID();
                UUID checklistItemId = UUID.randomUUID();
                UUID requirementId = UUID.randomUUID();

                Application application = org.mockito.Mockito.mock(Application.class);

                ApplicationChecklistItem completedItem = org.mockito.Mockito.mock(
                                ApplicationChecklistItem.class);

                ApplicationChecklistItem pendingItem = org.mockito.Mockito.mock(
                                ApplicationChecklistItem.class);

                ApplicationChecklistItem optionalItem = org.mockito.Mockito.mock(
                                ApplicationChecklistItem.class);

                RequirementDefinition completedRequirement = org.mockito.Mockito.mock(
                                RequirementDefinition.class);

                RequirementDefinition pendingRequirement = org.mockito.Mockito.mock(
                                RequirementDefinition.class);

                RequirementDefinition optionalRequirement = org.mockito.Mockito.mock(
                                RequirementDefinition.class);

                when(checklistRepository
                                .findByIdAndApplicationUserId(
                                                checklistItemId,
                                                userId))
                                .thenReturn(Optional.of(completedItem));

                when(completedItem.getApplication())
                                .thenReturn(application);
                when(application.getId()).thenReturn(applicationId);

                when(checklistRepository
                                .findByApplicationIdOrderByRequirementTitleAsc(
                                                applicationId))
                                .thenReturn(List.of(
                                                completedItem,
                                                pendingItem,
                                                optionalItem));

                when(completedItem.getId())
                                .thenReturn(checklistItemId);
                when(completedItem.getRequirement())
                                .thenReturn(completedRequirement);
                when(completedItem.isCompleted()).thenReturn(true);

                when(pendingItem.getRequirement())
                                .thenReturn(pendingRequirement);
                when(pendingItem.isCompleted()).thenReturn(false);

                when(optionalItem.getRequirement())
                                .thenReturn(optionalRequirement);
                // when(optionalItem.isCompleted()).thenReturn(false);

                when(completedRequirement.getId())
                                .thenReturn(requirementId);
                when(completedRequirement.getCode())
                                .thenReturn("PASSPORT");
                when(completedRequirement.getTitle())
                                .thenReturn("Passport");
                when(completedRequirement.isRequired())
                                .thenReturn(true);

                when(pendingRequirement.isRequired())
                                .thenReturn(true);

                when(optionalRequirement.isRequired())
                                .thenReturn(false);

                UpdateChecklistItemRequest request = new UpdateChecklistItemRequest(true);

                ChecklistItemResponse response = applicationService.updateChecklistItem(
                                userId,
                                checklistItemId,
                                request);

                assertEquals(checklistItemId, response.id());
                assertEquals("PASSPORT", response.requirementCode());
                assertEquals(true, response.completed());

                verify(completedItem).updateCompleted(true);
                verify(checklistRepository)
                                .saveAndFlush(completedItem);
                verify(application).updateCompleteness(50);
                verify(applicationRepository)
                                .saveAndFlush(application);
        }

        @Test
        void rejectsChecklistItemNotOwnedByUser() {
                UUID userId = UUID.randomUUID();
                UUID checklistItemId = UUID.randomUUID();

                when(checklistRepository
                                .findByIdAndApplicationUserId(
                                                checklistItemId,
                                                userId))
                                .thenReturn(Optional.empty());

                ChecklistItemNotFoundException exception = assertThrows(
                                ChecklistItemNotFoundException.class,
                                () -> applicationService.updateChecklistItem(
                                                userId,
                                                checklistItemId,
                                                new UpdateChecklistItemRequest(true)));

                assertEquals(
                                "Checklist item not found: " + checklistItemId,
                                exception.getMessage());

                verify(checklistRepository, never())
                                .saveAndFlush(
                                                any(ApplicationChecklistItem.class));

                verify(applicationRepository, never())
                                .saveAndFlush(any(Application.class));
        }
}
