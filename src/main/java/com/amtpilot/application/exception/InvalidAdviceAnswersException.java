package com.amtpilot.application.exception;

public class InvalidAdviceAnswersException extends RuntimeException {

    public InvalidAdviceAnswersException() {
        super("Answers must correspond to questions from the current AI review");
    }
}
