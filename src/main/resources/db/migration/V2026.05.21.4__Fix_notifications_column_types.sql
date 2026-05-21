-- The Notification entity stores type and priority as plain strings,
-- but the columns were created as PostgreSQL enum types which reject VARCHAR inserts.
-- Convert both to VARCHAR so all notification inserts succeed.

ALTER TABLE notifications
    ALTER COLUMN type     TYPE VARCHAR(50)   USING type::text,
    ALTER COLUMN priority TYPE VARCHAR(20)   USING priority::text;

-- Restore the default for priority as plain text
ALTER TABLE notifications ALTER COLUMN priority SET DEFAULT 'MEDIUM';
