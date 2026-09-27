CREATE TABLE process_guide (
    id UUID PRIMARY KEY,
    process_id UUID NOT NULL UNIQUE REFERENCES process_definition(id) ON DELETE CASCADE,
    overview_en TEXT NOT NULL,
    overview_de TEXT NOT NULL,
    eligibility_en TEXT NOT NULL,
    eligibility_de TEXT NOT NULL,
    steps_en JSONB NOT NULL,
    steps_de JSONB NOT NULL,
    deadline_en TEXT NOT NULL,
    deadline_de TEXT NOT NULL,
    fee_en TEXT NOT NULL,
    fee_de TEXT NOT NULL,
    appointment_required BOOLEAN NOT NULL,
    appointment_information_en TEXT NOT NULL,
    appointment_information_de TEXT NOT NULL,
    appointment_url VARCHAR(2048),
    source_title VARCHAR(500) NOT NULL,
    source_url VARCHAR(2048) NOT NULL,
    verified_at DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT process_guide_steps_en_array
        CHECK (jsonb_typeof(steps_en) = 'array'),
    CONSTRAINT process_guide_steps_de_array
        CHECK (jsonb_typeof(steps_de) = 'array')
);


INSERT INTO process_guide (
    id,
    process_id,
    overview_en,
    overview_de,
    eligibility_en,
    eligibility_de,
    steps_en,
    steps_de,
    deadline_en,
    deadline_de,
    fee_en,
    fee_de,
    appointment_required,
    appointment_information_en,
    appointment_information_de,
    appointment_url,
    source_title,
    source_url,
    verified_at
)
SELECT
    '99999999-9999-9999-9999-999999999999',
    process.id,
    'Register your new primary residence in Dortmund after moving into a home.',
    'Melden Sie Ihren neuen Hauptwohnsitz in Dortmund nach dem Einzug an.',
    'People aged 16 or older generally register themselves. The responsible Dortmund office and the available service route depend on citizenship and personal circumstances, so check the official source before starting.',
    'Personen ab 16 Jahren melden sich grundsätzlich selbst an. Welche Dortmunder Stelle zuständig ist und welcher Antragsweg möglich ist, hängt von Staatsangehörigkeit und persönlicher Situation ab. Prüfen Sie deshalb vorab die offizielle Quelle.',
    '[
        "Gather the identity cards and passports of everyone who is registering.",
        "Obtain a current landlord confirmation. Homeowners should prepare proof of ownership.",
        "Use the online service if you meet its requirements, or book an appointment with the responsible Dortmund office.",
        "Complete the registration within two weeks after moving in.",
        "Keep the registration confirmation and make sure address data is updated where required."
    ]'::jsonb,
    '[
        "Legen Sie die Personalausweise und Reisepässe aller Personen bereit, die angemeldet werden.",
        "Besorgen Sie eine aktuelle Wohnungsgeberbestätigung. Eigentümerinnen und Eigentümer sollten einen Eigentumsnachweis bereithalten.",
        "Nutzen Sie den Onlinedienst, wenn Sie die Voraussetzungen erfüllen, oder buchen Sie einen Termin bei der zuständigen Dortmunder Stelle.",
        "Führen Sie die Anmeldung innerhalb von zwei Wochen nach dem Einzug durch.",
        "Bewahren Sie die Meldebestätigung auf und lassen Sie Adressdaten aktualisieren, soweit dies erforderlich ist."
    ]'::jsonb,
    'Register within two weeks after moving into the new home.',
    'Melden Sie sich innerhalb von zwei Wochen nach dem Einzug in die neue Wohnung an.',
    'The registration confirmation is free of charge.',
    'Die Meldebestätigung ist gebührenfrei.',
    TRUE,
    'An appointment is required for an in-person visit. Eligible users can instead use the online residence-registration service with the required electronic identification and BundID. The responsible office depends on citizenship and personal circumstances.',
    'Für eine persönliche Vorsprache ist ein Termin erforderlich. Berechtigte Personen können stattdessen den Onlinedienst mit der erforderlichen Online-Ausweisfunktion und BundID nutzen. Die zuständige Stelle hängt von Staatsangehörigkeit und persönlicher Situation ab.',
    'https://www.dortmund.de/services/online-terminreservierung.html',
    'Wohnsitz in Dortmund anmelden',
    'https://www.dortmund.de/services/wohnsitzanmeldung.html',
    DATE '2026-09-27'
FROM process_definition process
WHERE process.code = 'ADDRESS_REGISTRATION'
ON CONFLICT (process_id) DO NOTHING;