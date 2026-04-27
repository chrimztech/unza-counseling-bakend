-- Restore the appointment views after V2025.02.16 converts
-- appointments.appointment_date to TIMESTAMP and counselor_id to users.id.
CREATE OR REPLACE VIEW counselor_workload AS
SELECT
    c.id AS counselor_id,
    u.first_name || ' ' || u.last_name AS counselor_name,
    c.employee_id,
    COUNT(a.id) AS total_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) AS completed_appointments,
    COUNT(CASE WHEN a.status = 'SCHEDULED' THEN 1 END) AS scheduled_appointments,
    COUNT(CASE WHEN DATE(a.appointment_date) = CURRENT_DATE THEN 1 END) AS today_appointments
FROM counselors c
JOIN users u ON c.user_id = u.id
LEFT JOIN appointments a ON c.user_id = a.counselor_id
WHERE c.is_available = true
GROUP BY c.id, u.first_name, u.last_name, c.employee_id;

CREATE OR REPLACE VIEW client_dashboard AS
SELECT
    cl.id AS client_id,
    u.first_name || ' ' || u.last_name AS client_name,
    cl.student_id,
    cl.program,
    cl.faculty,
    COUNT(a.id) AS total_appointments,
    COUNT(CASE WHEN a.status = 'COMPLETED' THEN 1 END) AS completed_sessions,
    MAX(a.appointment_date) AS last_appointment_date,
    COUNT(sa.id) AS total_assessments,
    MAX(sa.assessment_date) AS last_assessment_date
FROM clients cl
JOIN users u ON cl.user_id = u.id
LEFT JOIN appointments a ON cl.id = a.client_id
LEFT JOIN self_assessments sa ON cl.id = sa.client_id
GROUP BY cl.id, u.first_name, u.last_name, cl.student_id, cl.program, cl.faculty;

CREATE INDEX IF NOT EXISTS idx_appointments_client_date ON appointments(client_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_counselor_date ON appointments(counselor_id, appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_date_time ON appointments(appointment_date, start_time);
