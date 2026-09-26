package com.amtpilot.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ApplicationAdvice;

public interface ApplicationAdviceRepository
        extends JpaRepository<ApplicationAdvice, UUID> {

    Optional<ApplicationAdvice> findByApplicationId(
            UUID applicationId);
}
