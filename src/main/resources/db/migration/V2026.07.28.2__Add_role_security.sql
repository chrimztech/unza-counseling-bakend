-- Add ROLE_SECURITY for university Security staff who review sensitive-case alerts
-- (SecurityAlert feature). Mirrors the original role seeding in V10__Seed_all_schemas.sql.
INSERT INTO roles (id, name, description)
SELECT 6, 'ROLE_SECURITY', 'University security staff reviewing sensitive-case alerts'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_SECURITY');

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'roles_id_seq') THEN
        PERFORM setval('roles_id_seq', GREATEST((SELECT COALESCE(MAX(id), 0) FROM roles), 1));
    END IF;
END $$;
