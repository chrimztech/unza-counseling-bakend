-- Crisis alert tracking for flagged appointments and messages

CREATE TABLE crisis_alerts (
    id                BIGSERIAL PRIMARY KEY,
    source_type       VARCHAR(20)  NOT NULL,
    source_id         BIGINT,
    client_id         BIGINT       REFERENCES users(id),
    severity          VARCHAR(10)  NOT NULL,
    triggered_keywords VARCHAR(1000),
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    counselor_notes   VARCHAR(2000),
    reviewed_by_id    BIGINT       REFERENCES users(id),
    reviewed_at       TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crisis_alerts_status     ON crisis_alerts(status);
CREATE INDEX idx_crisis_alerts_client     ON crisis_alerts(client_id);
CREATE INDEX idx_crisis_alerts_created_at ON crisis_alerts(created_at DESC);

-- Flag column on appointments so the DTO can surface it to the frontend
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS is_critical      BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS crisis_keywords  VARCHAR(500);
