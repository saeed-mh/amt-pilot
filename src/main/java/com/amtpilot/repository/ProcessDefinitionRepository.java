package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ProcessDefinition;

public interface ProcessDefinitionRepository
        extends JpaRepository<ProcessDefinition, UUID> {

    Optional<ProcessDefinition> findByCode(String code);

    List<ProcessDefinition> findByCityIgnoreCaseAndActiveTrueOrderByTitleAsc(
            String city);
}