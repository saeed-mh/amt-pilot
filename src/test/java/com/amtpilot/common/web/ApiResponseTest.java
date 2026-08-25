package com.amtpilot.common.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

class ApiResponseTest {

    @Test
    void successCreatesSuccessfulResponse() {
        String data = "Hello";
        String traceId = "trace-123";

        ApiResponse<String> response =
                ApiResponse.success(data, traceId);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("Hello");
        assertThat(response.error()).isNull();
        assertThat(response.traceId()).isEqualTo("trace-123");
    }

    @Test
    void failureCreatesFailedResponse() {
        String traceId = "trace-456";

        ApiError error = new ApiError(
            "VALIDATION_FAILED",
            "Request validation failed",
            Map.of("email", "must be a valid email")
        );

        ApiResponse<Void> response = ApiResponse.failure(error, traceId);

        assertThat(response.success()).isFalse();
        assertThat(response.data()).isNull();
        assertThat(response.error()).isEqualTo(error);
        assertThat(response.traceId()).isEqualTo("trace-456");
    }
}