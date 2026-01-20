-- Add available_for_appointments column to users table
-- This column is needed for counselor availability management
ALTER TABLE users ADD COLUMN IF NOT EXISTS available_for_appointments BOOLEAN DEFAULT true;

-- Add index for better query performance on counselor availability
CREATE INDEX IF NOT EXISTS idx_users_available_for_appointments ON users(available_for_appointments);