package com.amtpilot.application.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(

        UUID id,

        UUID applicationId,

        UUID checklistItemId,

        String originalFilename,

        String contentType,

        long sizeBytes,

        Instant createdAt) {
}