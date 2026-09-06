package com.amtpilot.application.exception;

import java.util.UUID;

public class ChecklistItemNotFoundException
        extends RuntimeException {

    public ChecklistItemNotFoundException(UUID checklistItemId) {
        super("Checklist item not found: " + checklistItemId);
    }
}