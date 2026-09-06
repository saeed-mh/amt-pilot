package com.amtpilot.enums;

public enum ApplicationStatus {

    DRAFT,
    ANALYZING,
    ACTION_REQUIRED,
    READY_TO_SUBMIT,
    SUBMITTED,
    COMPLETED,
    NEEDS_REVIEW;

    public boolean canTransitionTo(ApplicationStatus newStatus) {
        if (newStatus == null) {
            return false;
        }

        if (this == newStatus) {
            return true;
        }

        return switch (this) {
            case DRAFT ->
                newStatus == ANALYZING;

            case ANALYZING ->
                newStatus == ACTION_REQUIRED
                        || newStatus == READY_TO_SUBMIT
                        || newStatus == NEEDS_REVIEW;

            case ACTION_REQUIRED ->
                newStatus == READY_TO_SUBMIT
                        || newStatus == NEEDS_REVIEW;

            case READY_TO_SUBMIT ->
                newStatus == SUBMITTED
                        || newStatus == ACTION_REQUIRED;

            case SUBMITTED ->
                newStatus == COMPLETED
                        || newStatus == ACTION_REQUIRED;

            case NEEDS_REVIEW ->
                newStatus == ANALYZING
                        || newStatus == ACTION_REQUIRED;

            case COMPLETED -> false;
        };
    }
}
