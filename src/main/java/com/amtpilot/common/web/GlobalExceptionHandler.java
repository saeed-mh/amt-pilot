package com.amtpilot.common.web;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.amtpilot.auth.exception.EmailAlreadyExistsException;
import com.amtpilot.auth.exception.InvalidCredentialsException;
import com.amtpilot.user.exception.UserNotFoundException;
import com.amtpilot.process.exception.ProcessNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        ResponseEntity<ApiResponse<Void>> handleValidation(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                Map<String, String> fieldErrors = new LinkedHashMap<>();
                for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
                        String message = fieldError.getDefaultMessage() == null
                                        ? "Invalid value"
                                        : fieldError.getDefaultMessage();
                        fieldErrors.putIfAbsent(fieldError.getField(), message);
                }

                ApiError error = new ApiError(
                                "VALIDATION_FAILED",
                                "Request validation failed",
                                fieldErrors);

                return ResponseEntity.badRequest()
                                .body(ApiResponse.failure(error, traceId(request)));
        }

        @ExceptionHandler(EmailAlreadyExistsException.class)
        ResponseEntity<ApiResponse<Void>> handleEmailAlreadyExists(
                        EmailAlreadyExistsException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "EMAIL_ALREADY_EXISTS",
                                exception.getMessage(),
                                Map.of());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.failure(error, traceId(request)));

        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        ResponseEntity<ApiResponse<Void>> handleMalformedJson(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "MALFORMED_JSON",
                                "Request body contains invalid JSON",
                                Map.of());

                return ResponseEntity.badRequest()
                                .body(ApiResponse.failure(error, traceId(request)));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
                        InvalidCredentialsException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "INVALID_CREDENTIALS",
                                exception.getMessage(),
                                Map.of());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.failure(error, traceId(request)));
        }

        @ExceptionHandler(UserNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleUserNotFound(
                        UserNotFoundException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "USER_NOT_FOUND",
                                exception.getMessage(),
                                Map.of());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.failure(error, traceId(request)));
        }

        @ExceptionHandler(ProcessNotFoundException.class)
        ResponseEntity<ApiResponse<Void>> handleProcessNotFound(
                        ProcessNotFoundException exception,
                        HttpServletRequest request) {

                ApiError error = new ApiError(
                                "PROCESS_NOT_FOUND",
                                exception.getMessage(),
                                Map.of());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.failure(error, traceId(request)));
        }

        @ExceptionHandler(Exception.class)
        ResponseEntity<ApiResponse<Void>> handleUnexpected(
                        Exception exception,
                        HttpServletRequest request) {

                String traceId = traceId(request);
                log.error("Unexpected request failure (traceId={})", traceId, exception);

                ApiError error = new ApiError(
                                "INTERNAL_ERROR",
                                "An unexpected error occurred",
                                Map.of());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.failure(error, traceId));
        }

        private String traceId(HttpServletRequest request) {
                Object value = request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
                return value instanceof String id ? id : "unknown";
        }
}
