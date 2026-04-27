ALTER TABLE IF EXISTS personal_data_forms
    ADD COLUMN IF NOT EXISTS student_category VARCHAR(50);

COMMENT ON COLUMN personal_data_forms.student_category IS
    'Paper form field: Local Student or International Student';
