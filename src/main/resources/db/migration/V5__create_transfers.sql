CREATE TABLE transfers (
    id              UUID          PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    from_account_id UUID          NOT NULL REFERENCES accounts(id),
    to_account_id   UUID          NOT NULL REFERENCES accounts(id),
    amount          DECIMAL(19,4) NOT NULL,
    transfer_date   DATE          NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
