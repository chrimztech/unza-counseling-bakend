-- Clinic referrals: counseling system refers clients to the university clinic
CREATE TABLE IF NOT EXISTS clinic_referrals (
    id                      BIGSERIAL PRIMARY KEY,
    referral_number         VARCHAR(50)   NOT NULL UNIQUE,
    client_id               BIGINT        NOT NULL REFERENCES users(id),
    counselor_id            BIGINT        NOT NULL REFERENCES users(id),
    case_id                 BIGINT        REFERENCES cases(id),
    urgency                 VARCHAR(20)   NOT NULL DEFAULT 'ROUTINE',
    status                  VARCHAR(30)   NOT NULL DEFAULT 'PENDING',
    reason                  TEXT          NOT NULL,
    clinical_notes          TEXT,
    clinic_notes            TEXT,
    clinic_appointment_date TIMESTAMP,
    external_reference_id   VARCHAR(200),          -- clinic system's own record ID
    sent_at                 TIMESTAMP,
    responded_at            TIMESTAMP,
    created_at              TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_clinic_referrals_client     ON clinic_referrals(client_id);
CREATE INDEX IF NOT EXISTS idx_clinic_referrals_counselor  ON clinic_referrals(counselor_id);
CREATE INDEX IF NOT EXISTS idx_clinic_referrals_case       ON clinic_referrals(case_id);
CREATE INDEX IF NOT EXISTS idx_clinic_referrals_status     ON clinic_referrals(status);

-- Clinic visits: records of a client's actual visits to the university clinic
-- Can be entered manually by counselors or pushed in by the clinic via webhook
CREATE TABLE IF NOT EXISTS clinic_visits (
    id                  BIGSERIAL PRIMARY KEY,
    client_id           BIGINT        NOT NULL REFERENCES users(id),
    referral_id         BIGINT        REFERENCES clinic_referrals(id),
    visit_date          TIMESTAMP     NOT NULL,
    visit_type          VARCHAR(30)   NOT NULL DEFAULT 'GENERAL',
    visit_purpose       VARCHAR(500),
    notes               TEXT,
    recorded_by         VARCHAR(50)   NOT NULL DEFAULT 'MANUAL',  -- 'MANUAL' | 'CLINIC_WEBHOOK'
    counselor_notified  BOOLEAN       DEFAULT FALSE,
    created_at          TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_clinic_visits_client      ON clinic_visits(client_id);
CREATE INDEX IF NOT EXISTS idx_clinic_visits_visit_date  ON clinic_visits(visit_date);
CREATE INDEX IF NOT EXISTS idx_clinic_visits_referral    ON clinic_visits(referral_id);
