CREATE TABLE users (
    id             UUID PRIMARY KEY,
    name           VARCHAR(150)  NOT NULL,
    email          VARCHAR(255)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255)  NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_login_at  TIMESTAMPTZ,
    email_verified BOOLEAN       NOT NULL DEFAULT false,
    is_active      BOOLEAN       NOT NULL DEFAULT true
);
