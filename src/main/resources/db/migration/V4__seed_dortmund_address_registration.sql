INSERT INTO authority (
    id,
    name,
    authority_type,
    city,
    official_url
)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Dortmund Citizens Office',
    'CITIZENS_OFFICE',
    'Dortmund',
    'https://www.dortmund.de'
)
ON CONFLICT DO NOTHING;

INSERT INTO process_definition (
    id,
    authority_id,
    code,
    title,
    city,
    domain,
    version,
    active
)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'ADDRESS_REGISTRATION',
    'Address Registration',
    'Dortmund',
    'REGISTRATION',
    1,
    TRUE
)
ON CONFLICT DO NOTHING;

INSERT INTO official_source (
    id,
    authority_id,
    url,
    title,
    city,
    language
)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'https://www.dortmund.de/services/wohnsitzanmeldung.html',
    'Wohnsitz in Dortmund anmelden',
    'Dortmund',
    'de'
)
ON CONFLICT DO NOTHING;

INSERT INTO requirement_definition (
    id,
    process_id,
    source_id,
    code,
    title,
    required,
    version
)
SELECT
    '44444444-4444-4444-4444-444444444441',
    process.id,
    source.id,
    'IDENTITY_DOCUMENTS',
    'All identity and passport documents',
    TRUE,
    1
FROM process_definition process
CROSS JOIN official_source source
WHERE process.code = 'ADDRESS_REGISTRATION'
  AND source.url = 'https://www.dortmund.de/services/wohnsitzanmeldung.html'
ON CONFLICT DO NOTHING;

INSERT INTO requirement_definition (
    id,
    process_id,
    source_id,
    code,
    title,
    required,
    version
)
SELECT
    '44444444-4444-4444-4444-444444444442',
    process.id,
    source.id,
    'LANDLORD_CONFIRMATION',
    'Landlord confirmation',
    TRUE,
    1
FROM process_definition process
CROSS JOIN official_source source
WHERE process.code = 'ADDRESS_REGISTRATION'
  AND source.url = 'https://www.dortmund.de/services/wohnsitzanmeldung.html'
ON CONFLICT DO NOTHING;

INSERT INTO requirement_definition (
    id,
    process_id,
    source_id,
    code,
    title,
    required,
    version
)
SELECT
    '44444444-4444-4444-4444-444444444443',
    process.id,
    source.id,
    'CIVIL_STATUS_DOCUMENTS',
    'Marriage or birth certificates (if applicable)',
    FALSE,
    1
FROM process_definition process
CROSS JOIN official_source source
WHERE process.code = 'ADDRESS_REGISTRATION'
  AND source.url = 'https://www.dortmund.de/services/wohnsitzanmeldung.html'
ON CONFLICT DO NOTHING;

INSERT INTO application_checklist_item (
    id,
    application_id,
    requirement_id
)
SELECT
    md5(application.id::text || ':' || requirement.id::text)::uuid,
    application.id,
    requirement.id
FROM application
JOIN process_definition process
    ON process.id = application.process_id
JOIN requirement_definition requirement
    ON requirement.process_id = process.id
WHERE process.code = 'ADDRESS_REGISTRATION'
ON CONFLICT DO NOTHING;
