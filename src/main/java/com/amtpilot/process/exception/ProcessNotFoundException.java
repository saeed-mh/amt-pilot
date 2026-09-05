package com.amtpilot.process.exception;

import java.util.UUID;

public class ProcessNotFoundException extends RuntimeException {

    public ProcessNotFoundException(UUID processId) {
        super("Process not found: " + processId);
    }
}