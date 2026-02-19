-- Make client_id and counselor_id columns nullable in appointments table
-- This allows appointments to be created without a Client record
-- The student_id field will be used to link appointments to users

-- First, drop the existing foreign key constraints
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointments_client_id;
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS fk_appointments_counselor_id;
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS appointments_counselor_id_fkey;

-- Make the client_id column nullable
ALTER TABLE appointments ALTER COLUMN client_id DROP NOT NULL;

-- Make the counselor_id column nullable (for unassigned appointments)
ALTER TABLE appointments ALTER COLUMN counselor_id DROP NOT NULL;

-- Re-add the foreign key constraints (now with nullable columns)
ALTER TABLE appointments
ADD CONSTRAINT fk_appointments_client_id
FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE SET NULL;

ALTER TABLE appointments
ADD CONSTRAINT fk_appointments_counselor_id
FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL;

-- Update any existing appointments that have invalid client_id references
-- Set them to NULL if the client doesn't exist
UPDATE appointments SET client_id = NULL 
WHERE client_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM clients WHERE clients.id = appointments.client_id);

-- Update any existing appointments that have invalid counselor_id references
-- Set them to NULL if the counselor doesn't exist
UPDATE appointments SET counselor_id = NULL 
WHERE counselor_id IS NOT NULL 
AND NOT EXISTS (SELECT 1 FROM users WHERE users.id = appointments.counselor_id);
