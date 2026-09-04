package com.amtpilot.process.dto;

import java.util.UUID;

public record ProcessResponse(
        UUID id,
        String code,
        String title,
        String city,
        String domain,
        int version,
        UUID authorityId,
        String authorityName
) {
}