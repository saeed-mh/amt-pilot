package com.amtpilot.application.dto;

import java.util.UUID;

public record ChecklistItemResponse(
        UUID id,
        UUID requirementId,
        String requirementCode,
        String title,
        boolean required,
        boolean completed) {
}