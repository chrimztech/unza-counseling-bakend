-- Fix appointments table schema to match JPA entity
-- Issues:
-- 1. student_id column is VARCHAR but should be BIGINT (FK to users.id)
-- 2. counselor_id references counselors(id) but should reference users(id)
-- 3. Missing columns: title, type, duration, description, session_mode, meeting_link, meeting_provider, location, cancellation_reason

-- ============================================================================
-- STEP 1: Drop existing foreign key constraints
-- ============================================================================
DO $$
DECLARE
    fk_record RECORD;
BEGIN
    -- Find and drop all foreign key constraints on appointments table
    FOR fk_record IN 
        SELECT constraint_name
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'appointments' 
            AND tc.constraint_type = 'FOREIGN KEY'
    LOOP
        EXECUTE format('ALTER TABLE appointments DROP CONSTRAINT IF EXISTS %I', fk_record.constraint_name);
    END LOOP;
END $$;

-- ============================================================================
-- STEP 2: Add missing columns if they don't exist
-- ============================================================================

-- Add title column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS title VARCHAR(200);

-- Add type column (appointment type enum)
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS type VARCHAR(50) DEFAULT 'INITIAL_CONSULTATION';

-- Add duration column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS duration INTEGER DEFAULT 60;

-- Add description column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS description VARCHAR(1000);

-- Add session_mode column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS session_mode VARCHAR(20) DEFAULT 'IN_PERSON';

-- Add meeting_link column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS meeting_link VARCHAR(500);

-- Add meeting_provider column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS meeting_provider VARCHAR(50);

-- Add location column
ALTER TABLE appointments ADD COLUMN IF NOT EXISTS location VARCHAR(255);

-- Add cancellation_reason column (if not exists as TEXT, add as VARCHAR)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'appointments' AND column_name = 'cancellation_reason'
    ) THEN
        ALTER TABLE appointments ADD COLUMN cancellation_reason VARCHAR(2000);
    END IF;
END $$;

-- ============================================================================
-- STEP 3: Fix student_id column type
-- ============================================================================
DO $$
BEGIN
    -- Check if student_id column exists in appointments
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'appointments' AND column_name = 'student_id'
    ) THEN
        -- Column exists, check its type
        -- If it's VARCHAR, we need to migrate the data
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'appointments' 
                AND column_name = 'student_id' 
                AND data_type = 'character varying'
        ) THEN
            -- Create temporary column with correct type
            ALTER TABLE appointments ADD COLUMN student_id_new BIGINT;
            
            -- Migrate data (convert VARCHAR to BIGINT)
            -- Only update where student_id is not null and is a valid number
            UPDATE appointments 
            SET student_id_new = CAST(student_id AS BIGINT)
            WHERE student_id IS NOT NULL 
                AND student_id ~ '^[0-9]+$';
            
            -- Drop old column
            ALTER TABLE appointments DROP COLUMN student_id;
            
            -- Rename new column
            ALTER TABLE appointments RENAME COLUMN student_id_new TO student_id;
        END IF;
    ELSE
        -- Column doesn't exist, add it
        ALTER TABLE appointments ADD COLUMN student_id BIGINT;
    END IF;
END $$;

-- ============================================================================
-- STEP 4: Fix counselor_id to reference users instead of counselors
-- ============================================================================
-- The counselor_id column should reference users.id, not counselors.id
-- This is already BIGINT, so we just need to update the FK constraint

-- ============================================================================
-- STEP 5: Update appointment_date to TIMESTAMP if it's DATE
-- ============================================================================
DO $$
BEGIN
    -- Check if appointment_date is DATE type and convert to TIMESTAMP
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'appointments' 
            AND column_name = 'appointment_date' 
            AND data_type = 'date'
    ) THEN
        -- Create temporary timestamp column
        ALTER TABLE appointments ADD COLUMN appointment_date_new TIMESTAMP;
        
        -- Migrate data
        UPDATE appointments 
        SET appointment_date_new = CAST(appointment_date AS TIMESTAMP)
        WHERE appointment_date IS NOT NULL;
        
        -- Drop old column
        ALTER TABLE appointments DROP COLUMN appointment_date;
        
        -- Rename new column
        ALTER TABLE appointments RENAME COLUMN appointment_date_new TO appointment_date;
    END IF;
END $$;

-- ============================================================================
-- STEP 6: Add foreign key constraints with correct references
-- ============================================================================

-- Add foreign key constraint for student_id -> users.id
ALTER TABLE appointments 
ADD CONSTRAINT fk_appointments_student_id 
FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add foreign key constraint for counselor_id -> users.id
ALTER TABLE appointments 
ADD CONSTRAINT fk_appointments_counselor_id 
FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL;

-- Add foreign key constraint for client_id -> clients.id (keep existing)
ALTER TABLE appointments 
ADD CONSTRAINT fk_appointments_client_id 
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE;

-- ============================================================================
-- STEP 7: Create indexes for better query performance
-- ============================================================================
CREATE INDEX IF NOT EXISTS idx_appointments_student_id ON appointments(student_id);
CREATE INDEX IF NOT EXISTS idx_appointments_counselor_id ON appointments(counselor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_client_id ON appointments(client_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date);
CREATE INDEX IF NOT EXISTS idx_appointments_status ON appointments(status);

-- ============================================================================
-- STEP 8: Update existing appointments to have title if missing
-- ============================================================================
UPDATE appointments 
SET title = 'Counseling Appointment'
WHERE title IS NULL OR title = '';

-- Update existing appointments to have type if missing
UPDATE appointments 
SET type = 'INITIAL_CONSULTATION'
WHERE type IS NULL;

-- Update existing appointments to have duration if missing
UPDATE appointments 
SET duration = 60
WHERE duration IS NULL;
