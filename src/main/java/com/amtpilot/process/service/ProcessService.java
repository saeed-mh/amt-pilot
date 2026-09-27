package com.amtpilot.process.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional(readOnly = true)
public class ProcessService {

    private final ProcessDefinitionRepository processDefinitionRepository;
    private final RequirementDefinitionRepository requirementRepository;
    private final ProcessGuideRepository processGuideRepository;

    public ProcessService(
            ProcessDefinitionRepository processDefinitionRepository,
            RequirementDefinitionRepository requirementRepository,
            ProcessGuideRepository processGuideRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
        this.requirementRepository = requirementRepository;
        this.processGuideRepository = processGuideRepository;
    }

    public List<ProcessResponse> getProcessesByCity(String city) {
        List<ProcessDefinition> processes = processDefinitionRepository
                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(city);

        if (processes.isEmpty()) {
            return List.of();
        }

        List<UUID> processIds = processes.stream()
                .map(ProcessDefinition::getId)
                .toList();

        Set<UUID> processIdsWithGuides = new HashSet<>(
                processGuideRepository.findByProcessIdIn(processIds)
                        .stream()
                        .map(guide -> guide.getProcess().getId())
                        .toList());

        return processes.stream()
                .map(process -> toResponse(
                        process,
                        processIdsWithGuides.contains(process.getId())))
                .toList();
    }

    public List<RequirementResponse> getRequirements(UUID processId) {
        findActiveProcess(processId);

        return requirementRepository
                .findByProcessIdOrderByTitleAsc(processId)
                .stream()
                .map(this::toRequirementResponse)
                .toList();
    }

    public ProcessGuideResponse getGuide(UUID processId, String language) {
        findActiveProcess(processId);

        ProcessGuide guide = processGuideRepository.findByProcessId(processId)
                .orElseThrow(() -> new ProcessGuideNotFoundException(processId));

        boolean german = "de".equalsIgnoreCase(language);

        return new ProcessGuideResponse(
                processId,
                german ? guide.getOverviewDe() : guide.getOverviewEn(),
                german ? guide.getEligibilityDe() : guide.getEligibilityEn(),
                List.copyOf(german ? guide.getStepsDe() : guide.getStepsEn()),
                german ? guide.getDeadlineDe() : guide.getDeadlineEn(),
                german ? guide.getFeeDe() : guide.getFeeEn(),
                guide.isAppointmentRequired(),
                german
                        ? guide.getAppointmentInformationDe()
                        : guide.getAppointmentInformationEn(),
                guide.getAppointmentUrl(),
                guide.getSourceTitle(),
                guide.getSourceUrl(),
                guide.getVerifiedAt());
    }

    private ProcessDefinition findActiveProcess(UUID processId) {
        return processDefinitionRepository.findById(processId)
                .filter(ProcessDefinition::isActive)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
    }

    private ProcessResponse toResponse(
            ProcessDefinition process,
            boolean guideAvailable) {

        Authority authority = process.getAuthority();

        return new ProcessResponse(
                process.getId(),
                process.getCode(),
                process.getTitle(),
                process.getCity(),
                process.getDomain(),
                process.getVersion(),
                authority != null ? authority.getId() : null,
                authority != null ? authority.getName() : null,
                guideAvailable);
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