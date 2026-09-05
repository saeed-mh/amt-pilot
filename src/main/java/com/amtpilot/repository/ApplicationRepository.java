package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.Application;

public interface ApplicationRepository
        extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Application> findByIdAndUserId(
            UUID applicationId,
            UUID userId);
}