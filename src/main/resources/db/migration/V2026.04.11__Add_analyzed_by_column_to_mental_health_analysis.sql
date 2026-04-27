-- Add missing analyzed_by column safely

ALTER TABLE IF EXISTS mental_health_academic_analysis
ADD COLUMN IF NOT EXISTS analyzed_by BIGINT;

-- Add FK safely (avoid duplicate constraint crash)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_analysis_analyzed_by'
    ) THEN
        ALTER TABLE mental_health_academic_analysis
        ADD CONSTRAINT fk_analysis_analyzed_by
        FOREIGN KEY (analyzed_by) REFERENCES users(id);
    END IF;
END $$;

-- Create index safely
CREATE INDEX IF NOT EXISTS idx_mental_health_analysis_analyzed_by
ON mental_health_academic_analysis(analyzed_by);