package com.amtpilot.application.exception;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException(UUID applicationId) {
        super("Application not found: " + applicationId);
    }
}