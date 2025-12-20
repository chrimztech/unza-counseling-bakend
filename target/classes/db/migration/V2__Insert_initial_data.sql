-- UNZA Counseling Management System - Initial Data
-- Version: 2.0.0
-- Created: 2025-12-20

-- Insert default roles
INSERT INTO roles (name, description, permissions, is_active) VALUES
('SUPER_ADMIN', 'Full system administrator with all permissions', 
 '{"users": ["CREATE", "READ", "UPDATE", "DELETE"], "system": ["CONFIGURE", "BACKUP", "RESTORE"], "reports": ["VIEW_ALL", "EXPORT"]}', true),
('ADMIN', 'System administrator with limited permissions', 
 '{"users": ["CREATE", "READ", "UPDATE"], "reports": ["VIEW_DEPARTMENT"], "appointments": ["MANAGE_ALL"]}', true),
('COUNSELOR', 'Licensed counselor providing services', 
 '{"clients": ["READ", "UPDATE"], "appointments": ["MANAGE_OWN"], "sessions": ["CREATE", "READ", "UPDATE"], "assessments": ["CREATE", "READ", "UPDATE"]}', true),
('CLIENT', 'Student or person seeking counseling services', 
 '{"profile": ["READ", "UPDATE"], "appointments": ["CREATE_OWN", "READ_OWN"], "assessments": ["CREATE", "READ_OWN"]}', true);

-- Insert default admin user
INSERT INTO users (email, password, first_name, last_name, role, is_active, is_verified) VALUES
('admin@unza.zm', '$2a$10$rOzRkKp5n9vF3K4xH8W6TeO9xJ2M5N8P1Q4R7T2Y6U8I0O3P6Q9S2V5X8', 'System', 'Administrator', 'SUPER_ADMIN', true, true);

-- Insert sample counselors
INSERT INTO users (email, password, first_name, last_name, phone_number, role, is_active, is_verified) VALUES
('john.mwanza@unza.zm', '$2a$10$rOzRkKp5n9vF3K4xH8W6TeO9xJ2M5N8P1Q4R7T2Y6U8I0O3P6Q9S2V5X8', 'John', 'Mwanza', '+260977123456', 'COUNSELOR', true, true),
('grace.chiluba@unza.zm', '$2a$10$rOzRkKp5n9vF3K4xH8W6TeO9xJ2M5N8P1Q4R7T2Y6U8I0O3P6Q9S2V5X8', 'Grace', 'Chiluba', '+260977654321', 'COUNSELOR', true, true),
('michael.simukoko@unza.zm', '$2a$10$rOzRkKp5n9vF3K4xH8W6TeO9xJ2M5N8P1Q4R7T2Y6U8I0O3P6Q9S2V5X8', 'Michael', 'Simukoko', '+260977987654', 'COUNSELOR', true, true);

-- Insert sample counselor profiles using user IDs from the users table
INSERT INTO counselors (user_id, employee_id, specialization, license_number, years_of_experience, bio, education, certifications, available_hours, max_clients_per_day, hourly_rate, is_available) VALUES
((SELECT id FROM users WHERE email = 'john.mwanza@unza.zm'), 'CNS001', ARRAY['Anxiety Disorders', 'Depression', 'Academic Stress'], 'LIC2023001', 8,
 'Experienced counselor specializing in anxiety and depression treatment with focus on academic stress management.',
 'M.Sc. Psychology, University of Zambia',
 ARRAY['Certified Cognitive Behavioral Therapist', 'Academic Counseling Certification'],
 '{"monday": {"start": "08:00", "end": "17:00"}, "tuesday": {"start": "08:00", "end": "17:00"}, "wednesday": {"start": "08:00", "end": "17:00"}, "thursday": {"start": "08:00", "end": "17:00"}, "friday": {"start": "08:00", "end": "17:00"}}',
 8, 150.00, true),
((SELECT id FROM users WHERE email = 'grace.chiluba@unza.zm'), 'CNS002', ARRAY['Relationship Counseling', 'Family Therapy', 'Youth Counseling'], 'LIC2023002', 5,
 'Specializes in relationship and family therapy with extensive experience in youth counseling.',
 'M.A. Clinical Psychology, University of Lusaka',
 ARRAY['Family Therapy Certification', 'Youth Mental Health First Aid'],
 '{"monday": {"start": "09:00", "end": "18:00"}, "tuesday": {"start": "09:00", "end": "18:00"}, "wednesday": {"start": "09:00", "end": "18:00"}, "thursday": {"start": "09:00", "end": "18:00"}, "friday": {"start": "09:00", "end": "18:00"}}',
 6, 120.00, true),
((SELECT id FROM users WHERE email = 'michael.simukoko@unza.zm'), 'CNS003', ARRAY['Addiction Counseling', 'Trauma Therapy', 'Crisis Intervention'], 'LIC2023003', 12,
 'Senior counselor with expertise in addiction and trauma therapy, providing crisis intervention services.',
 'Ph.D. Counseling Psychology, University of Cape Town',
 ARRAY['Addiction Counseling Certification', 'Trauma-Informed Care', 'Crisis Intervention Specialist'],
 '{"monday": {"start": "07:00", "end": "15:00"}, "tuesday": {"start": "07:00", "end": "15:00"}, "wednesday": {"start": "07:00", "end": "15:00"}, "thursday": {"start": "07:00", "end": "15:00"}, "friday": {"start": "07:00", "end": "15:00"}}',
 10, 200.00, true);

-- Insert sample admin profile
INSERT INTO admins (user_id, admin_level, permissions) VALUES
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'SYSTEM_ADMIN', '{"system_config": true, "user_management": true, "backup_restore": true, "audit_logs": true}');

-- Insert system resources
INSERT INTO resources (title, description, content_type, category, tags, is_public, uploaded_by) VALUES
('Mental Health Awareness Guide', 'Comprehensive guide to understanding mental health issues in university settings', 'PDF', 'Educational',
 ARRAY['mental health', 'awareness', 'guide'], true, (SELECT id FROM users WHERE email = 'admin@unza.zm')),
('Stress Management Techniques', 'Practical techniques for managing academic and personal stress', 'PDF', 'Self-Help',
 ARRAY['stress', 'management', 'techniques'], true, (SELECT id FROM users WHERE email = 'admin@unza.zm')),
('Crisis Intervention Protocol', 'Emergency response procedures for mental health crises', 'PDF', 'Protocol',
 ARRAY['crisis', 'intervention', 'emergency'], false, (SELECT id FROM users WHERE email = 'admin@unza.zm')),
('Counseling Intake Form Template', 'Standard intake form for new counseling clients', 'DOC', 'Forms',
 ARRAY['intake', 'form', 'template'], false, (SELECT id FROM users WHERE email = 'admin@unza.zm')),
('Academic Performance and Mental Health Correlation Study', 'Research findings on the relationship between academic performance and mental health', 'PDF', 'Research',
 ARRAY['research', 'academic', 'performance', 'mental health'], true, (SELECT id FROM users WHERE email = 'admin@unza.zm'));

-- Insert sample self-assessment questions (for future use)
-- This could be expanded to include a full assessment questionnaire
-- Sample data would go here once client records exist
-- For now, we'll leave this as a placeholder for future data

-- Insert sample academic performance data
-- This would also be populated once client records exist

-- Insert system configuration settings (could be expanded for feature flags, etc.)
-- This could be added to a configuration table in future migrations

-- Create views for common queries
CREATE OR REPLACE VIEW counselor_workload AS
SELECT 
    c.id as counselor_id,
    u.first_name || ' ' || u.last_name as counselor_name,
    c.employee_id,
    COUNT(a.id) as total_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_appointments,
    COUNT(CASE WHEN a.status = 'SCHEDULED' THEN 1 END) as scheduled_appointments,
    COUNT(CASE WHEN a.appointment_date = CURRENT_DATE THEN 1 END) as today_appointments
FROM counselors c
JOIN users u ON c.user_id = u.id
LEFT JOIN appointments a ON c.id = a.counselor_id
WHERE c.is_available = true
GROUP BY c.id, u.first_name, u.last_name, c.employee_id;

CREATE OR REPLACE VIEW client_dashboard AS
SELECT 
    cl.id as client_id,
    u.first_name || ' ' || u.last_name as client_name,
    cl.student_id,
    cl.program,
    cl.faculty,
    COUNT(a.id) as total_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) as completed_sessions,
    MAX(a.appointment_date) as last_appointment_date,
    COUNT(sa.id) as total_assessments,
    MAX(sa.assessment_date) as last_assessment_date
FROM clients cl
JOIN users u ON cl.user_id = u.id
LEFT JOIN appointments a ON cl.id = a.client_id
LEFT JOIN self_assessments sa ON cl.id = sa.client_id
GROUP BY cl.id, u.first_name, u.last_name, cl.student_id, cl.program, cl.faculty;

-- Create functions for common operations
CREATE OR REPLACE FUNCTION get_client_risk_summary(client_uuid BIGINT)
RETURNS TABLE (
    risk_level risk_level,
    assessment_count BIGINT,
    latest_assessment TIMESTAMP,
    high_risk_flags JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(ra.risk_level, 'LOW'::risk_level) as risk_level,
        COUNT(ra.id) as assessment_count,
        MAX(ra.assessment_date) as latest_assessment,
        jsonb_build_object(
            'suicide_risk', COALESCE(bool_or(ra.suicide_risk), false),
            'self_harm_risk', COALESCE(bool_or(ra.self_harm_risk), false),
            'substance_abuse_risk', COALESCE(bool_or(ra.substance_abuse_risk), false),
            'academic_dropout_risk', COALESCE(bool_or(ra.academic_dropout_risk), false)
        ) as high_risk_flags
    FROM clients c
    LEFT JOIN risk_assessments ra ON c.id = ra.client_id
    WHERE c.id = client_uuid
    GROUP BY ra.risk_level;
END;
$$ LANGUAGE plpgsql;

-- Create indexes for performance
CREATE INDEX idx_appointments_client_date ON appointments(client_id, appointment_date);
CREATE INDEX idx_appointments_counselor_date ON appointments(counselor_id, appointment_date);
CREATE INDEX idx_sessions_date_status ON sessions(session_date, status);
CREATE INDEX idx_risk_assessments_level_date ON risk_assessments(risk_level, assessment_date);
CREATE INDEX idx_self_assessments_client_date ON self_assessments(client_id, assessment_date);

-- Insert audit log entries for initial setup (will be added after user IDs are known)
INSERT INTO audit_log (user_id, action, entity_type, entity_id, timestamp) VALUES
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'INITIAL_SETUP', 'SYSTEM', 1, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'CREATE', 'ROLES', 1, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'CREATE', 'USERS', 1, CURRENT_TIMESTAMP),
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'CREATE', 'COUNSELORS', 1, CURRENT_TIMESTAMP);