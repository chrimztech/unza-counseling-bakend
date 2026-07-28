-- Security alerts: sensitive-case alerts (self-harm, suicide, sexual assault, physical
-- attack, panic button, other) raised locally or mirrored in from the university clinic
-- system, surfaced to university Security staff (ROLE_SECURITY).
CREATE TABLE IF NOT EXISTS security_alerts (
    id                      BIGSERIAL PRIMARY KEY,
    category                VARCHAR(30)   NOT NULL,
    severity                VARCHAR(10)   NOT NULL,
    origin_system           VARCHAR(20)   NOT NULL,
    source_type             VARCHAR(20)   NOT NULL,
    subject_student_id      VARCHAR(50),
    subject_name            VARCHAR(200),
    reported_by_user_id     VARCHAR(50),
    reported_by_name        VARCHAR(200),
    description             TEXT,
    latitude                DOUBLE PRECISION,
    longitude               DOUBLE PRECISION,
    status                  VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    acknowledged_by_name    VARCHAR(200),
    acknowledged_at         TIMESTAMP,
    resolved_by_name        VARCHAR(200),
    resolved_at             TIMESTAMP,
    resolution_notes        TEXT,
    external_alert_id       VARCHAR(100),          -- the OTHER system's own record ID
    external_system         VARCHAR(20),
    occurred_at             TIMESTAMP     NOT NULL,
    created_at              TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_security_alerts_status            ON security_alerts(status);
CREATE INDEX IF NOT EXISTS idx_security_alerts_category           ON security_alerts(category);
CREATE INDEX IF NOT EXISTS idx_security_alerts_origin_system      ON security_alerts(origin_system);
CREATE INDEX IF NOT EXISTS idx_security_alerts_external_alert_id  ON security_alerts(external_alert_id);
CREATE INDEX IF NOT EXISTS idx_security_alerts_created_at         ON security_alerts(created_at);
