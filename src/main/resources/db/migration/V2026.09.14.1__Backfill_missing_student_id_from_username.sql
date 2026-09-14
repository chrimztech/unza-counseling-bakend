-- Some student accounts created via SIS login never had student_id populated (the SIS
-- response didn't expose it under any of the field names the parser tried), leaving the
-- "Student ID" field blank on their profile even though their username is the same
-- student number they log in with. Backfill from username for exactly that case.
UPDATE users
SET student_id = username
WHERE (student_id IS NULL OR student_id = '')
  AND username IS NOT NULL
  AND username != ''
  AND id IN (
    SELECT ur.user_id
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE r.name = 'ROLE_STUDENT'
  );
