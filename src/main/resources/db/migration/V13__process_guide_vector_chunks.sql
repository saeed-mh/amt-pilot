CREATE TABLE process_guide_chunk (
    id UUID PRIMARY KEY,
    process_id UUID NOT NULL
        REFERENCES process_definition(id) ON DELETE CASCADE,
    section VARCHAR(40) NOT NULL,
    language VARCHAR(10) NOT NULL,
    chunk_index INTEGER NOT NULL CHECK (chunk_index >= 0),
    content TEXT NOT NULL CHECK (length(trim(content)) > 0),
    source_title VARCHAR(500) NOT NULL,
    source_url VARCHAR(2048) NOT NULL,
    verified_at DATE NOT NULL,
    embedding VECTOR(768),
    embedding_model VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT process_guide_chunk_section
        CHECK (section IN (
            'overview',
            'eligibility',
            'steps',
            'deadline',
            'fee',
            'appointment'
        )),
    CONSTRAINT process_guide_chunk_embedding_model
        CHECK (
            (embedding IS NULL AND embedding_model IS NULL)
            OR
            (embedding IS NOT NULL AND embedding_model IS NOT NULL)
        ),
    CONSTRAINT uq_process_guide_chunk
        UNIQUE (process_id, section, language, chunk_index)
);

CREATE INDEX idx_process_guide_chunk_lookup
    ON process_guide_chunk(process_id, language, section);

CREATE INDEX idx_process_guide_chunk_embedding
    ON process_guide_chunk
    USING hnsw (embedding vector_cosine_ops)
    WHERE embedding IS NOT NULL;

INSERT INTO process_guide_chunk (
    id,
    process_id,
    section,
    language,
    chunk_index,
    content,
    source_title,
    source_url,
    verified_at
)
SELECT
    gen_random_uuid(),
    guide.process_id,
    'overview',
    'en',
    0,
    guide.overview_en,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
WHERE process.code = 'ADDRESS_REGISTRATION'
UNION ALL
SELECT
    gen_random_uuid(),
    guide.process_id,
    'eligibility',
    'en',
    0,
    guide.eligibility_en,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
WHERE process.code = 'ADDRESS_REGISTRATION'
UNION ALL
SELECT
    gen_random_uuid(),
    guide.process_id,
    'deadline',
    'en',
    0,
    guide.deadline_en,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
WHERE process.code = 'ADDRESS_REGISTRATION'
UNION ALL
SELECT
    gen_random_uuid(),
    guide.process_id,
    'fee',
    'en',
    0,
    guide.fee_en,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
WHERE process.code = 'ADDRESS_REGISTRATION'
UNION ALL
SELECT
    gen_random_uuid(),
    guide.process_id,
    'appointment',
    'en',
    0,
    guide.appointment_information_en,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
WHERE process.code = 'ADDRESS_REGISTRATION';

INSERT INTO process_guide_chunk (
    id,
    process_id,
    section,
    language,
    chunk_index,
    content,
    source_title,
    source_url,
    verified_at
)
SELECT
    gen_random_uuid(),
    guide.process_id,
    'steps',
    'en',
    step.ordinality::INTEGER - 1,
    step.content,
    guide.source_title,
    guide.source_url,
    guide.verified_at
FROM process_guide guide
JOIN process_definition process ON process.id = guide.process_id
CROSS JOIN LATERAL jsonb_array_elements_text(guide.steps_en)
    WITH ORDINALITY AS step(content, ordinality)
WHERE process.code = 'ADDRESS_REGISTRATION';
