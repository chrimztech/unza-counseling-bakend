-- Create user_roles table if it doesn't exist
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create role_permissions table if it doesn't exist
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission VARCHAR(255),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ROLE_STUDENT', 'Student role for external users'),
    ('ROLE_COUNSELOR', 'Counselor role for professional counselors'),
    ('ROLE_ADMIN', 'Administrator role for system administrators'),
    ('ROLE_SUPER_ADMIN', 'Super administrator role with full access');

-- Insert default consent form
INSERT INTO consent_forms (title, content, is_active, created_at, updated_at) VALUES
    ('Counseling Services Consent Form', 'By signing this form, you agree to participate in counseling services provided by the University of Zambia Counseling Center. You understand that all information shared during counseling sessions will be kept confidential except in cases required by law. You have the right to withdraw from counseling at any time.', true, NOW(), NOW());