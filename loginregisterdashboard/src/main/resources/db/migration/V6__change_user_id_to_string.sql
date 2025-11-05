-- Migration V6: Change user IDs from UUID to VARCHAR with prefix format
-- Format: admin-1, user-1, user-2, etc.

-- Step 1: Add temporary column for new ID format
ALTER TABLE users ADD COLUMN IF NOT EXISTS id_new VARCHAR(20);

-- Step 2: Migrate existing data (if any)
-- Admin gets "admin-1", others get "user-X" based on creation order
-- If no existing data, this will be empty
-- Using subquery approach since window functions aren't allowed in UPDATE directly
UPDATE users u
SET id_new = CASE 
    WHEN u.role = 'ADMIN' THEN 'admin-1'
    ELSE 'user-' || (
        SELECT COUNT(*) + 1 
        FROM users u2 
        WHERE u2.role != 'ADMIN' 
        AND u2.email < u.email
    )::TEXT
END;

-- Step 3: Drop foreign key constraints (if they exist)
-- Note: PostgreSQL doesn't enforce foreign keys by default on UUID, but we'll check
-- We need to drop indexes first
DROP INDEX IF EXISTS idx_act_user_id;
DROP INDEX IF EXISTS idx_prt_user_id;
DROP INDEX IF EXISTS idx_ect_user_id;
DROP INDEX IF EXISTS idx_crt_user_id;

-- Step 4: Change user_id columns in token tables to VARCHAR
ALTER TABLE account_confirmation_tokens 
    ALTER COLUMN user_id TYPE VARCHAR(20) USING user_id::TEXT;

ALTER TABLE password_reset_tokens 
    ALTER COLUMN user_id TYPE VARCHAR(20) USING user_id::TEXT;

ALTER TABLE email_change_tokens 
    ALTER COLUMN user_id TYPE VARCHAR(20) USING user_id::TEXT;

ALTER TABLE confirm_resend_throttle 
    ALTER COLUMN user_id TYPE VARCHAR(20) USING user_id::TEXT;

-- Step 5: Update token tables with new user_id format
UPDATE account_confirmation_tokens act
SET user_id = u.id_new
FROM users u
WHERE act.user_id::TEXT = u.id::TEXT;

UPDATE password_reset_tokens prt
SET user_id = u.id_new
FROM users u
WHERE prt.user_id::TEXT = u.id::TEXT;

UPDATE email_change_tokens ect
SET user_id = u.id_new
FROM users u
WHERE ect.user_id::TEXT = u.id::TEXT;

UPDATE confirm_resend_throttle crt
SET user_id = u.id_new
FROM users u
WHERE crt.user_id::TEXT = u.id::TEXT;

-- Step 6: Drop old UUID column and rename new column
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE users DROP COLUMN IF EXISTS id;
ALTER TABLE users RENAME COLUMN id_new TO id;
ALTER TABLE users ADD PRIMARY KEY (id);

-- Step 7: Recreate indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_act_user_id ON account_confirmation_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_prt_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_ect_user_id ON email_change_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_crt_user_id ON confirm_resend_throttle(user_id);

