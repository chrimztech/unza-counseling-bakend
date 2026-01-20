-- UNZA Counseling Management System - Initial Database Schema
-- Version: 1.0.0
-- Created: 2025-12-20

-- Create ENUM types
DROP TYPE IF EXISTS user_role CASCADE;
CREATE TYPE user_role AS ENUM ('ADMIN', 'COUNSELOR', 'CLIENT', 'SUPER_ADMIN');
DROP TYPE IF EXISTS appointment_status CASCADE;
CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW');
DROP TYPE IF EXISTS session_status CASCADE;
CREATE TYPE session_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
DROP TYPE IF EXISTS risk_level CASCADE;
CREATE TYPE risk_level AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
DROP TYPE IF EXISTS notification_type CASCADE;
CREATE TYPE notification_type AS ENUM ('APPOINTMENT', 'RISK_ASSESSMENT', 'SYSTEM', 'REMINDER');
DROP TYPE IF EXISTS notification_priority CASCADE;
CREATE TYPE notification_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');
DROP TYPE IF EXISTS intervention_urgency CASCADE;
CREATE TYPE intervention_urgency AS ENUM ('IMMEDIATE', 'WITHIN_WEEK', 'WITHIN_MONTH', 'ROUTINE');

-- Drop existing tables if they exist (for development/reset scenarios)
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS session_notes CASCADE;
DROP TABLE IF EXISTS admins CASCADE;
DROP TABLE IF EXISTS resources CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS mental_health_academic_analysis CASCADE;
DROP TABLE IF EXISTS academic_performance CASCADE;
DROP TABLE IF EXISTS risk_assessments CASCADE;
DROP TABLE IF EXISTS self_assessments CASCADE;
DROP TABLE IF EXISTS sessions CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS counselors CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    role user_role NOT NULL DEFAULT 'CLIENT',
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    -- Additional fields from User entity
    username VARCHAR(50) UNIQUE,
    student_id VARCHAR(50) UNIQUE,
    bio TEXT,
    profile_picture VARCHAR(500),
    department VARCHAR(100),
    program VARCHAR(100),
    year_of_study INTEGER,
    reset_password_token VARCHAR(255),
    reset_password_expiry TIMESTAMP,
    authentication_source VARCHAR(50) DEFAULT 'INTERNAL',
    license_number VARCHAR(100),
    specialization VARCHAR(255),
    qualifications TEXT,
    years_of_experience INTEGER,
    available_for_appointments BOOLEAN DEFAULT true,
    has_signed_consent BOOLEAN DEFAULT false,
    -- Client-specific fields
    client_status VARCHAR(50),
    consent_to_treatment BOOLEAN DEFAULT false,
    counseling_history TEXT,
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relationship VARCHAR(100),
    faculty VARCHAR(100),
    gpa DECIMAL(4,2),
    last_session_date DATE,
    medical_history TEXT,
    notes TEXT,
    programme VARCHAR(100),
    referral_source VARCHAR(100),
    registration_date DATE,
    risk_level VARCHAR(20),
    risk_score INTEGER,
    total_sessions INTEGER,
    available BOOLEAN DEFAULT true,
    office_location VARCHAR(200)
);

-- Create indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);

-- Roles table (for role-based permissions)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    permissions JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Counselors table
CREATE TABLE counselors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    employee_id VARCHAR(50) UNIQUE NOT NULL,
    specialization TEXT[],
    license_number VARCHAR(100),
    years_of_experience INTEGER,
    bio TEXT,
    education TEXT,
    certifications TEXT[],
    available_hours JSONB,
    max_clients_per_day INTEGER DEFAULT 8,
    hourly_rate DECIMAL(10,2),
    is_available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for counselors table
CREATE INDEX idx_counselors_user_id ON counselors(user_id);
CREATE INDEX idx_counselors_employee_id ON counselors(employee_id);
CREATE INDEX idx_counselors_available ON counselors(is_available);

-- Clients table
CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    program VARCHAR(100),
    year_of_study INTEGER,
    faculty VARCHAR(100),
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    medical_conditions TEXT,
    medications TEXT,
    preferred_counselor_id BIGINT REFERENCES counselors(id),
    intake_completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for clients table
CREATE INDEX idx_clients_user_id ON clients(user_id);
CREATE INDEX idx_clients_student_id ON clients(student_id);
CREATE INDEX idx_clients_faculty ON clients(faculty);
CREATE INDEX idx_clients_preferred_counselor ON clients(preferred_counselor_id);

-- Appointments table
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    counselor_id BIGINT NOT NULL REFERENCES counselors(id) ON DELETE CASCADE,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status appointment_status NOT NULL DEFAULT 'SCHEDULED',
    session_type VARCHAR(50) DEFAULT 'INDIVIDUAL',
    notes TEXT,
    cancellation_reason TEXT,
    rescheduled_from_id BIGINT REFERENCES appointments(id),
    reminder_sent BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id)
);

-- Create indexes for appointments table
CREATE INDEX idx_appointments_client_id ON appointments(client_id);
CREATE INDEX idx_appointments_counselor_id ON appointments(counselor_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_status ON appointments(status);
CREATE INDEX idx_appointments_date_time ON appointments(appointment_date, start_time);

-- Sessions table
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(id) ON DELETE CASCADE,
    counselor_id BIGINT NOT NULL REFERENCES counselors(id) ON DELETE CASCADE,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    session_date TIMESTAMP NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME,
    duration_minutes INTEGER,
    status session_status NOT NULL DEFAULT 'SCHEDULED',
    session_type VARCHAR(50) DEFAULT 'INDIVIDUAL',
    session_summary TEXT,
    goals_achieved TEXT,
    next_steps TEXT,
    homework_assigned TEXT,
    client_progress_rating INTEGER CHECK (client_progress_rating >= 1 AND client_progress_rating <= 10),
    counselor_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for sessions table
CREATE INDEX idx_sessions_appointment_id ON sessions(appointment_id);
CREATE INDEX idx_sessions_counselor_id ON sessions(counselor_id);
CREATE INDEX idx_sessions_client_id ON sessions(client_id);
CREATE INDEX idx_sessions_date ON sessions(session_date);
CREATE INDEX idx_sessions_status ON sessions(status);

-- Self-assessments table
CREATE TABLE self_assessments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    assessment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    anxiety_level INTEGER CHECK (anxiety_level >= 1 AND anxiety_level <= 10),
    depression_level INTEGER CHECK (depression_level >= 1 AND depression_level <= 10),
    stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),
    sleep_quality INTEGER CHECK (sleep_quality >= 1 AND sleep_quality <= 10),
    academic_pressure INTEGER CHECK (academic_pressure >= 1 AND academic_pressure <= 10),
    social_relationships INTEGER CHECK (social_relationships >= 1 AND social_relationships <= 10),
    overall_wellbeing INTEGER CHECK (overall_wellbeing >= 1 AND overall_wellbeing <= 10),
    concerns TEXT,
    goals TEXT,
    is_anonymous BOOLEAN NOT NULL DEFAULT false,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for self_assessments table
CREATE INDEX idx_self_assessments_client_id ON self_assessments(client_id);
CREATE INDEX idx_self_assessments_date ON self_assessments(assessment_date);

-- Risk assessments table
CREATE TABLE risk_assessments (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    counselor_id BIGINT NOT NULL REFERENCES counselors(id) ON DELETE CASCADE,
    self_assessment_id BIGINT REFERENCES self_assessments(id),
    assessment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    risk_level risk_level NOT NULL,
    risk_factors JSONB,
    protective_factors JSONB,
    suicide_risk BOOLEAN NOT NULL DEFAULT false,
    self_harm_risk BOOLEAN NOT NULL DEFAULT false,
    substance_abuse_risk BOOLEAN NOT NULL DEFAULT false,
    academic_dropout_risk BOOLEAN NOT NULL DEFAULT false,
    risk_score DECIMAL(5,2),
    assessment_tool VARCHAR(100),
    clinical_notes TEXT,
    recommendations TEXT,
    follow_up_required BOOLEAN NOT NULL DEFAULT false,
    follow_up_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for risk_assessments table
CREATE INDEX idx_risk_assessments_client_id ON risk_assessments(client_id);
CREATE INDEX idx_risk_assessments_counselor_id ON risk_assessments(counselor_id);
CREATE INDEX idx_risk_assessments_date ON risk_assessments(assessment_date);
CREATE INDEX idx_risk_assessments_level ON risk_assessments(risk_level);
CREATE INDEX idx_risk_assessments_follow_up ON risk_assessments(follow_up_required);

-- Academic performance table
CREATE TABLE academic_performance (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    academic_year VARCHAR(20) NOT NULL,
    semester INTEGER NOT NULL CHECK (semester >= 1 AND semester <= 2),
    gpa DECIMAL(4,2),
    total_credits INTEGER,
    credits_earned INTEGER,
    courses JSONB,
    attendance_percentage DECIMAL(5,2),
    study_hours_per_week INTEGER,
    academic_goals TEXT,
    challenges_faced TEXT,
    support_needed TEXT,
    notes TEXT,
    recorded_by BIGINT REFERENCES counselors(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for academic_performance table
CREATE INDEX idx_academic_performance_client_id ON academic_performance(client_id);
CREATE INDEX idx_academic_performance_year_semester ON academic_performance(academic_year, semester);

-- Mental health academic analysis table
CREATE TABLE mental_health_academic_analysis (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    self_assessment_id BIGINT REFERENCES self_assessments(id),
    risk_assessment_id BIGINT REFERENCES risk_assessments(id),
    academic_performance_id BIGINT REFERENCES academic_performance(id),
    analysis_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    correlation_score DECIMAL(5,2),
    mental_health_score DECIMAL(5,2),
    academic_impact_score DECIMAL(5,2),
    risk_factors JSONB,
    recommendations JSONB,
    intervention_priority intervention_urgency,
    analysis_period_start DATE,
    analysis_period_end DATE,
    ai_generated BOOLEAN NOT NULL DEFAULT false,
    created_by BIGINT REFERENCES counselors(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for mental_health_academic_analysis table
CREATE INDEX idx_mental_health_academic_analysis_client_id ON mental_health_academic_analysis(client_id);
CREATE INDEX idx_mental_health_academic_analysis_date ON mental_health_academic_analysis(analysis_date);

-- Messages table
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    message_type VARCHAR(50) DEFAULT 'GENERAL',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    is_read BOOLEAN NOT NULL DEFAULT false,
    parent_message_id BIGINT REFERENCES messages(id),
    attachments JSONB,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for messages table
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_recipient_id ON messages(recipient_id);
CREATE INDEX idx_messages_read ON messages(is_read);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type notification_type NOT NULL,
    priority notification_priority NOT NULL DEFAULT 'MEDIUM',
    action_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for notifications table
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

-- Resources table
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    content_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500),
    file_size BIGINT,
    category VARCHAR(100),
    tags TEXT[],
    is_public BOOLEAN NOT NULL DEFAULT false,
    download_count INTEGER NOT NULL DEFAULT 0,
    uploaded_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for resources table
CREATE INDEX idx_resources_category ON resources(category);
CREATE INDEX idx_resources_public ON resources(is_public);
CREATE INDEX idx_resources_uploaded_by ON resources(uploaded_by);

-- Session notes table
CREATE TABLE session_notes (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
    counselor_id BIGINT NOT NULL REFERENCES counselors(id) ON DELETE CASCADE,
    note_type VARCHAR(50) DEFAULT 'PROGRESS',
    content TEXT NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for session_notes table
CREATE INDEX idx_session_notes_session_id ON session_notes(session_id);
CREATE INDEX idx_session_notes_counselor_id ON session_notes(counselor_id);

-- Audit log table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit_log table
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);

-- Admin users table
CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    admin_level VARCHAR(50) DEFAULT 'SYSTEM_ADMIN',
    permissions JSONB,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for admins table
CREATE INDEX idx_admins_user_id ON admins(user_id);
CREATE INDEX idx_admins_level ON admins(admin_level);

-- Create role_permissions table for Role entity @ElementCollection
CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission VARCHAR(100) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- Create indexes for role_permissions table
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);

-- NOTE: Triggers for updated_at columns have been temporarily removed due to Flyway dollar quote parsing issues.
-- These will be added in a separate migration file once the syntax issue is resolved.