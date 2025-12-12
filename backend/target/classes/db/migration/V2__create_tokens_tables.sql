-- Create account_confirmation_tokens table
CREATE TABLE IF NOT EXISTS account_confirmation_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    valid BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_act_user_id ON account_confirmation_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_act_token_hash ON account_confirmation_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_act_expires_at ON account_confirmation_tokens(expires_at);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_rt_token ON refresh_tokens(token);

-- Create password_reset_tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_prt_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_prt_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_prt_expires_at ON password_reset_tokens(expires_at);

-- Create email_change_tokens table
CREATE TABLE IF NOT EXISTS email_change_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    new_email_normalized VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    consumed_at TIMESTAMP,
    valid BOOLEAN
);

CREATE INDEX IF NOT EXISTS idx_ect_user_id ON email_change_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_ect_token_hash ON email_change_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_ect_new_email ON email_change_tokens(new_email_normalized);
CREATE INDEX IF NOT EXISTS idx_ect_expires_at ON email_change_tokens(expires_at);

-- Create confirm_resend_throttle table
CREATE TABLE IF NOT EXISTS confirm_resend_throttle (
    id VARCHAR(100) PRIMARY KEY,
    user_id UUID NOT NULL,
    email_hash VARCHAR(255),
    attempts_today INTEGER NOT NULL,
    last_sent_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_crt_user_id ON confirm_resend_throttle(user_id);
CREATE INDEX IF NOT EXISTS idx_crt_email_hash ON confirm_resend_throttle(email_hash);
CREATE INDEX IF NOT EXISTS idx_crt_created_at ON confirm_resend_throttle(created_at);

