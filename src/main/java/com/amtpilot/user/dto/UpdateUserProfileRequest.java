package com.amtpilot.user.dto;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(max = 10, message = "Preferred language must not exceed 10 characters") String preferredLanguage,

        @Size(max = 120, message = "City must not exceed 120 characters") String city,

        @Size(max = 120, message = "Country of origin must not exceed 120 characters") String countryOfOrigin,

        @Size(max = 40, message = "User type must not exceed 40 characters") String userType,

        @Size(max = 60, message = "Timezone must not exceed 60 characters") String timezone) {
}