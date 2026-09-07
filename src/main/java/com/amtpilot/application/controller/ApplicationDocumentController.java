package com.amtpilot.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.amtpilot.application.dto.DocumentResponse;
import com.amtpilot.application.service.ApplicationDocumentService;
import com.amtpilot.common.web.ApiResponse;
import com.amtpilot.common.web.TraceIdFilter;

@RestController
@RequestMapping("/api/v1/applications/{applicationId}/documents")
public class ApplicationDocumentController {

    private final ApplicationDocumentService documentService;

    public ApplicationDocumentController(
            ApplicationDocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID applicationId,
            @RequestParam(required = false) UUID checklistItemId,
            @RequestPart("file") MultipartFile file,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE)
            String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        DocumentResponse document = documentService.upload(
                userId,
                applicationId,
                checklistItemId,
                file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(document, traceId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID applicationId,
            @RequestAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE)
            String traceId) {

        UUID userId = UUID.fromString(jwt.getSubject());

        List<DocumentResponse> documents = documentService.list(
                userId,
                applicationId);

        return ResponseEntity.ok(
                ApiResponse.success(documents, traceId));
    }
}