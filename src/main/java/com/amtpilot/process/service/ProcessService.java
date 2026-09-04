package com.amtpilot.process.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amtpilot.entity.Authority;
import com.amtpilot.entity.ProcessDefinition;
import com.amtpilot.process.dto.ProcessResponse;
import com.amtpilot.repository.ProcessDefinitionRepository;

@Service
@Transactional(readOnly = true)
public class ProcessService {

    private final ProcessDefinitionRepository processDefinitionRepository;

    public ProcessService(
            ProcessDefinitionRepository processDefinitionRepository) {
        this.processDefinitionRepository = processDefinitionRepository;
    }

    public List<ProcessResponse> getProcessesByCity(String city) {
        return processDefinitionRepository
                .findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(city)
                .stream()
                .map(this::toResponse)
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
}