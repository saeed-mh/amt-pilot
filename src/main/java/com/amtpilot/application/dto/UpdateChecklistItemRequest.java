package com.amtpilot.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateChecklistItemRequest(

        @NotNull(message = "Completed must be provided")
        Boolean completed) {
}