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


-- Insert sample admin profile
INSERT INTO admins (user_id, admin_level, permissions) VALUES
((SELECT id FROM users WHERE email = 'admin@unza.zm'), 'SYSTEM_ADMIN', '{"system_config": true, "user_management": true, "backup_restore": true, "audit_logs": true}');


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

-- Note: Complex functions with dollar quotes will be added in future migrations
-- For now, keeping the migration simple to avoid parsing issues

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