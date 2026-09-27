package com.amtpilot.process.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProcessGuideResponse(
        UUID processId,
        String overview,
        String eligibility,
        List<String> steps,
        String deadline,
        String fee,
        boolean appointmentRequired,
        String appointmentInformation,
        String appointmentUrl,
        String sourceTitle,
        String sourceUrl,
        LocalDate verifiedAt
) {
}