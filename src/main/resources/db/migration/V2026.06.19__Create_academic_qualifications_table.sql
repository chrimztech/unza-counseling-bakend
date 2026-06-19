CREATE TABLE IF NOT EXISTS academic_qualifications (
    id                    BIGSERIAL PRIMARY KEY,
    client_id             BIGINT       NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    student_id            VARCHAR(50)  NOT NULL,

    -- Course information
    course_code           VARCHAR(50)  NOT NULL,
    course_title          VARCHAR(255),
    credit_hours          INTEGER,
    semester              VARCHAR(20),
    academic_year         VARCHAR(10),

    -- Grade information
    grade                 VARCHAR(10),
    grade_point           NUMERIC(5, 2),
    marks                 NUMERIC(6, 2),
    course_status         VARCHAR(30),
    course_type           VARCHAR(20),

    -- Academic standing snapshot at sync time
    current_gpa           NUMERIC(5, 2),
    cumulative_gpa        NUMERIC(5, 2),
    total_credits_earned  INTEGER,
    total_credits_attempted INTEGER,
    academic_standing     VARCHAR(50),

    -- Sync metadata
    sis_sync_date         TIMESTAMP,
    external_data_hash    VARCHAR(128),
    is_active             BOOLEAN      NOT NULL DEFAULT TRUE,

    -- Audit
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_aq_client_id    ON academic_qualifications(client_id);
CREATE INDEX IF NOT EXISTS idx_aq_student_id   ON academic_qualifications(student_id);
CREATE INDEX IF NOT EXISTS idx_aq_course_code  ON academic_qualifications(course_code);
CREATE INDEX IF NOT EXISTS idx_aq_academic_year ON academic_qualifications(academic_year);
CREATE INDEX IF NOT EXISTS idx_aq_is_active    ON academic_qualifications(is_active);
