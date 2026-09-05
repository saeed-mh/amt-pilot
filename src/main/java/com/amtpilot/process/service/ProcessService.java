package com.amtpilot.process.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.entity.Authority;
import com.amtpilot.entity.OfficialSource;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.entity.RequirementDefinition;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.process.dto.RequirementResponse;
import com.amtpilot.process.exception.ProcessNotFoundException;
import com.amtpilot.repository.ProcessDefinitionRepository;
import com.amtpilot.repository.RequirementDefinitionRepository;

@Service
@Transactional(readOnly = true)
public class ProcessService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final RequirementDefinitionRepository requirementRepository;

    public ProcessService(
            ProcessDefinitionRepository processDefinitionRepository,
            RequirementDefinitionRepository requirementRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
        this.requirementRepository = requirementRepository;
    }

    public List<ProcessResponse> getProcessesByCity(String city) {
        return processDefinitionRepository
                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(city)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RequirementResponse> getRequirements(UUID processId) {
        processDefinitionRepository.findById(processId)
                .filter(ProcessDefinition::isActive)
                .orElseThrow(() -> new ProcessNotFoundException(processId));

        return requirementRepository
                .findByProcessIdOrderByTitleAsc(processId)
                .stream()
                .map(this::toRequirementResponse)
                .toList();
    }

    private ProcessResponse toResponse(ProcessDefinition process) {
        Authority authority = process.getAuthority();

        return new ProcessResponse(
                process.getId(),
                process.getCode(),
                process.getTitle(),
                process.getCity(),
                process.getDomain(),
                process.getVersion(),
                authority != null ? authority.getId() : null,
                authority != null ? authority.getName() : null);
    }

    private RequirementResponse toRequirementResponse(
            RequirementDefinition requirement) {

        OfficialSource source = requirement.getSource();

        return new RequirementResponse(
                requirement.getId(),
                requirement.getCode(),
                requirement.getTitle(),
                requirement.isRequired(),
                requirement.getVersion(),
                source.getId(),
                source.getTitle(),
                source.getUrl());
    }
}