package com.amtpilot.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.amtpilot.enums.ApplicationStatus;

public record ApplicationResponse(
        UUID id,
        UUID processId,
        String processCode,
        String processTitle,
        ApplicationStatus status,
        int completeness,
        Instant createdAt,
        Instant updatedAt) {
}