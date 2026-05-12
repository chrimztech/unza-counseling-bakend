-- Convert intake_data_json from PostgreSQL OID (large-object) to plain TEXT.
-- The @Lob annotation on the Java field caused Hibernate to create this column
-- as OID, which requires an open transaction to read the LOB stream.
-- Removing @Lob fixes future rows; this migration fixes the column type for
-- any existing data.

DO $$
BEGIN
    -- Only run if the column exists and is of OID type
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name   = 'appointments'
          AND column_name  = 'intake_data_json'
          AND data_type    = 'oid'
    ) THEN
        -- Promote OID content to text, then drop the old OID column
        ALTER TABLE appointments
            ADD COLUMN IF NOT EXISTS intake_data_json_text TEXT;

        -- Copy readable text from any stored large objects
        -- (rows that have NULL or empty OIDs are simply left NULL)
        UPDATE appointments
        SET intake_data_json_text = convert_from(lo_get(intake_data_json::oid), 'UTF8')
        WHERE intake_data_json IS NOT NULL;

        ALTER TABLE appointments DROP COLUMN intake_data_json;
        ALTER TABLE appointments RENAME COLUMN intake_data_json_text TO intake_data_json;

    -- If the column doesn't exist yet, add it as TEXT
    ELSIF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name  = 'appointments'
          AND column_name = 'intake_data_json'
    ) THEN
        ALTER TABLE appointments ADD COLUMN intake_data_json TEXT;
    END IF;
    -- If it already exists as TEXT/VARCHAR nothing needs to change
END;
$$;
