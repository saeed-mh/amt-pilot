package com.amtpilot.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.entity.Application;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.User;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ApplicationRepository;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.UserRepository;
import com.amtpilot.user.exception.UserNotFoundException;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final ProcessDefinitionRepository processRepository;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            ProcessDefinitionRepository processRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
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
        Application savedApplication = applicationRepository.save(application);

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
}