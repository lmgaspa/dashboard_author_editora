-- Fix email_change_tokens.valid field to have NOT NULL and DEFAULT TRUE
-- This migration updates existing table structure to match the corrected V2 migration

-- First, set any NULL values to TRUE (they should all be TRUE anyway)
UPDATE email_change_tokens SET valid = TRUE WHERE valid IS NULL;

-- Now alter the column to be NOT NULL with DEFAULT TRUE
ALTER TABLE email_change_tokens 
    ALTER COLUMN valid SET DEFAULT TRUE,
    ALTER COLUMN valid SET NOT NULL;

