package com.amtpilot.ai.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

public record AiOfficialSourceReferenceResponse(
        String section,
        List<String> statements,
        @JsonAlias("source_title") String sourceTitle,
        @JsonAlias("source_url") String sourceUrl,
        @JsonAlias("verified_at") LocalDate verifiedAt) {
}
