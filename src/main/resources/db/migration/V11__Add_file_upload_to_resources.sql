-- Add file upload columns to resources table
-- This migration adds support for file uploads to mental health resources

-- Add new columns for file upload support
ALTER TABLE resources ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
ALTER TABLE resources ADD COLUMN IF NOT EXISTS file_type VARCHAR(100);
ALTER TABLE resources ADD COLUMN IF NOT EXISTS file_url VARCHAR(500);
ALTER TABLE resources ADD COLUMN IF NOT EXISTS file_key VARCHAR(255);

-- Add a type column to distinguish between different resource types (BOOK, MAGAZINE, VIDEO, AUDIO, DOCUMENT, etc.)
ALTER TABLE resources ADD COLUMN IF NOT EXISTS type VARCHAR(50);

-- Add featured flag for highlighting important resources
ALTER TABLE resources ADD COLUMN IF NOT EXISTS featured BOOLEAN DEFAULT false;

-- Add URL column for external resources
ALTER TABLE resources ADD COLUMN IF NOT EXISTS url VARCHAR(500);

-- Create index for file_key to speed up lookups
CREATE INDEX IF NOT EXISTS idx_resources_file_key ON resources(file_key);

-- Create index for type column
CREATE INDEX IF NOT EXISTS idx_resources_type ON resources(type);

-- Create index for featured column
CREATE INDEX IF NOT EXISTS idx_resources_featured ON resources(featured);
