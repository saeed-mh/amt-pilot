package com.amtpilot.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ProcessGuide;

public interface ProcessGuideRepository
        extends JpaRepository<ProcessGuide, UUID> {

    Optional<ProcessGuide> findByProcessId(UUID processId);

    List<ProcessGuide> findByProcessIdIn(Collection<UUID> processIds);
}