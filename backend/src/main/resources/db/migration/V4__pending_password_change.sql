CREATE TABLE pending_password_changes (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    new_password_hash VARCHAR(100) NOT NULL,
    otp_hash VARCHAR(100) NOT NULL,
    otp_expires_at TIMESTAMPTZ NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);