-- Check if consent_form table already exists before creating
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'consent_form') THEN
        CREATE TABLE consent_form (
            id BIGSERIAL PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            content TEXT,
            version VARCHAR(50),
            is_active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP,
            updated_at TIMESTAMP,
            created_by_id BIGINT
        );
    END IF;
END $$;

-- Check if user_consent table already exists before creating
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_consent') THEN
        CREATE TABLE user_consent (
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL,
            consent_form_id BIGINT NOT NULL,
            signed_at TIMESTAMP,
            ip_address VARCHAR(255),
            user_agent VARCHAR(255),
            is_active BOOLEAN DEFAULT TRUE
        );
    END IF;
END $$;

-- Check if foreign key constraints exist before adding them
-- Also check if the referenced tables exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'consent_form') AND 
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_consent_form_creator') THEN
        ALTER TABLE consent_form
            ADD CONSTRAINT fk_consent_form_creator
            FOREIGN KEY (created_by_id)
            REFERENCES users (id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_consent') AND 
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_user_consent_user') THEN
        ALTER TABLE user_consent
            ADD CONSTRAINT fk_user_consent_user
            FOREIGN KEY (user_id)
            REFERENCES users (id);
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_consent') AND 
       EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'consent_form') AND 
       NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_user_consent_form') THEN
        ALTER TABLE user_consent
            ADD CONSTRAINT fk_user_consent_form
            FOREIGN KEY (consent_form_id)
            REFERENCES consent_form (id);
    END IF;
END $$;