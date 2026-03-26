-- Add default theme/appearance settings
-- This migration adds default values for theme settings to support dark mode

-- Insert default theme settings if they don't exist
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