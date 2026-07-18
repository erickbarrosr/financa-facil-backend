CREATE TABLE transactions (
    id               UUID          PRIMARY KEY,
    user_id          UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id       UUID          NOT NULL REFERENCES accounts(id),
    category_id      UUID          REFERENCES categories(id) ON DELETE SET NULL,
    type             VARCHAR(10)   NOT NULL CHECK (type IN ('income','expense')),
    amount           DECIMAL(19,4) NOT NULL,
    description      TEXT,
    transaction_date DATE          NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED')),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);
