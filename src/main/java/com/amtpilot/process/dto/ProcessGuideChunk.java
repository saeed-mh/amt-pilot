package com.amtpilot.process.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ProcessGuideChunk(
        UUID id,
        String section,
        String content,
        String sourceTitle,
        String sourceUrl,
        LocalDate verifiedAt) {
}
