-- Migration V7: Confirm all existing users' emails
-- Since we removed the email confirmation flow, all existing users should be marked as confirmed

UPDATE users 
SET email_confirmed = true 
WHERE email_confirmed = false;

