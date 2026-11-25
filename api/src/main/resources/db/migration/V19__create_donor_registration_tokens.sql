CREATE TABLE donor_registration_tokens (
    email VARCHAR(255) PRIMARY KEY,
    token_hash CHAR(64) NOT NULL UNIQUE,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_donor_registration_expiry CHECK (expires_at > issued_at)
);

CREATE INDEX ix_donor_registration_expiry ON donor_registration_tokens (expires_at);
