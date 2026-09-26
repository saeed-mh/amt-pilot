CREATE TABLE application_advice (
    id UUID PRIMARY KEY,

    application_id UUID NOT NULL
        REFERENCES application(id) ON DELETE CASCADE,

    readiness VARCHAR(40) NOT NULL,
    summary TEXT NOT NULL,

    requirement_assessments JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(requirement_assessments) = 'array'),

    inconsistencies JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(inconsistencies) = 'array'),

    next_steps JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(next_steps) = 'array'),

    questions_for_user JSONB NOT NULL DEFAULT '[]'::jsonb
        CHECK (jsonb_typeof(questions_for_user) = 'array'),

    disclaimer TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMP WITH TIME ZONE
        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_application_advice_application
        UNIQUE (application_id)
);
