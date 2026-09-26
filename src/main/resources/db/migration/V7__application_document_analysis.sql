CREATE TABLE application_document_analysis (
    id UUID PRIMARY KEY,

    document_id UUID NOT NULL
        REFERENCES application_document(id) ON DELETE CASCADE,

    document_type VARCHAR(255) NOT NULL,
    primary_language VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,

    extracted_fields JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(extracted_fields) = 'array'),

    missing_or_unclear JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(missing_or_unclear) = 'array'),

    warnings JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(warnings) = 'array'),

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_document_analysis_document
        UNIQUE (document_id)
);
