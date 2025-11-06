-- Script para corrigir o checksum do Flyway após modificar uma migration já aplicada
-- 
-- PROBLEMA:
-- A migration V6 foi modificada depois de ter sido aplicada no banco de dados.
-- O Flyway detecta isso e bloqueia a inicialização para evitar inconsistências.
-- 
-- ERRO:
-- Migration checksum mismatch for migration version 6
-- -> Applied to database : -1778059936
-- -> Resolved locally    : 1901482821
--
-- SOLUÇÃO:
-- Atualizar o checksum na tabela flyway_schema_history para corresponder ao arquivo atual.
--
-- Execute este script com:
-- psql -h localhost -U seu_usuario -d loginregisterdashboardeditora -f fix_flyway_checksum.sql
-- 
-- Ou execute diretamente no psql:
-- \c loginregisterdashboardeditora
-- UPDATE flyway_schema_history SET checksum = 1901482821 WHERE version = '6';

-- Atualizar checksum da migration V6
UPDATE flyway_schema_history 
SET checksum = 1901482821 
WHERE version = '6';

-- Verificar se foi atualizado
SELECT version, description, checksum, installed_on, success 
FROM flyway_schema_history 
WHERE version = '6';

