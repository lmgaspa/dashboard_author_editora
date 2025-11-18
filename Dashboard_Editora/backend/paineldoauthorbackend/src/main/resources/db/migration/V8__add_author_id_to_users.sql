-- Migration V8: Add author_id field to users table
-- This field links users to authors in the external system (nullable)

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS author_id VARCHAR(255);

-- Create index for faster queries by author_id
CREATE INDEX IF NOT EXISTS idx_user_author_id ON users(author_id);

