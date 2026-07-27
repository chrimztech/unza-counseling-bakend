-- Stores the non-expiring bearer token returned by the SIS student login endpoint
-- (devoap.unza.zm/api/v1/students/login) so it can be reused for subsequent SIS calls
-- without re-authenticating.
ALTER TABLE users ADD COLUMN IF NOT EXISTS sis_token TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sis_token_type VARCHAR(20);
