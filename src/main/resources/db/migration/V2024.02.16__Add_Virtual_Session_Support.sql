-- Add session_mode and meeting_link columns to appointments table
-- Migration for Virtual Session Support

-- Add session_mode column with default value
ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS session_mode VARCHAR(20) DEFAULT 'IN_PERSON';

-- Add meeting_link column
ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS meeting_link VARCHAR(500);

-- Add meeting_provider column
ALTER TABLE appointments 
ADD COLUMN IF NOT EXISTS meeting_provider VARCHAR(50);

-- Update existing appointments to have a default session mode
UPDATE appointments 
SET session_mode = 'IN_PERSON' 
WHERE session_mode IS NULL;

-- Add index for faster queries on session_mode
CREATE INDEX IF NOT EXISTS idx_appointments_session_mode ON appointments(session_mode);

-- Add meeting_link column to sessions table
ALTER TABLE sessions 
ADD COLUMN IF NOT EXISTS meeting_link VARCHAR(500);
