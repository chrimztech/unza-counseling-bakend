-- Make uploaded_by nullable in resources table
-- This is needed because the controller now sets the uploadedBy from authentication
ALTER TABLE resources ALTER COLUMN uploaded_by DROP NOT NULL;