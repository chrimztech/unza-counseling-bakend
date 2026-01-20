-- Add missing columns to users table to match User entity
-- This migration adds all the columns that are defined in the User entity but missing from the database

-- Basic user fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_picture VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS program VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS year_of_study INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_password_token VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS reset_password_expiry TIMESTAMP;

-- Counselor-specific fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS license_number VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS specialization VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS qualifications TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS years_of_experience INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS available_for_appointments BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS has_signed_consent BOOLEAN DEFAULT false;

-- Admin-specific fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS admin_level VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department_managed VARCHAR(100);

-- Client-specific fields
ALTER TABLE users ADD COLUMN IF NOT EXISTS client_status VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS consent_to_treatment BOOLEAN DEFAULT false;
ALTER TABLE users ADD COLUMN IF NOT EXISTS counseling_history TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(200);
ALTER TABLE users ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS emergency_contact_relationship VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS faculty VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS gpa DECIMAL(4,2);
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_session_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS medical_history TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS programme VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS referral_source VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS registration_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS risk_level VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS risk_score INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS total_sessions INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS available BOOLEAN DEFAULT true;
ALTER TABLE users ADD COLUMN IF NOT EXISTS office_location VARCHAR(200);

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_available_for_appointments ON users(available_for_appointments);
CREATE INDEX IF NOT EXISTS idx_users_specialization ON users(specialization);
CREATE INDEX IF NOT EXISTS idx_users_department ON users(department);
CREATE INDEX IF NOT EXISTS idx_users_program ON users(program);
CREATE INDEX IF NOT EXISTS idx_users_faculty ON users(faculty);
CREATE INDEX IF NOT EXISTS idx_users_admin_level ON users(admin_level);