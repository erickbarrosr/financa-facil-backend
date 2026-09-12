CREATE TABLE accounts (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(20)  NOT NULL CHECK (type IN ('checking','wallet','credit_card','investment')),
    balance    DECIMAL(19,4) NOT NULL DEFAULT 0,
    color      VARCHAR(7)   NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
