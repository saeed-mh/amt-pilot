package com.amtpilot.application.exception;

public class DocumentStorageException extends RuntimeException {

    public DocumentStorageException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}