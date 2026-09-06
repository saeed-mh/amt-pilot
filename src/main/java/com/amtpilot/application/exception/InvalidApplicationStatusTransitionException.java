package com.amtpilot.application.exception;

import com.amtpilot.enums.ApplicationStatus;

public class InvalidApplicationStatusTransitionException
        extends RuntimeException {

    public InvalidApplicationStatusTransitionException(
            ApplicationStatus currentStatus,
            ApplicationStatus newStatus) {

        super(
                "Cannot change application status from "
                        + currentStatus
                        + " to "
                        + newStatus);
    }
}