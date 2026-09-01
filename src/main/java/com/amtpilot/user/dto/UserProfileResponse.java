package com.amtpilot.user.dto;

import java.time.Instant;
import java.util.UUID;

import com.amtpilot.enums.Role;

public record UserProfileResponse(
        UUID id,
        String email,
        String preferredLanguage,
        String city,
        String countryOfOrigin,
        String userType,
        String timezone,
        Role role,
        Instant createdAt
) {
}