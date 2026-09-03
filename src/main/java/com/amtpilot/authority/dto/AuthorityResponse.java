package com.amtpilot.authority.dto;

import java.util.UUID;

public record AuthorityResponse(
        UUID id,
        String name,
        String authorityType,
        String city,
        String officialUrl,
        String contactUrl
) {
}