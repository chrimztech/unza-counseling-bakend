-- Add default theme/appearance settings
-- This migration adds default values for theme settings to support dark mode
--
-- Guarded: the "settings" table is managed by Hibernate (ddl-auto=update), which
-- runs AFTER Flyway migrations on startup. On a fresh database this table does not
-- exist yet when this migration runs, so every statement is wrapped in an
-- existence check (same defensive pattern used elsewhere in this migration set)
-- rather than failing the whole startup. On a database where Hibernate has
-- already created the table, this behaves exactly as before.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'settings') THEN
        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'themeMode', 'LIGHT', 'STRING', 'APPEARANCE', 'Theme mode: LIGHT, DARK, or SYSTEM', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'themeMode');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'primaryColor', '#3B82F6', 'STRING', 'APPEARANCE', 'Primary color hex code', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'primaryColor');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'compactMode', 'false', 'BOOLEAN', 'APPEARANCE', 'Enable compact mode', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'compactMode');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'reducedMotion', 'false', 'BOOLEAN', 'APPEARANCE', 'Enable reduced motion for accessibility', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'reducedMotion');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'highContrast', 'false', 'BOOLEAN', 'APPEARANCE', 'Enable high contrast mode', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'highContrast');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'fontSize', 'MEDIUM', 'STRING', 'APPEARANCE', 'Font size preference: SMALL, MEDIUM, LARGE', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'fontSize');

        -- Add session timeout settings
        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'sessionTimeoutMinutes', '30', 'INTEGER', 'SECURITY', 'Session timeout in minutes (0 = no timeout)', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'sessionTimeoutMinutes');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'sessionTimeoutEnabled', 'true', 'BOOLEAN', 'SECURITY', 'Enable session timeout', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'sessionTimeoutEnabled');

        INSERT INTO settings (key, value, type, category, description, active)
        SELECT 'sessionWarningMinutes', '5', 'INTEGER', 'SECURITY', 'Warning time before session timeout (in minutes)', true
        WHERE NOT EXISTS (SELECT 1 FROM settings WHERE key = 'sessionWarningMinutes');
    END IF;
END $$;
