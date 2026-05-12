-- ============================================================
-- Clinical fields for appointments (triage + intake capture)
-- ============================================================

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS urgency_level         VARCHAR(20)  DEFAULT 'ROUTINE',
    ADD COLUMN IF NOT EXISTS presenting_concern    TEXT,
    ADD COLUMN IF NOT EXISTS referral_source       VARCHAR(200),
    ADD COLUMN IF NOT EXISTS previous_counseling   BOOLEAN,
    ADD COLUMN IF NOT EXISTS consent_acknowledged  BOOLEAN;

-- ============================================================
-- Clinical fields for sessions (structured progress notes)
-- ============================================================

ALTER TABLE sessions
    ADD COLUMN IF NOT EXISTS session_number        INTEGER,
    ADD COLUMN IF NOT EXISTS client_mood_rating    INTEGER       CHECK (client_mood_rating BETWEEN 1 AND 10),
    ADD COLUMN IF NOT EXISTS risk_level_assessed   VARCHAR(20),
    ADD COLUMN IF NOT EXISTS goals_addressed       TEXT,
    ADD COLUMN IF NOT EXISTS homework_assigned     TEXT;

-- Index to retrieve sessions by appointment quickly
CREATE INDEX IF NOT EXISTS idx_session_appointment ON sessions (appointment_id);
