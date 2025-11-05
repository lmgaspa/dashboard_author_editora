#!/bin/bash

# Script para verificar o banco de dados
# Uso: ./check_database.sh [usuario] [senha]

USERNAME="${1:-${DATABASE_USERNAME:-postgres}}"
PASSWORD="${2:-${DATABASE_PASSWORD}}"
DB_NAME="loginregisterdashboardeditora"

echo "🔍 Verificando banco de dados: $DB_NAME"
echo "👤 Usuário: $USERNAME"
echo ""

if [ -z "$PASSWORD" ]; then
    echo "⚠️  Senha não fornecida. Tentando conectar sem senha..."
    export PGPASSWORD=""
else
    export PGPASSWORD="$PASSWORD"
fi

# Verificar se o banco existe
echo "1️⃣  Verificando se o banco existe..."
psql -h localhost -U "$USERNAME" -d postgres -c "SELECT datname FROM pg_database WHERE datname = '$DB_NAME';" 2>&1

echo ""
echo "2️⃣  Listando todas as tabelas..."
psql -h localhost -U "$USERNAME" -d "$DB_NAME" -c "\dt" 2>&1

echo ""
echo "3️⃣  Contando registros nas tabelas..."
psql -h localhost -U "$USERNAME" -d "$DB_NAME" <<EOF
SELECT 
    'users' as tabela, 
    COUNT(*) as total_registros 
FROM users
UNION ALL
SELECT 'account_confirmation_tokens', COUNT(*) FROM account_confirmation_tokens
UNION ALL
SELECT 'refresh_tokens', COUNT(*) FROM refresh_tokens
UNION ALL
SELECT 'password_reset_tokens', COUNT(*) FROM password_reset_tokens
UNION ALL
SELECT 'email_change_tokens', COUNT(*) FROM email_change_tokens
UNION ALL
SELECT 'confirm_resend_throttle', COUNT(*) FROM confirm_resend_throttle;
EOF

echo ""
echo "4️⃣  Verificando usuário admin..."
psql -h localhost -U "$USERNAME" -d "$DB_NAME" -c "SELECT id, name, email, role, email_confirmed FROM users WHERE role = 'ADMIN';" 2>&1

echo ""
echo "5️⃣  Verificando versão do Flyway..."
psql -h localhost -U "$USERNAME" -d "$DB_NAME" -c "SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;" 2>&1

echo ""
echo "✅ Verificação concluída!"

