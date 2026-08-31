package com.amtpilot.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {

}
