package com.amtpilot.common.web;

public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    String traceId
) {
    public static <T> ApiResponse<T> success(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId);
    }

    public static <T> ApiResponse<T> failure(ApiError error, String traceId) {
        return new ApiResponse<>(false, null, error, traceId);
    }
}
