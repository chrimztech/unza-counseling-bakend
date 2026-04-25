-- Update audit_log table structure to match AuditLog entity
-- Changes:
-- - id: BIGSERIAL -> UUID (with GenerationType.UUID)
-- - user_id: BIGINT FK -> VARCHAR (just stores user id as string)
-- - entity_id: BIGINT -> VARCHAR
-- - old_values JSONB -> details VARCHAR(1000)
-- - new_values JSONB -> metadata JSONB
-- - ip_address: INET -> VARCHAR
-- - user_agent: TEXT -> removed
-- - timestamp: TIMESTAMP -> created_at TIMESTAMP
-- - Add: severity VARCHAR, success BOOLEAN

-- Drop foreign key constraint
ALTER TABLE audit_log DROP CONSTRAINT IF EXISTS audit_log_user_id_fkey;

-- Add new columns
ALTER TABLE audit_log ADD COLUMN id_new UUID DEFAULT gen_random_uuid();
ALTER TABLE audit_log ADD COLUMN entity_id_new VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN user_id_new VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN details VARCHAR(1000);
ALTER TABLE audit_log ADD COLUMN ip_address_new VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN severity VARCHAR(100);
ALTER TABLE audit_log ADD COLUMN success BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE audit_log ADD COLUMN metadata JSONB;
ALTER TABLE audit_log ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Migrate data
UPDATE audit_log SET 
    id_new = gen_random_uuid(),
    entity_id_new = CAST(entity_id AS VARCHAR),
    user_id_new = CAST(user_id AS VARCHAR),
    details = old_values::VARCHAR,
    ip_address_new = CAST(ip_address AS VARCHAR),
    severity = 'INFO',
    success = true,
    metadata = new_values,
    created_at = COALESCE(timestamp, CURRENT_TIMESTAMP);

-- Drop old columns
ALTER TABLE audit_log DROP COLUMN user_id;
ALTER TABLE audit_log DROP COLUMN entity_id;
ALTER TABLE audit_log DROP COLUMN old_values;
ALTER TABLE audit_log DROP COLUMN new_values;
ALTER TABLE audit_log DROP COLUMN ip_address;
ALTER TABLE audit_log DROP COLUMN user_agent;
ALTER TABLE audit_log DROP COLUMN timestamp;

-- Drop old id and PK, rename new id
ALTER TABLE audit_log DROP COLUMN id;
ALTER TABLE audit_log RENAME COLUMN id_new TO id;
ALTER TABLE audit_log ADD PRIMARY KEY (id);

-- Rename other columns
ALTER TABLE audit_log RENAME COLUMN entity_id_new TO entity_id;
ALTER TABLE audit_log RENAME COLUMN user_id_new TO user_id;
ALTER TABLE audit_log RENAME COLUMN ip_address_new TO ip_address;

-- Create new indexes (dropping existing ones first)
DROP INDEX IF EXISTS idx_audit_log_user_id;
DROP INDEX IF EXISTS idx_audit_log_action;
DROP INDEX IF EXISTS idx_audit_log_entity;
DROP INDEX IF EXISTS idx_audit_log_timestamp;

CREATE INDEX idx_audit_log_user_id_new ON audit_log(user_id);
CREATE INDEX idx_audit_log_action_new ON audit_log(action);
CREATE INDEX idx_audit_log_entity_new ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created_at_new ON audit_log(created_at);