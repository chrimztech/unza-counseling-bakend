ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS anonymous_identifier_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS anonymous_display_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS last_anonymous_activity_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS idx_users_anonymous_identifier_hash
    ON users (anonymous_identifier_hash)
    WHERE anonymous_identifier_hash IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_users_is_anonymous
    ON users (is_anonymous);

ALTER TABLE IF EXISTS self_assessments
    ADD COLUMN IF NOT EXISTS submitted_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS assessment_date TIMESTAMP,
    ADD COLUMN IF NOT EXISTS responses_json TEXT,
    ADD COLUMN IF NOT EXISTS phq9_score INTEGER,
    ADD COLUMN IF NOT EXISTS gad7_score INTEGER,
    ADD COLUMN IF NOT EXISTS pss_score INTEGER,
    ADD COLUMN IF NOT EXISTS sleep_quality INTEGER,
    ADD COLUMN IF NOT EXISTS overall_wellness INTEGER,
    ADD COLUMN IF NOT EXISTS appetite_changes BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS concentration_difficulty BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS social_withdrawal BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS submitted_as_anonymous BOOLEAN NOT NULL DEFAULT FALSE;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_self_assessments_submitted_by_user'
          AND table_name = 'self_assessments'
    ) THEN
        ALTER TABLE self_assessments
            ADD CONSTRAINT fk_self_assessments_submitted_by_user
            FOREIGN KEY (submitted_by_user_id) REFERENCES users(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_self_assessments_submitted_by_user
    ON self_assessments (submitted_by_user_id);

CREATE INDEX IF NOT EXISTS idx_self_assessments_assessment_date
    ON self_assessments (assessment_date);
