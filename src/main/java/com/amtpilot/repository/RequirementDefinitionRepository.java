package com.amtpilot.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.RequirementDefinition;

public interface RequirementDefinitionRepository
        extends JpaRepository<RequirementDefinition, UUID> {

    List<RequirementDefinition> findByProcessIdOrderByTitleAsc(
            UUID processId);
}