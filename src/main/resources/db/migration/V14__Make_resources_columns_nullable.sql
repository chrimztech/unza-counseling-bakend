-- Make several columns nullable in resources table to prevent validation errors
ALTER TABLE resources ALTER COLUMN title DROP NOT NULL;
ALTER TABLE resources ALTER COLUMN content_type DROP NOT NULL;
ALTER TABLE resources ALTER COLUMN is_public DROP NOT NULL;
ALTER TABLE resources ALTER COLUMN download_count DROP NOT NULL;