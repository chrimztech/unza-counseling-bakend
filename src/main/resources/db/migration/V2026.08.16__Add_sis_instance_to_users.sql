-- Caches which SIS population (ug, pg, gsb, ide, zou, ecampus) a student authenticated
-- against on the devoap.unza.zm student login endpoint, so subsequent logins can go
-- straight to the right instance instead of trying all six in sequence every time.
ALTER TABLE users ADD COLUMN IF NOT EXISTS sis_instance VARCHAR(20);
