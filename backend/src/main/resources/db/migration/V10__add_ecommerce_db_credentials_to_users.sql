-- Migration V10: Add e-commerce database credentials to users table
-- Cada autor tem seu próprio e-commerce com seu próprio banco de dados

ALTER TABLE users 
ADD COLUMN IF NOT EXISTS ecommerce_db_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS ecommerce_db_username VARCHAR(255),
ADD COLUMN IF NOT EXISTS ecommerce_db_password VARCHAR(500);  -- Será criptografado no futuro

-- Índice para buscar por URL de e-commerce
CREATE INDEX IF NOT EXISTS idx_user_ecommerce_url ON users(ecommerce_url) WHERE ecommerce_url IS NOT NULL;

