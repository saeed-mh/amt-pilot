package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.amtpilot.entity.ApplicationDocument;

public interface ApplicationDocumentRepository
        extends JpaRepository<ApplicationDocument, UUID> {

    List<ApplicationDocument> findByApplicationIdOrderByCreatedAtDesc(
            UUID applicationId);

    @Query("""
            SELECT document.storagePath
            FROM ApplicationDocument document
            WHERE document.application.id = :applicationId
            """)
    List<String> findStoragePathsByApplicationId(
            @Param("applicationId") UUID applicationId);

    Optional<ApplicationDocument> findByIdAndApplicationUserId(
            UUID documentId,
            UUID userId);

    boolean existsByChecklistItemId(UUID checklistItemId);
}
