-- The runtime session entity now uses student_id as the primary user link.
-- Older databases still keep a required client_id column from the legacy schema,
-- which breaks new inserts whenever no legacy Client row exists.

ALTER TABLE IF EXISTS sessions
    ALTER COLUMN client_id DROP NOT NULL;

-- Preserve client linkage for existing rows wherever the appointment already has it.
UPDATE sessions AS s
SET client_id = a.client_id
FROM appointments AS a
WHERE s.appointment_id = a.id
  AND s.client_id IS NULL
  AND a.client_id IS NOT NULL;

-- Backfill from student_id when the legacy clients row shares the same identifier.
UPDATE sessions AS s
SET client_id = s.student_id
WHERE s.client_id IS NULL
  AND s.student_id IS NOT NULL
  AND EXISTS (
      SELECT 1
      FROM clients AS c
      WHERE c.id = s.student_id
  );
