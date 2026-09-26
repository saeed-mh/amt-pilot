package com.amtpilot.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ApplicationDocumentAnalysis;

public interface ApplicationDocumentAnalysisRepository
        extends JpaRepository<ApplicationDocumentAnalysis, UUID> {

    Optional<ApplicationDocumentAnalysis> findByDocumentId(UUID documentId);
}