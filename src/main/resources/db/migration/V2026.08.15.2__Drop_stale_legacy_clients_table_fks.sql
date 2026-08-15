-- The Client JPA entity uses SINGLE_TABLE inheritance (Client extends User,
-- @DiscriminatorValue("CLIENT")) — a Client IS a row in the "users" table, and
-- Client.id == User.id. ClientIdentityService and every actively-used service
-- (AcademicPerformanceService, SessionService, AppointmentServiceImpl, etc.)
-- resolve/promote clients through "users", never through the separate legacy
-- "clients" table that an earlier schema generation created.
--
-- Four tables were left with TWO foreign keys on client_id from that earlier
-- generation: one correctly targeting users(id) (matching every other
-- client_id column in the schema), and one stale leftover still targeting the
-- now-unpopulated legacy "clients" table. Since nothing in the application
-- writes to that table anymore, the stale constraint made it impossible to
-- insert academic performance, risk assessment, self-assessment, or mental
-- health analysis records for any client who was promoted the normal way
-- (via ClientIdentityService, i.e. every real client in this system).
--
-- The legacy "clients" table itself is left in place (not dropped) since its
-- removal isn't required to fix this and carries more risk to review safely.

ALTER TABLE IF EXISTS academic_performance
    DROP CONSTRAINT IF EXISTS academic_performance_client_id_fkey;

ALTER TABLE IF EXISTS mental_health_academic_analysis
    DROP CONSTRAINT IF EXISTS mental_health_academic_analysis_client_id_fkey;

ALTER TABLE IF EXISTS risk_assessments
    DROP CONSTRAINT IF EXISTS risk_assessments_client_id_fkey;

ALTER TABLE IF EXISTS self_assessments
    DROP CONSTRAINT IF EXISTS self_assessments_client_id_fkey;
