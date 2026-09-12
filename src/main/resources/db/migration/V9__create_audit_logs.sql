CREATE TABLE audit_logs (
    id         UUID         PRIMARY KEY,
    user_id    UUID         REFERENCES users(id) ON DELETE SET NULL,
    action     VARCHAR(100) NOT NULL,
    entity     VARCHAR(100),
    entity_id  UUID,
    metadata   JSONB,
    ip         VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
