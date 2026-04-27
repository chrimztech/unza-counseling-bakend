-- Preserve message rows for audit visibility even when end users delete them.
ALTER TABLE IF EXISTS messages ADD COLUMN deleted_by_sender BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS messages ADD COLUMN deleted_by_recipient BOOLEAN DEFAULT FALSE;
ALTER TABLE IF EXISTS messages ADD COLUMN deleted_at TIMESTAMP NULL;
