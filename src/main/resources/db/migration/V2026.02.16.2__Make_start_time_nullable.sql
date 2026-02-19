-- Make start_time and end_time nullable in appointments table
-- The entity uses appointmentDate (TIMESTAMP) for the full datetime
-- The start_time and end_time columns are legacy and can be null

-- Make start_time nullable
ALTER TABLE appointments ALTER COLUMN start_time DROP NOT NULL;

-- Make end_time nullable (in case it's not already)
ALTER TABLE appointments ALTER COLUMN end_time DROP NOT NULL;
