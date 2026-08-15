-- The RiskAssessment JPA entity has no "counselor" field at all — it uses
-- assessor_id (nullable, no default) to record who performed the assessment.
-- counselor_id is a legacy leftover column from an earlier schema generation
-- that Hibernate can never populate, so its NOT NULL constraint made every
-- risk assessment insert fail. Same class of issue as the stale legacy
-- "clients" table foreign keys fixed in V2026.08.15.2.

ALTER TABLE IF EXISTS risk_assessments
    ALTER COLUMN counselor_id DROP NOT NULL;
