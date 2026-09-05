package com.amtpilot.process.dto;

import java.util.UUID;

public record RequirementResponse(
        UUID id,
        String code,
        String title,
        boolean required,
        int version,
        UUID sourceId,
        String sourceTitle,
        String sourceUrl) {
}