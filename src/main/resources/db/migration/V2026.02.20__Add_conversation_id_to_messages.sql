-- Add conversation_id column to messages table
ALTER TABLE messages ADD COLUMN conversation_id BIGINT;

-- Create index for conversation_id for better query performance
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
