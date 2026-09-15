package com.amtpilot.application.exception;

public class ActiveApplicationAlreadyExistsException extends RuntimeException {

    public ActiveApplicationAlreadyExistsException(String processTitle) {
        super("You already have an active application for " + processTitle + ".");
    }
}
