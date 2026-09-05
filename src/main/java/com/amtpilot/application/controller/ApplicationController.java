package com.amtpilot.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amtpilot.application.dto.ApplicationResponse;
import com.amtpilot.application.dto.CreateApplicationRequest;
import com.amtpilot.application.service.ApplicationService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateApplicationRequest request,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        ApplicationResponse response = applicationService.create(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, traceId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplications(
            @AuthenticationPrincipal Jwt jwt,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<ApplicationResponse> applications = applicationService.getApplicationsForUser(userId);

        return ResponseEntity.ok(
                ApiResponse.success(applications, traceId));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplication(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID applicationId,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        ApplicationResponse application = applicationService.getApplicationForUser(userId, applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(application, traceId));
    }
}