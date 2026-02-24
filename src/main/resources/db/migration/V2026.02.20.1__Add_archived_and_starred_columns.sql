-- Add is_archived and is_starred columns to messages table
ALTER TABLE messages ADD COLUMN is_archived BOOLEAN DEFAULT FALSE;
ALTER TABLE messages ADD COLUMN is_starred BOOLEAN DEFAULT FALSE;
