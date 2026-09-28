package com.amtpilot.application.exception;

import java.util.UUID;

public class ApplicationAdviceNotFoundException extends RuntimeException {

    public ApplicationAdviceNotFoundException(UUID applicationId) {
        super("AI application review not found for application: " + applicationId);
    }
}
