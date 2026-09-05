package com.amtpilot.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(

        @NotNull(message = "Process ID is required") UUID processId) {
}