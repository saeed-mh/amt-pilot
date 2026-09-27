ALTER TABLE application_advice
ADD COLUMN official_source_references JSONB NOT NULL DEFAULT '[]'::jsonb
    CHECK (jsonb_typeof(official_source_references) = 'array');
