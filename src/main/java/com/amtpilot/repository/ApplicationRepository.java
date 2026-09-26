package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.Application;
import com.amtpilot.enums.ApplicationStatus;

public interface ApplicationRepository
        extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = "process")
    Optional<Application> findByIdAndUserId(
            UUID applicationId,
            UUID userId);

    boolean existsByUserIdAndProcessIdAndStatusNot(
            UUID userId,
            UUID processId,
            ApplicationStatus status);
}
