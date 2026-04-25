-- Fix sessions.counselor_id foreign key to reference users(id) instead of counselors(id)
-- The Session entity maps counselor as a User (not Counselor profile), so FK must point to users.id

-- Drop existing constraint
ALTER TABLE sessions DROP CONSTRAINT IF EXISTS sessions_counselor_id_fkey;

-- Add correct constraint referencing users(id)
ALTER TABLE sessions ADD CONSTRAINT sessions_counselor_id_fkey 
    FOREIGN KEY (counselor_id) REFERENCES users(id) ON DELETE SET NULL;
