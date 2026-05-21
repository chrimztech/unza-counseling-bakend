-- Add referral point columns missing from personal_data_forms

ALTER TABLE personal_data_forms
    ADD COLUMN IF NOT EXISTS referral_point_from VARCHAR(500),
    ADD COLUMN IF NOT EXISTS referral_point_to   VARCHAR(500);
