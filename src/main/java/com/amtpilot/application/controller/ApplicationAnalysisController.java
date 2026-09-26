package com.amtpilot.application.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amtpilot.application.dto.ApplicationAdviceResponse;
import com.amtpilot.application.dto.DocumentAnalysisResponse;
import com.amtpilot.application.service.ApplicationAdviceService;
import com.amtpilot.application.service.ApplicationDocumentAnalysisService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationAnalysisController {

    private final ApplicationDocumentAnalysisService analysisService;
    private final ApplicationAdviceService adviceService;

    public ApplicationAnalysisController(
            ApplicationDocumentAnalysisService analysisService,
            ApplicationAdviceService adviceService) {

        this.analysisService = analysisService;
        this.adviceService = adviceService;
    }

    @GetMapping("/{applicationId}/analyses")
    public ResponseEntity<ApiResponse<List<DocumentAnalysisResponse>>> getAnalyses(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID applicationId,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<DocumentAnalysisResponse> analyses = analysisService.listForApplication(
                userId,
                applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(analyses, traceId));
    }

    @GetMapping("/{applicationId}/advice")
    public ResponseEntity<ApiResponse<ApplicationAdviceResponse>> getAdvice(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID applicationId,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE) String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        Optional<ApplicationAdviceResponse> advice = adviceService
                .getForApplication(
                        userId,
                        applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        advice.orElse(null),
                        traceId));
    }
}
