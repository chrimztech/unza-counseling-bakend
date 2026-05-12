-- Create scholarships table
CREATE TABLE IF NOT EXISTS scholarships (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sponsor VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2),
    type VARCHAR(50) NOT NULL,
    deadline DATE,
    academic_year VARCHAR(20),
    max_recipients INTEGER,
    eligibility_criteria TEXT,
    required_min_gpa DOUBLE PRECISION,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Create scholarship_recommendations table
CREATE TABLE IF NOT EXISTS scholarship_recommendations (
    id BIGSERIAL PRIMARY KEY,
    scholarship_id BIGINT NOT NULL REFERENCES scholarships(id) ON DELETE CASCADE,
    client_id BIGINT NOT NULL REFERENCES users(id),
    recommended_by BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    justification TEXT,
    financial_need_level VARCHAR(20),
    vulnerability_score INTEGER,
    academic_standing VARCHAR(100),
    personal_statement TEXT,
    supporting_notes TEXT,
    approved_by BIGINT,
    rejection_reason TEXT,
    awarded_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    UNIQUE (scholarship_id, client_id)
);

CREATE INDEX IF NOT EXISTS idx_scholarship_recommendations_scholarship ON scholarship_recommendations(scholarship_id);
CREATE INDEX IF NOT EXISTS idx_scholarship_recommendations_client ON scholarship_recommendations(client_id);
CREATE INDEX IF NOT EXISTS idx_scholarship_recommendations_status ON scholarship_recommendations(status);
CREATE INDEX IF NOT EXISTS idx_scholarships_status ON scholarships(status);
