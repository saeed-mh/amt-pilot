package com.amtpilot.application.dto;

import com.amtpilot.enums.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateApplicationRequest(

        @NotNull(message = "Status is required")
        ApplicationStatus status) {
}