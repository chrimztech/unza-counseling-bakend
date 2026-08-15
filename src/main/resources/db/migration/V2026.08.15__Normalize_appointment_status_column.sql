-- Align appointment status persistence with JPA EnumType.STRING usage, same fix
-- already applied to sessions.status in V2026.04.18__Normalize_session_status_column.sql.
-- The legacy PostgreSQL "appointment_status" enum type causes queries to fail
-- (operator does not exist: appointment_status = character varying) whenever
-- Hibernate binds the Java enum as a VARCHAR parameter, e.g. any WHERE status = ?.
--
-- counselor_workload and client_dashboard (created in
-- V2025.02.16.1__Recreate_appointment_views_after_column_retype.sql) depend on
-- this column, so they're dropped and recreated around the type change, same
-- pattern as that migration used.

DROP VIEW IF EXISTS counselor_workload;
DROP VIEW IF EXISTS client_dashboard;

ALTER TABLE IF EXISTS appointments
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE IF EXISTS appointments
    ALTER COLUMN status TYPE VARCHAR(50)
    USING status::text;

ALTER TABLE IF EXISTS appointments
    ALTER COLUMN status SET DEFAULT 'SCHEDULED';

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
