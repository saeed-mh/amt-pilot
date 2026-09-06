CREATE TABLE application_checklist_item (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL
        REFERENCES application(id) ON DELETE CASCADE,
    requirement_id UUID NOT NULL
        REFERENCES requirement_definition(id),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_application_requirement
        UNIQUE (application_id, requirement_id)
);

CREATE INDEX idx_checklist_item_application
    ON application_checklist_item(application_id);