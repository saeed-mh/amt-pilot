package com.amtpilot.application.dto;

import com.amtpilot.enums.ApplicationStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateApplicationRequest(

        @NotNull(message = "Status is required") ApplicationStatus status,

        @NotNull(message = "Completeness is required") @Min(value = 0, message = "Completeness must be at least 0") @Max(value = 100, message = "Completeness must not exceed 100") Integer completeness) {
}