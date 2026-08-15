-- Same fix as V2026.04.18__Normalize_session_status_column.sql and
-- V2026.08.15__Normalize_appointment_status_column.sql, applied to every
-- remaining column still using a native PostgreSQL enum type. Hibernate binds
-- JPA @Enumerated(EnumType.STRING) fields as VARCHAR parameters, which fails
-- against native enum columns with "operator does not exist: <enum> = character varying"
-- on any WHERE/comparison against these columns.

ALTER TABLE IF EXISTS mental_health_academic_analysis
    ALTER COLUMN intervention_priority TYPE VARCHAR(50)
    USING intervention_priority::text;

ALTER TABLE IF EXISTS risk_assessments
    ALTER COLUMN risk_level TYPE VARCHAR(50)
    USING risk_level::text;

ALTER TABLE IF EXISTS users
    ALTER COLUMN role DROP DEFAULT;

ALTER TABLE IF EXISTS users
    ALTER COLUMN role TYPE VARCHAR(50)
    USING role::text;

ALTER TABLE IF EXISTS users
    ALTER COLUMN role SET DEFAULT 'CLIENT';
