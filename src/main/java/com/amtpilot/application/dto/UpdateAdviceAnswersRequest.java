package com.amtpilot.application.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAdviceAnswersRequest(
        @NotNull
        @NotEmpty
        @Size(max = 10)
        Map<
                @NotBlank @Size(max = 1000) String,
                @NotBlank @Size(max = 2000) String> answers) {
}
