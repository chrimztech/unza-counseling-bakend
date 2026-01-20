-- Add email_verified column to users table to match User entity
-- This migration adds the email_verified column that is defined in the User entity but missing from the database

ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT false;

-- Add index for better performance on email_verified queries
CREATE INDEX IF NOT EXISTS idx_users_email_verified ON users(email_verified);