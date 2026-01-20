-- UNZA Counseling Management System - Comprehensive Seed Data
-- Version: 10.0.0
-- Created: 2026-01-11
-- This migration seeds all schemas with realistic data for frontend development

-- First, let's enable Flyway to run this migration
-- We need to create a new migration that works with the current JPA entity structure

-- Clear existing data to avoid conflicts only if tables exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_roles') THEN
        EXECUTE 'DELETE FROM user_roles';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'role_permissions') THEN
        EXECUTE 'DELETE FROM role_permissions';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_consent') THEN
        EXECUTE 'DELETE FROM user_consent';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'consent_form') THEN
        EXECUTE 'DELETE FROM consent_form';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'academic_performance') THEN
        EXECUTE 'DELETE FROM academic_performance';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'risk_assessment') THEN
        EXECUTE 'DELETE FROM risk_assessment';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'self_assessment') THEN
        EXECUTE 'DELETE FROM self_assessment';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'session_note') THEN
        EXECUTE 'DELETE FROM session_note';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'session') THEN
        EXECUTE 'DELETE FROM "session"';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'appointment') THEN
        EXECUTE 'DELETE FROM appointment';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'message') THEN
        EXECUTE 'DELETE FROM message';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'notification') THEN
        EXECUTE 'DELETE FROM notification';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'report') THEN
        EXECUTE 'DELETE FROM report';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'resource') THEN
        EXECUTE 'DELETE FROM resource';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_log') THEN
        EXECUTE 'DELETE FROM audit_log';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'settings') THEN
        EXECUTE 'DELETE FROM settings';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user') THEN
        EXECUTE 'DELETE FROM "user"';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'roles') THEN
        EXECUTE 'DELETE FROM roles';
    END IF;
END $$;

-- Reset sequences only if they exist
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'roles_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE roles_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'user_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE user_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'appointment_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE appointment_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'academic_performance_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE academic_performance_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'risk_assessment_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE risk_assessment_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'self_assessment_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE self_assessment_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'session_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE session_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'session_note_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE session_note_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'consent_form_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE consent_form_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'user_consent_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE user_consent_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'message_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE message_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'notification_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE notification_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'report_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE report_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'resource_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE resource_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'settings_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE settings_id_seq RESTART WITH 1';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'audit_log_id_seq') THEN
        EXECUTE 'ALTER SEQUENCE audit_log_id_seq RESTART WITH 1';
    END IF;
END $$;

-- Insert roles first
INSERT INTO roles (id, name, description) VALUES
    (1, 'ROLE_SUPER_ADMIN', 'Super administrator with full system access'),
    (2, 'ROLE_ADMIN', 'Administrator with system management capabilities'),
    (3, 'ROLE_COUNSELOR', 'Licensed counselor providing counseling services'),
    (4, 'ROLE_STUDENT', 'Student seeking counseling services'),
    (5, 'ROLE_CLIENT', 'General client role for counseling services');

-- Create users table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        EXECUTE 'CREATE TABLE users (
            id BIGSERIAL PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            email VARCHAR(100) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL,
            first_name VARCHAR(50),
            last_name VARCHAR(50),
            phone_number VARCHAR(20),
            bio TEXT,
            gender VARCHAR(20),
            date_of_birth DATE,
            department VARCHAR(100),
            program VARCHAR(100),
            year_of_study INTEGER,
            is_active BOOLEAN DEFAULT TRUE,
            email_verified BOOLEAN DEFAULT FALSE,
            last_login_at TIMESTAMP,
            reset_password_token VARCHAR(255),
            reset_password_expiry TIMESTAMP,
            authentication_source VARCHAR(50),
            license_number VARCHAR(50),
            specialization TEXT,
            qualifications TEXT,
            years_of_experience INTEGER,
            available_for_appointments BOOLEAN,
            has_signed_consent BOOLEAN DEFAULT FALSE,
            created_at TIMESTAMP,
            updated_at TIMESTAMP,
            user_type VARCHAR(20)
        )';
    END IF;
END $$;

-- Insert users with different roles only if they don't already exist
-- Admin users
INSERT INTO users (id, username, email, password, first_name, last_name, phone_number, bio, gender, date_of_birth, department, program, year_of_study, is_active, email_verified, last_login_at, reset_password_token, reset_password_expiry, authentication_source, license_number, specialization, qualifications, years_of_experience, available_for_appointments, has_signed_consent, created_at, updated_at, user_type)
SELECT
    -- Super Admin
    1, 'superadmin', 'superadmin@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'System', 'Administrator', '+260971234567', 'System administrator for UNZA Counseling System', 'MALE', '1980-01-01', 'IT', 'Computer Science', 5, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1)

UNION ALL
SELECT
    -- Admin
    2, 'admin', 'admin@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'John', 'Mwanza', '+260972345678', 'Counseling system administrator', 'MALE', '1985-03-15', 'Psychology', 'Counseling Psychology', 3, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 2)

UNION ALL
SELECT
    -- Counselors
    3, 'counselor1', 'grace.chiluba@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Grace', 'Chiluba', '+260973456789', 'Experienced counselor specializing in relationship and family therapy', 'FEMALE', '1982-07-22', 'Psychology', 'Clinical Psychology', 5, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', 'LIC2023002', 'Relationship Counseling, Family Therapy', 'M.A. Clinical Psychology, University of Lusaka', 5, true, true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 3)

UNION ALL
SELECT
    4, 'counselor2', 'michael.simukoko@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Michael', 'Simukoko', '+260974567890', 'Senior counselor with expertise in addiction and trauma therapy', 'MALE', '1978-11-05', 'Psychology', 'Counseling Psychology', 12, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', 'LIC2023003', 'Addiction Counseling, Trauma Therapy', 'Ph.D. Counseling Psychology, University of Cape Town', 12, true, true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 4)

UNION ALL
SELECT
    5, 'counselor3', 'sarah.banda@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Sarah', 'Banda', '+260975678901', 'Counselor specializing in academic stress and anxiety', 'FEMALE', '1988-04-18', 'Psychology', 'Educational Psychology', 3, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', 'LIC2023004', 'Academic Stress, Anxiety Disorders', 'M.Sc. Psychology, University of Zambia', 3, true, true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 5)

UNION ALL
SELECT
    -- Students
    6, 'student1', 'student1@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'David', 'Phiri', '+260976789012', 'Computer Science student seeking counseling', 'MALE', '2000-09-12', 'Computer Science', 'Bachelor of Science', 2, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 6)

UNION ALL
SELECT
    7, 'student2', 'student2@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Emily', 'Kabwe', '+260977890123', 'Psychology student interested in counseling services', 'FEMALE', '2001-02-25', 'Psychology', 'Bachelor of Arts', 3, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 7)

UNION ALL
SELECT
    8, 'student3', 'student3@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'James', 'Mwamba', '+260978901234', 'Engineering student dealing with academic stress', 'MALE', '1999-11-30', 'Engineering', 'Bachelor of Engineering', 4, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 8)

UNION ALL
SELECT
    9, 'student4', 'student4@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Sophia', 'Lungu', '+260979012345', 'Medicine student needing stress management', 'FEMALE', '2002-05-14', 'Medicine', 'Bachelor of Medicine', 1, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 9)

UNION ALL
SELECT
    10, 'student5', 'student5@unza.zm', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQQQQQQQQQQQQQQQQQQQQQQ', 'Peter', 'Chanda', '+260970123456', 'Business student with anxiety issues', 'MALE', '2000-08-19', 'Business', 'Bachelor of Business Administration', 3, true, true, '2026-01-10 08:00:00', null, null, 'INTERNAL', null, null, null, null, false, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 10);

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES
    -- Super Admin
    (1, 1),
    
    -- Admin
    (2, 2),
    
    -- Counselors
    (3, 3),
    (4, 3),
    (5, 3),
    
    -- Students
    (6, 4),
    (7, 4),
    (8, 4),
    (9, 4),
    (10, 4);

-- Insert consent forms
INSERT INTO consent_form (id, title, content, version, is_active, created_at, updated_at, created_by_id)
VALUES
    (1, 'UNZA Counseling Services Consent Form', 'I hereby consent to receive counseling services from the University of Zambia Counseling Center. I understand that all information shared will be kept confidential except in cases where there is risk of harm to self or others.', '1.0', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 2),
    
    (2, 'Data Privacy and Confidentiality Agreement', 'I agree to the terms of data privacy and confidentiality as outlined by the UNZA Counseling Center. My personal information will be protected according to university policies and Zambian data protection laws.', '1.0', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00', 2);

-- Insert user consents (some students have signed, some haven't)
INSERT INTO user_consent (id, user_id, consent_form_id, signed_at, ip_address, user_agent, is_active)
VALUES
    (1, 6, 1, '2026-01-09 10:00:00', '192.168.1.100', 'Mozilla/5.0', true),
    (2, 6, 2, '2026-01-09 10:05:00', '192.168.1.100', 'Mozilla/5.0', true),
    (3, 7, 1, '2026-01-09 11:00:00', '192.168.1.101', 'Mozilla/5.0', true),
    (4, 7, 2, '2026-01-09 11:05:00', '192.168.1.101', 'Mozilla/5.0', true),
    (5, 8, 1, '2026-01-09 14:00:00', '192.168.1.102', 'Mozilla/5.0', true),
    (6, 8, 2, '2026-01-09 14:05:00', '192.168.1.102', 'Mozilla/5.0', true);

-- Update users to reflect consent status
UPDATE users SET has_signed_consent = true WHERE id IN (6, 7, 8);

-- Insert appointments
INSERT INTO appointment (id, title, student_id, counselor_id, appointment_date, duration, type, status, description, meeting_link, location, cancellation_reason, reminder_sent, created_at, updated_at)
VALUES
    -- Past appointments
    (1, 'Initial Consultation - Academic Stress', 6, 3, '2026-01-05 10:00:00', 60, 'INITIAL_CONSULTATION', 'COMPLETED', 'Student experiencing academic stress and anxiety', null, 'Counseling Office 1', null, false, '2026-01-04 08:00:00', '2026-01-05 12:00:00'),
    
    (2, 'Follow-up Session - Anxiety Management', 6, 3, '2026-01-07 14:00:00', 45, 'FOLLOW_UP', 'COMPLETED', 'Follow-up on anxiety management techniques', null, 'Counseling Office 1', null, false, '2026-01-06 08:00:00', '2026-01-07 16:00:00'),
    
    (3, 'Initial Consultation - Relationship Issues', 7, 4, '2026-01-06 09:00:00', 60, 'INITIAL_CONSULTATION', 'COMPLETED', 'Student dealing with relationship challenges', null, 'Counseling Office 2', null, false, '2026-01-05 08:00:00', '2026-01-06 11:00:00'),
    
    (4, 'Crisis Intervention - Exam Stress', 8, 5, '2026-01-08 11:00:00', 90, 'CRISIS_INTERVENTION', 'COMPLETED', 'Urgent session for exam-related stress and panic attacks', null, 'Counseling Office 3', null, false, '2026-01-07 08:00:00', '2026-01-08 13:00:00'),
    
    -- Upcoming appointments
    (5, 'Follow-up - Relationship Counseling', 7, 4, '2026-01-12 10:00:00', 45, 'FOLLOW_UP', 'SCHEDULED', 'Follow-up session on relationship progress', null, 'Counseling Office 2', null, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (6, 'Academic Performance Review', 8, 5, '2026-01-13 14:00:00', 60, 'ASSESSMENT', 'SCHEDULED', 'Review academic performance and stress management', null, 'Counseling Office 3', null, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (7, 'Initial Consultation - New Student', 9, 3, '2026-01-11 09:00:00', 60, 'INITIAL_CONSULTATION', 'SCHEDULED', 'First-time counseling session for medical student', null, 'Counseling Office 1', null, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (8, 'Group Session - Stress Management', 10, 4, '2026-01-14 15:00:00', 90, 'GROUP_SESSION', 'SCHEDULED', 'Group session on stress management techniques', null, 'Seminar Room A', null, false, '2026-01-10 08:00:00', '2026-01-10 08:00:00');

-- Insert sessions (linked to completed appointments)
INSERT INTO "session" (id, appointment_id, counselor_id, student_id, session_date, duration, status, type, summary, goals, next_steps, progress_notes, created_at, updated_at)
VALUES
    (1, 1, 3, 6, '2026-01-05 10:00:00', 60, 'COMPLETED', 'INDIVIDUAL', 'Initial consultation with David Phiri. Discussed academic stress, time management issues, and anxiety symptoms. Student reports difficulty concentrating and sleep disturbances.', 'Identify main stress triggers, develop basic coping strategies', 'Schedule follow-up session, provide stress management resources', 'Student showed good insight into issues. Willing to engage in counseling process.', '2026-01-05 12:00:00', '2026-01-05 12:00:00'),
    
    (2, 2, 3, 6, '2026-01-07 14:00:00', 45, 'COMPLETED', 'INDIVIDUAL', 'Follow-up session with David. Reviewed progress on stress management techniques. Student reports some improvement in sleep but still struggling with concentration.', 'Refine coping strategies, address concentration issues', 'Continue with current techniques, add mindfulness exercises', 'Student is making progress but needs more support with academic focus.', '2026-01-07 16:00:00', '2026-01-07 16:00:00'),
    
    (3, 3, 4, 7, '2026-01-06 09:00:00', 60, 'COMPLETED', 'INDIVIDUAL', 'Initial consultation with Emily Kabwe. Discussed relationship challenges with family and peers. Student reports feelings of isolation and low self-esteem.', 'Assess relationship patterns, build self-esteem', 'Explore communication skills, schedule regular sessions', 'Student is motivated but needs significant support with emotional regulation.', '2026-01-06 11:00:00', '2026-01-06 11:00:00'),
    
    (4, 4, 5, 8, '2026-01-08 11:00:00', 90, 'COMPLETED', 'INDIVIDUAL', 'Crisis intervention session with James Mwamba. Student presented with severe exam-related anxiety and panic symptoms. Provided immediate coping strategies and safety planning.', 'Stabilize acute symptoms, develop long-term stress management plan', 'Daily check-ins, refer to psychiatric services if needed', 'Student was in significant distress but responded well to grounding techniques.', '2026-01-08 13:00:00', '2026-01-08 13:00:00');

-- Insert session notes
INSERT INTO session_note (id, session_id, counselor_id, note_type, content, is_private, created_at, updated_at)
VALUES
    (1, 1, 3, 'PROGRESS', 'David showed good engagement during the session. He was able to identify specific stress triggers related to upcoming exams and family expectations. Responded well to basic relaxation techniques.', false, '2026-01-05 12:15:00', '2026-01-05 12:15:00'),
    
    (2, 1, 3, 'TREATMENT_PLAN', 'Plan: 1) Teach progressive muscle relaxation, 2) Develop study schedule with breaks, 3) Explore cognitive restructuring for negative thoughts about performance.', true, '2026-01-05 12:20:00', '2026-01-05 12:20:00'),
    
    (3, 2, 3, 'PROGRESS', 'David reports practicing relaxation techniques 3-4 times this week. Sleep has improved from 4-5 hours to 6-7 hours per night. Still struggling with concentration during study sessions.', false, '2026-01-07 16:15:00', '2026-01-07 16:15:00'),
    
    (4, 3, 4, 'ASSESSMENT', 'Emily presents with avoidant attachment style. Reports difficulty trusting peers and fear of abandonment. Family history suggests enmeshment patterns. Will explore attachment theory in next session.', true, '2026-01-06 11:15:00', '2026-01-06 11:15:00'),
    
    (5, 4, 5, 'CRISIS', 'James arrived in acute distress with hyperventilation and depersonalization symptoms. Used 4-7-8 breathing and grounding techniques to stabilize. Safety contract established.', true, '2026-01-08 13:15:00', '2026-01-08 13:15:00');

-- Insert self-assessments
INSERT INTO self_assessment (id, user_id, assessment_date, anxiety_level, depression_level, stress_level, sleep_quality, academic_pressure, social_relationships, overall_wellbeing, concerns, goals, is_anonymous, created_at, updated_at)
VALUES
    (1, 6, '2026-01-04 09:00:00', 8, 5, 9, 4, 9, 6, 5, 'Struggling with exam preparation and sleep disturbances', 'Improve concentration and reduce anxiety', false, '2026-01-04 09:00:00', '2026-01-04 09:00:00'),
    
    (2, 6, '2026-01-10 08:30:00', 6, 4, 7, 6, 8, 7, 6, 'Still stressed but sleep has improved', 'Maintain progress and prepare for next exams', false, '2026-01-10 08:30:00', '2026-01-10 08:30:00'),
    
    (3, 7, '2026-01-05 10:00:00', 7, 6, 8, 5, 7, 4, 5, 'Relationship issues affecting my studies', 'Improve communication skills and self-esteem', false, '2026-01-05 10:00:00', '2026-01-05 10:00:00'),
    
    (4, 8, '2026-01-07 15:00:00', 9, 7, 10, 3, 10, 5, 4, 'Severe anxiety about upcoming exams', 'Get through exams without panic attacks', false, '2026-01-07 15:00:00', '2026-01-07 15:00:00'),
    
    (5, 8, '2026-01-10 09:00:00', 6, 5, 8, 5, 8, 6, 5, 'Feeling better after crisis session', 'Maintain stability during exam period', false, '2026-01-10 09:00:00', '2026-01-10 09:00:00');

-- Insert risk assessments
INSERT INTO risk_assessment (id, user_id, counselor_id, assessment_date, risk_level, risk_factors, protective_factors, suicide_risk, self_harm_risk, substance_abuse_risk, academic_dropout_risk, risk_score, assessment_tool, clinical_notes, recommendations, follow_up_required, follow_up_date, created_at, updated_at)
VALUES
    (1, 6, 3, '2026-01-05 11:00:00', 'MEDIUM', '{"academic_stress": "high", "sleep_disturbances": "moderate", "concentration_issues": "moderate"}', '{"social_support": "moderate", "motivation": "high", "coping_skills": "developing"}', false, false, false, false, 55.00, 'UNZA Counseling Risk Assessment Tool', 'Student presents with moderate risk factors but good protective factors. Academic stress is primary concern.', 'Weekly counseling sessions, stress management workshop referral, monitor sleep patterns', true, '2026-01-12', '2026-01-05 11:00:00', '2026-01-05 11:00:00'),
    
    (2, 7, 4, '2026-01-06 10:00:00', 'LOW', '{"relationship_issues": "moderate", "self_esteem": "low"}', '{"social_support": "high", "academic_performance": "good", "motivation": "high"}', false, false, false, false, 35.00, 'UNZA Counseling Risk Assessment Tool', 'Student has good support system despite relationship challenges. Low risk of academic impact.', 'Bi-weekly counseling sessions, social skills group recommendation', true, '2026-01-20', '2026-01-06 10:00:00', '2026-01-06 10:00:00'),
    
    (3, 8, 5, '2026-01-08 12:00:00', 'HIGH', '{"exam_anxiety": "severe", "panic_attacks": "frequent", "sleep_deprivation": "severe"}', '{"intellectual_ability": "high", "family_support": "moderate"}', true, false, false, true, 85.00, 'UNZA Counseling Risk Assessment Tool', 'Student in acute distress with high risk of academic dropout. Immediate intervention required.', 'Intensive counseling (2x/week), psychiatric referral, academic accommodation request, daily check-ins', true, '2026-01-11', '2026-01-08 12:00:00', '2026-01-08 12:00:00');

-- Insert academic performance data
INSERT INTO academic_performance (id, user_id, academic_year, semester, gpa, total_credits, credits_earned, courses, attendance_percentage, study_hours_per_week, academic_goals, challenges_faced, support_needed, created_at, updated_at)
VALUES
    (1, 6, '2024/2025', 1, 3.20, 18, 18, '{"CS101": {"grade": "B+", "credits": 3}, "MATH101": {"grade": "B", "credits": 4}, "ENG101": {"grade": "A-", "credits": 3}}', 85.5, 15, 'Improve GPA to 3.5, complete programming projects', 'Time management, exam anxiety', 'Study skills workshop, stress management', '2026-01-05 10:00:00', '2026-01-05 10:00:00'),
    
    (2, 6, '2024/2025', 2, 3.00, 18, 15, '{"CS102": {"grade": "B", "credits": 3}, "MATH102": {"grade": "C+", "credits": 4}, "PSY101": {"grade": "B+", "credits": 3}}', 78.2, 12, 'Recover from last semester, focus on math', 'Exam performance, concentration', 'Tutoring, counseling', '2026-01-05 10:00:00', '2026-01-05 10:00:00'),
    
    (3, 7, '2024/2025', 1, 3.70, 18, 18, '{"PSY101": {"grade": "A", "credits": 3}, "SOC101": {"grade": "A-", "credits": 3}, "ENG101": {"grade": "A", "credits": 3}}', 92.1, 20, 'Maintain high GPA, prepare for honors program', 'Relationship stress affecting focus', 'Counseling, time management', '2026-01-06 09:00:00', '2026-01-06 09:00:00'),
    
    (4, 8, '2024/2025', 1, 2.80, 18, 15, '{"ENG101": {"grade": "B", "credits": 3}, "MATH101": {"grade": "C", "credits": 4}, "CHEM101": {"grade": "B-", "credits": 3}}', 75.3, 10, 'Improve engineering grades, reduce stress', 'Exam anxiety, time management', 'Tutoring, counseling, study group', '2026-01-08 11:00:00', '2026-01-08 11:00:00');

-- Insert resources
INSERT INTO resource (id, title, description, content_type, category, tags, is_public, created_at, updated_at)
VALUES
    (1, 'Mental Health Awareness Guide', 'Comprehensive guide to understanding mental health issues in university settings', 'PDF', 'Educational', 'mental health,awareness,guide', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (2, 'Stress Management Techniques', 'Practical techniques for managing academic and personal stress', 'PDF', 'Self-Help', 'stress,management,techniques', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (3, 'Crisis Intervention Protocol', 'Emergency response procedures for mental health crises', 'PDF', 'Protocol', 'crisis,intervention,emergency', false, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (4, 'Counseling Intake Form Template', 'Standard intake form for new counseling clients', 'DOC', 'Forms', 'intake,form,template', false, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (5, 'Academic Performance and Mental Health Study', 'Research findings on the relationship between academic performance and mental health', 'PDF', 'Research', 'research,academic,performance,mental health', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00');

-- Insert messages
INSERT INTO message (id, sender_id, recipient_id, subject, content, message_type, priority, is_read, read_at, created_at, updated_at)
VALUES
    (1, 6, 3, 'Follow-up Question', 'Thank you for our session yesterday. I wanted to ask about the relaxation techniques you mentioned.', 'GENERAL', 'NORMAL', true, '2026-01-06 10:00:00', '2026-01-06 10:00:00', '2026-01-06 10:00:00'),
    
    (2, 3, 6, 'Re: Follow-up Question', 'Hi David, I''m glad you found the session helpful. I''ve attached a guide with the relaxation techniques we discussed.', 'REPLY', 'NORMAL', true, '2026-01-06 11:00:00', '2026-01-06 11:00:00', '2026-01-06 11:00:00'),
    
    (3, 7, 4, 'Session Rescheduling', 'I need to reschedule our session on the 12th. Would the 13th at the same time work?', 'APPOINTMENT', 'HIGH', false, null, '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
    
    (4, 8, 5, 'Urgent: Need Help', 'I''m feeling overwhelmed with the upcoming exams. Can we schedule an emergency session?', 'CRISIS', 'URGENT', true, '2026-01-08 10:00:00', '2026-01-08 10:00:00', '2026-01-08 10:00:00');

-- Insert notifications
INSERT INTO notification (id, user_id, title, message, type, priority, action_url, is_read, read_at, created_at, updated_at)
VALUES
    (1, 6, 'Upcoming Appointment', 'You have an appointment with Grace Chiluba on January 12, 2026 at 10:00 AM', 'APPOINTMENT', 'MEDIUM', '/appointments/5', false, null, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (2, 7, 'New Message', 'You have a new message from your counselor regarding session rescheduling', 'MESSAGE', 'HIGH', '/messages/3', false, null, '2026-01-10 09:00:00', '2026-01-10 09:00:00'),
    
    (3, 8, 'Crisis Support Available', 'Remember that crisis support is available 24/7. Contact us immediately if you need help.', 'RISK_ASSESSMENT', 'URGENT', '/crisis-support', false, null, '2026-01-08 12:00:00', '2026-01-08 12:00:00'),
    
    (4, 3, 'New Client Assignment', 'You have been assigned a new client: David Phiri. Review the intake form before the first session.', 'SYSTEM', 'MEDIUM', '/clients/6', true, '2026-01-05 09:00:00', '2026-01-05 09:00:00', '2026-01-05 09:00:00');

-- Insert settings
INSERT INTO settings (id, key, value, description, category, is_active, created_at, updated_at)
VALUES
    (1, 'appointment.reminder.hours', '24', 'Hours before appointment to send reminder', 'APPOINTMENT', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (2, 'session.max.duration', '120', 'Maximum duration for counseling sessions in minutes', 'SESSION', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (3, 'risk_assessment.followup.days', '7', 'Days to follow up after high-risk assessment', 'RISK_ASSESSMENT', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (4, 'notification.email.enabled', 'true', 'Enable email notifications for users', 'NOTIFICATION', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00'),
    
    (5, 'system.maintenance.mode', 'false', 'Enable or disable system maintenance mode', 'SYSTEM', true, '2026-01-10 08:00:00', '2026-01-10 08:00:00');

-- Insert audit log entries
INSERT INTO audit_log (id, user_id, action, entity_type, entity_id, old_values, new_values, ip_address, user_agent, timestamp)
VALUES
    (1, 1, 'SYSTEM_INITIALIZATION', 'SYSTEM', 1, null, '{"status": "initialized", "users": 10, "roles": 5}', '192.168.1.1', 'Mozilla/5.0', '2026-01-10 08:00:00'),
    
    (2, 2, 'USER_CREATION', 'USER', 6, null, '{"username": "student1", "email": "student1@unza.zm", "role": "STUDENT"}', '192.168.1.1', 'Mozilla/5.0', '2026-01-10 08:15:00'),
    
    (3, 3, 'APPOINTMENT_CREATION', 'APPOINTMENT', 1, null, '{"student_id": 6, "counselor_id": 3, "status": "SCHEDULED"}', '192.168.1.1', 'Mozilla/5.0', '2026-01-10 08:30:00'),
    
    (4, 4, 'RISK_ASSESSMENT_UPDATE', 'RISK_ASSESSMENT', 3, '{"risk_level": "HIGH"}', '{"risk_level": "MEDIUM", "follow_up_required": true}', '192.168.1.1', 'Mozilla/5.0', '2026-01-10 09:00:00');