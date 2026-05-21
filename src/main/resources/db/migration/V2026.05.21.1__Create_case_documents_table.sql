-- Create case_documents table for attaching files/documents to case records

CREATE TABLE IF NOT EXISTS case_documents (
    id              BIGSERIAL PRIMARY KEY,
    case_id         BIGINT        NOT NULL REFERENCES cases(id),
    file_name       VARCHAR(255)  NOT NULL,
    file_path       VARCHAR(1000) NOT NULL,
    file_type       VARCHAR(100),
    file_size       BIGINT,
    description     VARCHAR(1000),
    uploaded_by     BIGINT,
    uploaded_at     TIMESTAMP     NOT NULL DEFAULT now(),
    is_public       BOOLEAN       DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_case_documents_case_id ON case_documents(case_id);
