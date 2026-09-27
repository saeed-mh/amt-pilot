package com.amtpilot.process.exception;

import java.util.UUID;

public class ProcessGuideNotFoundException extends RuntimeException {

    public ProcessGuideNotFoundException(UUID processId) {
        super("Process guide not found for process: " + processId);
    }
}