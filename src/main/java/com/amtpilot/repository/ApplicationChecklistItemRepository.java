package com.amtpilot.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.amtpilot.entity.ApplicationChecklistItem;

public interface ApplicationChecklistItemRepository
        extends JpaRepository<ApplicationChecklistItem, UUID> {

    List<ApplicationChecklistItem>
            findByApplicationIdOrderByRequirementTitleAsc(
                    UUID applicationId);

    Optional<ApplicationChecklistItem>
            findByIdAndApplicationUserId(
                    UUID checklistItemId,
                    UUID userId);
}