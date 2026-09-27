package com.amtpilot.ai.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiOfficialProcessGuideRequest(
        String overview,
        String eligibility,
        List<String> steps,
        String deadline,
        String fee,
        @JsonProperty("appointment_required") boolean appointmentRequired,
        @JsonProperty("appointment_information") String appointmentInformation,
        @JsonProperty("appointment_url") String appointmentUrl,
        @JsonProperty("source_title") String sourceTitle,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("verified_at") LocalDate verifiedAt) {
}
