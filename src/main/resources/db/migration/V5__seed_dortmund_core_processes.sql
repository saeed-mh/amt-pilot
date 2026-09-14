-- Curated from official City of Dortmund service pages and checked on 2026-09-14.

UPDATE authority
SET official_url = 'https://www.dortmund.de/rathaus/verwaltung/buergerdienste/',
    contact_url = 'https://www.dortmund.de/rathaus/verwaltung/buergerdienste/kontakt/',
    updated_at = CURRENT_TIMESTAMP
WHERE id = '11111111-1111-1111-1111-111111111111';

INSERT INTO authority (
    id,
    name,
    authority_type,
    city,
    official_url,
    contact_url
)
VALUES
    (
        '55555555-5555-5555-5555-555555555551',
        'Dortmund Registry Office',
        'REGISTRY_OFFICE',
        'Dortmund',
        'https://www.dortmund.de/rathaus/verwaltung/buergerdienste/standesamt/',
        'https://www.dortmund.de/rathaus/verwaltung/buergerdienste/standesamt/'
    ),
    (
        '55555555-5555-5555-5555-555555555552',
        'Dortmund Office for Migration',
        'MIGRATION_OFFICE',
        'Dortmund',
        'https://www.dortmund.de/rathaus-und-verwaltung/verwaltung/amt-fuer-migration/',
        'https://www.dortmund.de/rathaus-und-verwaltung/verwaltung/amt-fuer-migration/kontakt/'
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
VALUES
    (
        '55555555-5555-5555-5555-555555555601',
        '11111111-1111-1111-1111-111111111111',
        'IDENTITY_CARD_APPLICATION',
        'Identity Card Application',
        'Dortmund',
        'IDENTITY_DOCUMENTS',
        1,
        TRUE
    ),
    (
        '55555555-5555-5555-5555-555555555602',
        '11111111-1111-1111-1111-111111111111',
        'PASSPORT_APPLICATION',
        'Passport Application',
        'Dortmund',
        'IDENTITY_DOCUMENTS',
        1,
        TRUE
    ),
    (
        '55555555-5555-5555-5555-555555555603',
        '11111111-1111-1111-1111-111111111111',
        'CERTIFICATE_OF_CONDUCT',
        'Certificate of Conduct',
        'Dortmund',
        'BACKGROUND_CHECK',
        1,
        TRUE
    ),
    (
        '55555555-5555-5555-5555-555555555604',
        '55555555-5555-5555-5555-555555555551',
        'MARRIAGE_REGISTRATION',
        'Marriage Registration',
        'Dortmund',
        'FAMILY',
        1,
        TRUE
    ),
    (
        '55555555-5555-5555-5555-555555555605',
        '55555555-5555-5555-5555-555555555551',
        'BIRTH_CERTIFICATE_REQUEST',
        'Birth Certificate Request',
        'Dortmund',
        'CERTIFICATES',
        1,
        TRUE
    ),
    (
        '55555555-5555-5555-5555-555555555606',
        '55555555-5555-5555-5555-555555555552',
        'NATURALIZATION',
        'Naturalization',
        'Dortmund',
        'CITIZENSHIP',
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
VALUES
    (
        '66666666-6666-6666-6666-666666666601',
        '11111111-1111-1111-1111-111111111111',
        'https://www.dortmund.de/services/personalausweis.html',
        'Personalausweis beantragen',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666602',
        '11111111-1111-1111-1111-111111111111',
        'https://www.dortmund.de/services/reisepass.html',
        'Reisepass beantragen',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666603',
        '11111111-1111-1111-1111-111111111111',
        'https://www.dortmund.de/services/fuehrungszeugnis-beantragen.html',
        'Fuehrungszeugnis beantragen',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666604',
        '55555555-5555-5555-5555-555555555551',
        'https://www.dortmund.de/services/eheschliessung-anmeldung.html',
        'Eheschliessung anmelden',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666605',
        '55555555-5555-5555-5555-555555555551',
        'https://www.dortmund.de/services/personenstandsurkunde-anfordern-nutzung-fuer-ahnen-oder-familienforschung-moeglich.html',
        'Personenstandsurkunde anfordern',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666606',
        '55555555-5555-5555-5555-555555555552',
        'https://www.dortmund.de/services/einbuergerung-38-4-2.html',
        'Einbuergerung',
        'Dortmund',
        'de'
    )
ON CONFLICT DO NOTHING;

-- Requirements are linked to both the process and the official source.

WITH requirement_seed (id, process_code, source_url, code, title, required) AS (
    VALUES
        ('77777777-7777-7777-7777-777777777701'::uuid, 'IDENTITY_CARD_APPLICATION', 'https://www.dortmund.de/services/personalausweis.html', 'CURRENT_ID_DOCUMENT', 'Current identity card or passport', TRUE),
        ('77777777-7777-7777-7777-777777777702'::uuid, 'IDENTITY_CARD_APPLICATION', 'https://www.dortmund.de/services/personalausweis.html', 'DIGITAL_BIOMETRIC_PHOTO', 'Current digital biometric photo', TRUE),
        ('77777777-7777-7777-7777-777777777703'::uuid, 'IDENTITY_CARD_APPLICATION', 'https://www.dortmund.de/services/personalausweis.html', 'PARENTAL_CONSENT', 'Parental consent for applicants under 16', FALSE),
        ('77777777-7777-7777-7777-777777777704'::uuid, 'IDENTITY_CARD_APPLICATION', 'https://www.dortmund.de/services/personalausweis.html', 'CIVIL_STATUS_DOCUMENT', 'Civil-status document if personal data needs clarification', FALSE),
        ('77777777-7777-7777-7777-777777777711'::uuid, 'PASSPORT_APPLICATION', 'https://www.dortmund.de/services/reisepass.html', 'CURRENT_ID_DOCUMENT', 'Current identity card or passport', TRUE),
        ('77777777-7777-7777-7777-777777777712'::uuid, 'PASSPORT_APPLICATION', 'https://www.dortmund.de/services/reisepass.html', 'DIGITAL_BIOMETRIC_PHOTO', 'Current digital biometric photo', TRUE),
        ('77777777-7777-7777-7777-777777777713'::uuid, 'PASSPORT_APPLICATION', 'https://www.dortmund.de/services/reisepass.html', 'PARENTAL_CONSENT', 'Parental consent and ID copies for a minor', FALSE),
        ('77777777-7777-7777-7777-777777777714'::uuid, 'PASSPORT_APPLICATION', 'https://www.dortmund.de/services/reisepass.html', 'URGENCY_EVIDENCE', 'Evidence of urgency for a temporary passport', FALSE),
        ('77777777-7777-7777-7777-777777777721'::uuid, 'CERTIFICATE_OF_CONDUCT', 'https://www.dortmund.de/services/fuehrungszeugnis-beantragen.html', 'IDENTITY_DOCUMENT', 'Identity card or passport', TRUE),
        ('77777777-7777-7777-7777-777777777722'::uuid, 'CERTIFICATE_OF_CONDUCT', 'https://www.dortmund.de/services/fuehrungszeugnis-beantragen.html', 'REQUESTING_AUTHORITY_DETAILS', 'Requesting authority name, address and reference', FALSE),
        ('77777777-7777-7777-7777-777777777723'::uuid, 'CERTIFICATE_OF_CONDUCT', 'https://www.dortmund.de/services/fuehrungszeugnis-beantragen.html', 'CERTIFIED_WRITTEN_APPLICATION', 'Signed application with certified signature for postal requests', FALSE),
        ('77777777-7777-7777-7777-777777777724'::uuid, 'CERTIFICATE_OF_CONDUCT', 'https://www.dortmund.de/services/fuehrungszeugnis-beantragen.html', 'EXTENDED_CERTIFICATE_REQUEST', 'Written request for an extended certificate of conduct', FALSE)
)
INSERT INTO requirement_definition (id, process_id, source_id, code, title, required, version)
SELECT seed.id, process.id, source.id, seed.code, seed.title, seed.required, 1
FROM requirement_seed seed
JOIN process_definition process ON process.code = seed.process_code
JOIN official_source source ON source.url = seed.source_url
ON CONFLICT DO NOTHING;

-- Registry-office requirements.

WITH requirement_seed (id, process_code, source_url, code, title, required) AS (
    VALUES
        ('77777777-7777-7777-7777-777777777731'::uuid, 'MARRIAGE_REGISTRATION', 'https://www.dortmund.de/services/eheschliessung-anmeldung.html', 'PARTNER_IDENTITY_DOCUMENTS', 'Valid identity documents for both partners', TRUE),
        ('77777777-7777-7777-7777-777777777732'::uuid, 'MARRIAGE_REGISTRATION', 'https://www.dortmund.de/services/eheschliessung-anmeldung.html', 'BIRTH_RECORD_DOCUMENTS', 'Birth record documents if requested by the registry office', FALSE),
        ('77777777-7777-7777-7777-777777777733'::uuid, 'MARRIAGE_REGISTRATION', 'https://www.dortmund.de/services/eheschliessung-anmeldung.html', 'RESIDENCE_CERTIFICATE', 'Residence certificate when registered outside Dortmund', FALSE),
        ('77777777-7777-7777-7777-777777777734'::uuid, 'MARRIAGE_REGISTRATION', 'https://www.dortmund.de/services/eheschliessung-anmeldung.html', 'PREVIOUS_MARRIAGE_DOCUMENTS', 'Documents concerning previous marriages, if applicable', FALSE),
        ('77777777-7777-7777-7777-777777777735'::uuid, 'MARRIAGE_REGISTRATION', 'https://www.dortmund.de/services/eheschliessung-anmeldung.html', 'FOREIGN_DOCUMENT_TRANSLATIONS', 'Certified translations and authentication for foreign documents', FALSE),
        ('77777777-7777-7777-7777-777777777741'::uuid, 'BIRTH_CERTIFICATE_REQUEST', 'https://www.dortmund.de/services/personenstandsurkunde-anfordern-nutzung-fuer-ahnen-oder-familienforschung-moeglich.html', 'PERSONAL_DETAILS', 'Full name, birth name and date of birth', TRUE),
        ('77777777-7777-7777-7777-777777777742'::uuid, 'BIRTH_CERTIFICATE_REQUEST', 'https://www.dortmund.de/services/personenstandsurkunde-anfordern-nutzung-fuer-ahnen-oder-familienforschung-moeglich.html', 'BIRTH_PLACE_DETAILS', 'Exact place of birth in Dortmund', TRUE),
        ('77777777-7777-7777-7777-777777777743'::uuid, 'BIRTH_CERTIFICATE_REQUEST', 'https://www.dortmund.de/services/personenstandsurkunde-anfordern-nutzung-fuer-ahnen-oder-familienforschung-moeglich.html', 'LEGAL_INTEREST_PROOF', 'Proof of relationship, authority or legal interest if required', FALSE)
)
INSERT INTO requirement_definition (id, process_id, source_id, code, title, required, version)
SELECT seed.id, process.id, source.id, seed.code, seed.title, seed.required, 1
FROM requirement_seed seed
JOIN process_definition process ON process.code = seed.process_code
JOIN official_source source ON source.url = seed.source_url
ON CONFLICT DO NOTHING;

-- Migration-office requirements.

WITH requirement_seed (id, process_code, source_url, code, title, required) AS (
    VALUES
        ('77777777-7777-7777-7777-777777777751'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'PASSPORT_AND_RESIDENCE_TITLE', 'Passport, identity documents and residence title', TRUE),
        ('77777777-7777-7777-7777-777777777752'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'BIRTH_CERTIFICATE', 'Birth certificate with certified translation if needed', TRUE),
        ('77777777-7777-7777-7777-777777777753'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'MARRIAGE_CIVIL_STATUS_DOCUMENTS', 'Marriage or civil-status documents, if applicable', FALSE),
        ('77777777-7777-7777-7777-777777777754'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'INCOME_EVIDENCE', 'Current evidence of all family income', TRUE),
        ('77777777-7777-7777-7777-777777777755'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'PENSION_INSURANCE_RECORD', 'Current pension insurance contribution record', TRUE),
        ('77777777-7777-7777-7777-777777777756'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'GERMAN_LANGUAGE_PROOF', 'Proof of German language skills', TRUE),
        ('77777777-7777-7777-7777-777777777757'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'CIVIC_KNOWLEDGE_PROOF', 'Proof of civic and legal knowledge', TRUE),
        ('77777777-7777-7777-7777-777777777758'::uuid, 'NATURALIZATION', 'https://www.dortmund.de/services/einbuergerung-38-4-2.html', 'CHILDREN_SCHOOL_CERTIFICATES', 'School certificates for school-age children included in the application', FALSE)
)
INSERT INTO requirement_definition (id, process_id, source_id, code, title, required, version)
SELECT seed.id, process.id, source.id, seed.code, seed.title, seed.required, 1
FROM requirement_seed seed
JOIN process_definition process ON process.code = seed.process_code
JOIN official_source source ON source.url = seed.source_url
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
JOIN process_definition process ON process.id = application.process_id
JOIN requirement_definition requirement ON requirement.process_id = process.id
WHERE process.code IN (
    'ADDRESS_REGISTRATION',
    'IDENTITY_CARD_APPLICATION',
    'PASSPORT_APPLICATION',
    'CERTIFICATE_OF_CONDUCT',
    'MARRIAGE_REGISTRATION',
    'BIRTH_CERTIFICATE_REQUEST',
    'NATURALIZATION'
)
ON CONFLICT DO NOTHING;
