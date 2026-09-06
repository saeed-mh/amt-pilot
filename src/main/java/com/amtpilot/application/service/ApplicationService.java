package com.amtpilot.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.ChecklistItemResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.application.dto.UpdateApplicationRequest;
import com.amtpilot.application.exception.ApplicationNotFoundException;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ApplicationChecklistItem;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ApplicationChecklistItemRepository;
import com.amtpilot.repository.ApplicationRepository;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.RequirementDefinitionRepository;
import com.amtpilot.repository.UserRepository;
import com.amtpilot.user.exception.UserNotFoundException;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ProcessDefinitionRepository processRepository;
    private final RequirementDefinitionRepository requirementRepository;
    private final ApplicationChecklistItemRepository checklistRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            ProcessDefinitionRepository processRepository,
            RequirementDefinitionRepository requirementRepository,
            ApplicationChecklistItemRepository checklistRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.requirementRepository = requirementRepository;
        this.checklistRepository = checklistRepository;
    }

    @Transactional
    public ApplicationResponse create(
            UUID userId,
            CreateApplicationRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        ProcessDefinition process = processRepository
                .findById(request.processId())
                .orElseThrow(() -> new ProcessNotFoundException(request.processId()));

        if (!process.isActive()) {
            throw new ProcessNotFoundException(request.processId());
        }

        Application application = new Application(user, process);
        Application savedApplication = applicationRepository.saveAndFlush(application);

        List<RequirementDefinition> requirements =
                requirementRepository
                        .findByProcessIdOrderByTitleAsc(process.getId());

        List<ApplicationChecklistItem> checklistItems =
                requirements.stream()
                        .map(requirement ->
                                new ApplicationChecklistItem(
                                        savedApplication,
                                        requirement))
                        .toList();

        checklistRepository.saveAll(checklistItems);

        return toResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsForUser(UUID userId) {
        return applicationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChecklistItemResponse> getChecklistForUser(
            UUID userId,
            UUID applicationId) {

        applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(
                                applicationId));

        return checklistRepository
                .findByApplicationIdOrderByRequirementTitleAsc(
                        applicationId)
                .stream()
                .map(this::toChecklistItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationForUser(
            UUID userId,
            UUID applicationId) {

        Application application = applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        return toResponse(application);
    }

    @Transactional
    public ApplicationResponse update(
            UUID userId,
            UUID applicationId,
            UpdateApplicationRequest request) {

        Application application = applicationRepository
                .findByIdAndUserId(applicationId, userId)
                .orElseThrow(
                        () -> new ApplicationNotFoundException(applicationId));

        application.changeStatus(request.status());
        application.updateCompleteness(request.completeness());

        Application savedApplication = applicationRepository.saveAndFlush(application);

        return toResponse(savedApplication);
    }

    private ApplicationResponse toResponse(Application application) {
        ProcessDefinition process = application.getProcess();

        return new ApplicationResponse(
                application.getId(),
                process.getId(),
                process.getCode(),
                process.getTitle(),
                application.getStatus(),
                application.getCompleteness(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    private ChecklistItemResponse toChecklistItemResponse(
            ApplicationChecklistItem item) {

        RequirementDefinition requirement =
                item.getRequirement();

        return new ChecklistItemResponse(
                item.getId(),
                requirement.getId(),
                requirement.getCode(),
                requirement.getTitle(),
                requirement.isRequired(),
                item.isCompleted());
    }
}