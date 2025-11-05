-- Script para verificar o banco de dados e tabelas
-- Execute com: psql -h localhost -U seu_usuario -d loginregisterdashboardeditora -f check_database.sql

-- 1. Verificar se o banco existe
SELECT datname FROM pg_database WHERE datname = 'loginregisterdashboardeditora';

-- 2. Listar todas as tabelas
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- 3. Verificar estrutura da tabela users
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND table_name = 'users'
ORDER BY ordinal_position;

-- 4. Contar registros nas tabelas principais
SELECT 
    'users' as tabela, 
    COUNT(*) as total_registros 
FROM users
UNION ALL
SELECT 
    'account_confirmation_tokens', 
    COUNT(*) 
FROM account_confirmation_tokens
UNION ALL
SELECT 
    'refresh_tokens', 
    COUNT(*) 
FROM refresh_tokens
UNION ALL
SELECT 
    'password_reset_tokens', 
    COUNT(*) 
FROM password_reset_tokens
UNION ALL
SELECT 
    'email_change_tokens', 
    COUNT(*) 
FROM email_change_tokens
UNION ALL
SELECT 
    'confirm_resend_throttle', 
    COUNT(*) 
FROM confirm_resend_throttle;

-- 5. Verificar usuário admin (se existe)
SELECT id, name, email, role, email_confirmed, auth_provider 
FROM users 
WHERE role = 'ADMIN';

-- 6. Verificar versão do Flyway
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC;

