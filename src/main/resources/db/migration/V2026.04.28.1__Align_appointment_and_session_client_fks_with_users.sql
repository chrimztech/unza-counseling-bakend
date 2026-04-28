-- Runtime entities model Client as a discriminator-based subclass stored in users.
-- Older schemas still point appointments.client_id and sessions.client_id at legacy
-- clients(id), which causes inserts/reads to drift away from the active model.

-- First, translate only clearly legacy client references into the matching users.id.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'clients'
          AND column_name = 'user_id'
    ) THEN
        UPDATE appointments AS a
        SET client_id = c.user_id
        FROM clients AS c
        WHERE a.client_id = c.id
          AND c.user_id IS NOT NULL
          AND NOT EXISTS (
              SELECT 1
              FROM users AS u
              WHERE u.id = a.client_id
          );

        UPDATE sessions AS s
        SET client_id = c.user_id
        FROM clients AS c
        WHERE s.client_id = c.id
          AND c.user_id IS NOT NULL
          AND NOT EXISTS (
              SELECT 1
              FROM users AS u
              WHERE u.id = s.client_id
          );
    END IF;
END $$;

-- Drop legacy client foreign keys if they exist.
ALTER TABLE IF EXISTS appointments DROP CONSTRAINT IF EXISTS fk_appointments_client_id;
ALTER TABLE IF EXISTS appointments DROP CONSTRAINT IF EXISTS appointments_client_id_fkey;
ALTER TABLE IF EXISTS sessions DROP CONSTRAINT IF EXISTS sessions_client_id_fkey;
ALTER TABLE IF EXISTS sessions DROP CONSTRAINT IF EXISTS fk_sessions_client_id;

-- Null out any remaining invalid references before attaching the new constraints.
UPDATE appointments AS a
SET client_id = NULL
WHERE a.client_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM users AS u
      WHERE u.id = a.client_id
  );

UPDATE sessions AS s
SET client_id = NULL
WHERE s.client_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM users AS u
      WHERE u.id = s.client_id
  );

ALTER TABLE IF EXISTS appointments
    ADD CONSTRAINT fk_appointments_client_id
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS sessions
    ADD CONSTRAINT sessions_client_id_fkey
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE SET NULL;
