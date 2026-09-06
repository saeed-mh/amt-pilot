package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ApplicationDocument;

public interface ApplicationDocumentRepository
        extends JpaRepository<ApplicationDocument, UUID> {

    List<ApplicationDocument> findByApplicationIdOrderByCreatedAtDesc(
            UUID applicationId);

    Optional<ApplicationDocument> findByIdAndApplicationUserId(
            UUID documentId,
            UUID userId);
}