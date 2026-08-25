CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    city VARCHAR(120),
    country_of_origin VARCHAR(120),
    user_type VARCHAR(40),
    timezone VARCHAR(60) NOT NULL DEFAULT 'Europe/Berlin',
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE authority (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    authority_type VARCHAR(60) NOT NULL,
    city VARCHAR(120) NOT NULL,
    official_url VARCHAR(2048) NOT NULL,
    contact_url VARCHAR(2048),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE official_source (
    id UUID PRIMARY KEY,
    authority_id UUID REFERENCES authority(id),
    url VARCHAR(2048) NOT NULL UNIQUE,
    title VARCHAR(500) NOT NULL,
    city VARCHAR(120) NOT NULL,
    language VARCHAR(10) NOT NULL,
    fetched_at TIMESTAMP WITH TIME ZONE,
    checksum VARCHAR(64),
    status VARCHAR(30) NOT NULL DEFAULT 'NEEDS_REVIEW',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE process_definition (
    id UUID PRIMARY KEY,
    authority_id UUID REFERENCES authority(id),
    code VARCHAR(120) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    city VARCHAR(120) NOT NULL,
    domain VARCHAR(80) NOT NULL,
    version INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE requirement_definition (
    id UUID PRIMARY KEY,
    process_id UUID NOT NULL REFERENCES process_definition(id) ON DELETE CASCADE,
    source_id UUID NOT NULL REFERENCES official_source(id),
    code VARCHAR(120) NOT NULL,
    title VARCHAR(255) NOT NULL,
    required BOOLEAN NOT NULL,
    version INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_requirement_process_code UNIQUE (process_id, code)
);

CREATE TABLE application (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    process_id UUID NOT NULL REFERENCES process_definition(id),
    status VARCHAR(40) NOT NULL,
    completeness SMALLINT NOT NULL DEFAULT 0 CHECK (completeness BETWEEN 0 AND 100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    actor_id UUID REFERENCES app_user(id) ON DELETE SET NULL,
    action VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120) NOT NULL,
    entity_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_process_definition_city_active ON process_definition(city, active);
CREATE INDEX idx_application_user_status ON application(user_id, status);
CREATE INDEX idx_audit_event_entity ON audit_event(entity_type, entity_id);
