-- Migration V9: Add ecommerce_url field to users table
-- This field stores the base URL of the author's e-commerce (each author has their own e-commerce)

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS ecommerce_url VARCHAR(500);

