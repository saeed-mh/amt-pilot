package com.amtpilot.application.dto;

import org.springframework.core.io.Resource;

public record DocumentDownload(
        Resource resource,
        String originalFilename,
        String contentType,
        long sizeBytes) {
}
