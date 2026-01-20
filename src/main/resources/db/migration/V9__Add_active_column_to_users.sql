-- V9__Add_active_column_to_users.sql
-- Add active column to users table

ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT true;
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);
COMMENT ON COLUMN users.active IS 'Whether the user account is active (true) or deactivated (false)';
