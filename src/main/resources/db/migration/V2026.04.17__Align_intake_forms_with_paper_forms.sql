-- Align the digital intake records with the UNZA paper personal data form
-- and the counselor-completed client intake form.

ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS date_of_interview DATE;
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS sex VARCHAR(30);
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS year_of_birth INTEGER;
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS age INTEGER;
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS school VARCHAR(200);
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS year_of_study INTEGER;
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30);

COMMENT ON COLUMN personal_data_forms.sex IS 'Paper form field: Male/Female';
COMMENT ON COLUMN personal_data_forms.school IS 'Paper form field labelled School';
COMMENT ON COLUMN form_previous_counselling.counselling_type IS 'Valid values now include NONE and SUBJECT_COUNSELLOR_TUTOR_DEAN';
COMMENT ON COLUMN form_referral_sources.referral_source IS 'Valid values now include SELF_REFERRAL, SUBJECT_COUNSELLOR_TUTOR_DEAN, HEALTH_WORKER_CLINIC';

CREATE TABLE IF NOT EXISTS client_intake_forms (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL UNIQUE,
    case_id BIGINT,
    counselor_id BIGINT,
    client_file_no VARCHAR(50),
    sex VARCHAR(30),
    age INTEGER,
    marital_status VARCHAR(30),
    computer_no VARCHAR(50),
    year_of_study INTEGER,
    school VARCHAR(200),
    hall_room_no VARCHAR(100),
    contact_phone_no VARCHAR(30),
    presenting_concern TEXT,
    problem_conceptualization TEXT,
    tentative_goals_directions TEXT,
    coping_strategies TEXT,
    action_taken TEXT,
    time_taken_counselling VARCHAR(100),
    next_contact_appointment_date DATE,
    number_of_contact_sessions INTEGER,
    counsellor_name VARCHAR(200),
    form_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_client_intake_forms_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_client_intake_forms_case FOREIGN KEY (case_id) REFERENCES cases(id),
    CONSTRAINT fk_client_intake_forms_counselor FOREIGN KEY (counselor_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_client_intake_forms_case ON client_intake_forms(case_id);
CREATE INDEX IF NOT EXISTS idx_client_intake_forms_counselor ON client_intake_forms(counselor_id);
