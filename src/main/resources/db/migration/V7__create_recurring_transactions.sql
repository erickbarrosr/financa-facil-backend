CREATE TABLE recurring_transactions (
    id             UUID          PRIMARY KEY,
    user_id        UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id     UUID          NOT NULL REFERENCES accounts(id),
    category_id    UUID          REFERENCES categories(id) ON DELETE SET NULL,
    amount         DECIMAL(19,4) NOT NULL,
    type           VARCHAR(10)   NOT NULL CHECK (type IN ('income','expense')),
    frequency      VARCHAR(10)   NOT NULL CHECK (frequency IN ('daily','weekly','monthly','yearly')),
    next_execution DATE          NOT NULL,
    active         BOOLEAN       NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
