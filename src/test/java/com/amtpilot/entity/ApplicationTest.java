package com.amtpilot.entity;

import org.junit.jupiter.api.Test;

import com.amtpilot.application.exception.InvalidApplicationStatusTransitionException;
import com.amtpilot.enums.ApplicationStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationTest {

    @Test
    void allowsValidStatusTransition() {
        Application application = createApplication();

        application.changeStatus(ApplicationStatus.ANALYZING);

        assertEquals(
                ApplicationStatus.ANALYZING,
                application.getStatus());
    }

    @Test
    void rejectsInvalidStatusTransition() {
        Application application = createApplication();

        InvalidApplicationStatusTransitionException exception =
                assertThrows(
                        InvalidApplicationStatusTransitionException.class,
                        () -> application.changeStatus(
                                ApplicationStatus.COMPLETED));

        assertEquals(
                "Cannot change application status from DRAFT to COMPLETED",
                exception.getMessage());

        assertEquals(
                ApplicationStatus.DRAFT,
                application.getStatus());
    }

    private Application createApplication() {
        User user = new User(
                "user@example.com",
                "password-hash");

        ProcessDefinition process = new ProcessDefinition(
                null,
                "ADDRESS_REGISTRATION",
                "Address Registration",
                "Dortmund",
                "REGISTRATION");

        return new Application(user, process);
    }
}