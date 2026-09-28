ALTER TABLE application_advice
ADD COLUMN user_answers JSONB NOT NULL DEFAULT '{}'::jsonb
    CHECK (jsonb_typeof(user_answers) = 'object');
