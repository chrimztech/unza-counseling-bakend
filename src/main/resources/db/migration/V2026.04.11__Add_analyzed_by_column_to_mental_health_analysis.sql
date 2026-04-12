-- Add missing analyzed_by column to mental_health_academic_analysis table
ALTER TABLE mental_health_academic_analysis ADD COLUMN analyzed_by BIGINT;
ALTER TABLE mental_health_academic_analysis ADD CONSTRAINT fk_analysis_analyzed_by FOREIGN KEY (analyzed_by) REFERENCES users(id);
CREATE INDEX idx_mental_health_analysis_analyzed_by ON mental_health_academic_analysis(analyzed_by);