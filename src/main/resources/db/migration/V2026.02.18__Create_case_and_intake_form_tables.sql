-- Migration: Create tables for Case Management and Client Intake Forms
-- Created: 2026-02-18
-- This migration creates tables for:
--   - Cases (case management)
--   - Case Assignments (tracking counselor assignments)
--   - Personal Data Forms (client intake forms)
--   - Family Members (family history in intake forms)
--   - Reasons for Counselling (nested object in intake forms)

-- =====================================================================
-- CASES TABLE
-- =====================================================================
CREATE TABLE IF NOT EXISTS cases (
    id BIGSERIAL PRIMARY KEY,
    case_number VARCHAR(50) UNIQUE NOT NULL,
    client_id BIGINT NOT NULL,
    counselor_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    subject VARCHAR(200),
    description TEXT,
    notes TEXT,
    assigned_by BIGINT,
    assigned_at TIMESTAMP,
    last_activity_at TIMESTAMP,
    expected_resolution_date TIMESTAMP,
    actual_resolution_date TIMESTAMP,
    escalation_level INTEGER DEFAULT 0,
    tags VARCHAR(1000),
    custom_fields VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    closed_at TIMESTAMP,
    CONSTRAINT fk_cases_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_cases_counselor FOREIGN KEY (counselor_id) REFERENCES users(id),
    CONSTRAINT fk_cases_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_cases_client ON cases(client_id);
CREATE INDEX IF NOT EXISTS idx_cases_counselor ON cases(counselor_id);
CREATE INDEX IF NOT EXISTS idx_cases_status ON cases(status);

-- =====================================================================
-- CASE ASSIGNMENTS TABLE
-- =====================================================================
CREATE TABLE IF NOT EXISTS case_assignments (
    id BIGSERIAL PRIMARY KEY,
    case_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_to BIGINT NOT NULL,
    assignment_reason VARCHAR(1000),
    assignment_notes VARCHAR(2000),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    closed_at TIMESTAMP,
    CONSTRAINT fk_case_assignments_case FOREIGN KEY (case_id) REFERENCES cases(id),
    CONSTRAINT fk_case_assignments_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id),
    CONSTRAINT fk_case_assignments_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_case_assignments_case ON case_assignments(case_id);
CREATE INDEX IF NOT EXISTS idx_case_assignments_assigned_to ON case_assignments(assigned_to);
CREATE INDEX IF NOT EXISTS idx_case_assignments_status ON case_assignments(status);

-- =====================================================================
-- PERSONAL DATA FORMS TABLE (Client Intake Forms)
-- =====================================================================
CREATE TABLE IF NOT EXISTS personal_data_forms (
    id BIGSERIAL PRIMARY KEY,
    client_file_no VARCHAR(50) UNIQUE,
    client_id BIGINT NOT NULL,
    case_id BIGINT,
    computer_no VARCHAR(50),
    occupation VARCHAR(100),
    contact_address VARCHAR(500),
    marital_status VARCHAR(30),
    previous_counselling_other TEXT,
    referral_source_other TEXT,
    health_status VARCHAR(3),
    health_condition TEXT,
    taking_medication VARCHAR(3),
    medication_details TEXT,
    additional_info TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_personal_data_forms_client FOREIGN KEY (client_id) REFERENCES users(id),
    CONSTRAINT fk_personal_data_forms_case FOREIGN KEY (case_id) REFERENCES cases(id)
);

CREATE INDEX IF NOT EXISTS idx_personal_data_forms_client ON personal_data_forms(client_id);
CREATE INDEX IF NOT EXISTS idx_personal_data_forms_case ON personal_data_forms(case_id);
CREATE INDEX IF NOT EXISTS idx_personal_data_forms_client_file_no ON personal_data_forms(client_file_no);

-- =====================================================================
-- PREVIOUS COUNSELLING TABLE (Enum array for PersonalDataForm)
-- =====================================================================
CREATE TABLE IF NOT EXISTS form_previous_counselling (
    form_id BIGINT NOT NULL,
    counselling_type VARCHAR(30) NOT NULL,
    CONSTRAINT fk_form_previous_counselling_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, counselling_type)
);

-- =====================================================================
-- REFERRAL SOURCES TABLE (Enum array for PersonalDataForm)
-- =====================================================================
CREATE TABLE IF NOT EXISTS form_referral_sources (
    form_id BIGINT NOT NULL,
    referral_source VARCHAR(30) NOT NULL,
    CONSTRAINT fk_form_referral_sources_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, referral_source)
);

-- =====================================================================
-- REASONS FOR COUNSELLING TABLES (Nested object)
-- =====================================================================

-- Personal Reasons
CREATE TABLE IF NOT EXISTS reasons_personal (
    form_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reasons_personal_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, reason)
);

-- Personal Other (add column to personal_data_forms)
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS personal_other TEXT;

-- Health Reasons
CREATE TABLE IF NOT EXISTS reasons_health (
    form_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reasons_health_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, reason)
);

-- Health Other
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS health_other TEXT;

-- Educational Reasons
CREATE TABLE IF NOT EXISTS reasons_educational (
    form_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reasons_educational_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, reason)
);

-- Educational Other
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS educational_other TEXT;

-- Career Reasons
CREATE TABLE IF NOT EXISTS reasons_career (
    form_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reasons_career_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, reason)
);

-- Career Other
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS career_other TEXT;

-- Financial Reasons
CREATE TABLE IF NOT EXISTS reasons_financial (
    form_id BIGINT NOT NULL,
    reason VARCHAR(50) NOT NULL,
    CONSTRAINT fk_reasons_financial_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE,
    PRIMARY KEY (form_id, reason)
);

-- Financial Other
ALTER TABLE personal_data_forms ADD COLUMN IF NOT EXISTS financial_other TEXT;

-- =====================================================================
-- FAMILY MEMBERS TABLE
-- =====================================================================
CREATE TABLE IF NOT EXISTS family_members (
    id BIGSERIAL PRIMARY KEY,
    form_id BIGINT NOT NULL,
    name VARCHAR(100),
    relationship VARCHAR(50),
    age VARCHAR(10),
    education VARCHAR(100),
    occupation VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_family_members_form FOREIGN KEY (form_id) REFERENCES personal_data_forms(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_family_members_form ON family_members(form_id);

-- =====================================================================
-- UPDATE CASE STATUS AND PRIORITY ENUMS
-- =====================================================================
-- Add comments to document valid values
COMMENT ON COLUMN cases.status IS 'Valid values: OPEN, IN_PROGRESS, CLOSED, ON_HOLD, RESOLVED';
COMMENT ON COLUMN cases.priority IS 'Valid values: LOW, MEDIUM, HIGH, CRITICAL';
COMMENT ON COLUMN case_assignments.status IS 'Valid values: ACTIVE, CLOSED';
COMMENT ON COLUMN personal_data_forms.marital_status IS 'Valid values: SINGLE, MARRIED, DIVORCED, SEPARATED, WIDOWED, LIVING_WITH_PARTNER';
COMMENT ON COLUMN personal_data_forms.health_status IS 'Valid values: YES, NO';
COMMENT ON COLUMN personal_data_forms.taking_medication IS 'Valid values: YES, NO';
COMMENT ON COLUMN form_previous_counselling.counselling_type IS 'Valid values: UNIVERSITY, SUBJECT_COUNSELLOR, OTHER';
COMMENT ON COLUMN form_referral_sources.referral_source IS 'Valid values: SELF, SUBJECT_COUNSELLOR, FRIEND, PARTNER, FAMILY, HEALTH_WORKER, OTHER';
