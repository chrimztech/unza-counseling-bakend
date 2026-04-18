-- Align session status persistence with JPA EnumType.STRING usage.
-- The legacy PostgreSQL enum type causes insert/update failures when Hibernate
-- binds Java enums as VARCHAR values.

ALTER TABLE sessions
    ALTER COLUMN status DROP DEFAULT;

ALTER TABLE sessions
    ALTER COLUMN status TYPE VARCHAR(50)
    USING status::text;

ALTER TABLE sessions
    ALTER COLUMN status SET DEFAULT 'SCHEDULED';
