CREATE TABLE application_document (
    id UUID PRIMARY KEY,

    application_id UUID NOT NULL
        REFERENCES application(id) ON DELETE CASCADE,

    checklist_item_id UUID
        REFERENCES application_checklist_item(id) ON DELETE SET NULL,

    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes > 0),
    storage_path VARCHAR(1000) NOT NULL UNIQUE,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_application_document_application
    ON application_document(application_id);