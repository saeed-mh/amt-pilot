-- Residence-permit extension guidance from official City of Dortmund pages,
-- checked on 2026-09-14. Exact documents can vary by residence purpose.

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
    '55555555-5555-5555-5555-555555555607',
    '55555555-5555-5555-5555-555555555552',
    'RESIDENCE_PERMIT_EXTENSION',
    'Residence Permit Extension',
    'Dortmund',
    'IMMIGRATION',
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
        '66666666-6666-6666-6666-666666666607',
        '55555555-5555-5555-5555-555555555552',
        'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/familiaere-und-unbefristete-aufenthalte-38-4-1/',
        'Familiäre und unbefristete Aufenthalte',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666608',
        '55555555-5555-5555-5555-555555555552',
        'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/ausbildung-und-studium-38-2-2/',
        'Ausbildung und Studium',
        'Dortmund',
        'de'
    ),
    (
        '66666666-6666-6666-6666-666666666609',
        '55555555-5555-5555-5555-555555555552',
        'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/einreise-und-erwerbstaetigkeit-38-2-1/erwerbstaetigkeit/',
        'Erwerbstätigkeit',
        'Dortmund',
        'de'
    )
ON CONFLICT DO NOTHING;

WITH requirement_seed (id, source_url, code, title, required) AS (
    VALUES
        (
            '77777777-7777-7777-7777-777777777761'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/familiaere-und-unbefristete-aufenthalte-38-4-1/',
            'VALID_PASSPORT',
            'Valid national passport (original and copy)',
            TRUE
        ),
        (
            '77777777-7777-7777-7777-777777777762'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/familiaere-und-unbefristete-aufenthalte-38-4-1/',
            'CURRENT_RESIDENCE_DOCUMENT',
            'Current residence permit or temporary residence document with supplementary sheet (original and copy)',
            TRUE
        ),
        (
            '77777777-7777-7777-7777-777777777763'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/familiaere-und-unbefristete-aufenthalte-38-4-1/',
            'BIOMETRIC_PHOTO',
            'Current biometric passport photo',
            TRUE
        ),
        (
            '77777777-7777-7777-7777-777777777764'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/familiaere-und-unbefristete-aufenthalte-38-4-1/',
            'FINANCIAL_EVIDENCE',
            'Current proof of financial means',
            TRUE
        ),
        (
            '77777777-7777-7777-7777-777777777765'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/ausbildung-und-studium-38-2-2/',
            'HEALTH_INSURANCE_PROOF',
            'Current proof of health insurance, if requested for your residence purpose',
            FALSE
        ),
        (
            '77777777-7777-7777-7777-777777777766'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/ausbildung-und-studium-38-2-2/',
            'ENROLLMENT_OR_TRAINING_PROOF',
            'Current enrollment, study-progress or training evidence, if applicable',
            FALSE
        ),
        (
            '77777777-7777-7777-7777-777777777767'::uuid,
            'https://www.dortmund.de/themen/aufenthalt-einbuergerung-und-auslaenderwesen/einreise-und-erwerbstaetigkeit-38-2-1/erwerbstaetigkeit/',
            'EMPLOYMENT_EVIDENCE',
            'Current employer certificate, employment contract and recent payslips, if applicable',
            FALSE
        )
)
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
    seed.id,
    process.id,
    source.id,
    seed.code,
    seed.title,
    seed.required,
    1
FROM requirement_seed seed
JOIN process_definition process
    ON process.code = 'RESIDENCE_PERMIT_EXTENSION'
JOIN official_source source
    ON source.url = seed.source_url
ON CONFLICT DO NOTHING;
