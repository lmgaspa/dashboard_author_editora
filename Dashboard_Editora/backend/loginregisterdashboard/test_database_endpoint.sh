#!/bin/bash

# Script para testar o endpoint de verificação do banco de dados
# Uso: ./test_database_endpoint.sh [admin_email] [admin_password]

ADMIN_EMAIL="${1:-${ADMIN_EMAIL:-andescoresoftware@gmail.com}}"
ADMIN_PASSWORD="${2:-${ADMIN_PASSWORD:-Lila1210@}}"
BASE_URL="http://localhost:8000"

echo "🧪 Testando endpoint de verificação do banco de dados"
echo "📧 Email: $ADMIN_EMAIL"
echo "🔗 URL: $BASE_URL"
echo ""

# 1. Fazer login
echo "1️⃣  Fazendo login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$ADMIN_EMAIL\",
    \"password\": \"$ADMIN_PASSWORD\"
  }")

echo "Resposta do login:"
echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
echo ""

# Extrair o access token (o campo correto é 'accessToken' no LoginResponse)
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken // .access // .access_token // empty' 2>/dev/null)

if [ -z "$ACCESS_TOKEN" ] || [ "$ACCESS_TOKEN" = "null" ]; then
  echo "❌ Erro: Não foi possível obter o token de acesso"
  echo "Verifique se as credenciais estão corretas e se o usuário é ADMIN"
  exit 1
fi

echo "✅ Token obtido: ${ACCESS_TOKEN:0:20}..."
echo ""

# 2. Testar endpoint do banco de dados
echo "2️⃣  Testando endpoint /api/admin/database/status..."
DB_STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/database/status" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json")

echo "Resposta do status do banco:"
echo "$DB_STATUS_RESPONSE" | jq '.' 2>/dev/null || echo "$DB_STATUS_RESPONSE"
echo ""

# Verificar se foi bem-sucedido
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/database/status" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

if [ "$HTTP_CODE" = "200" ]; then
  echo "✅ Endpoint funcionando corretamente!"
  
  # Mostrar informações resumidas
  echo ""
  echo "📊 Resumo:"
  echo "$DB_STATUS_RESPONSE" | jq -r '
    "  Mensagem: " + .message,
    "  Total de tabelas: " + (.totalTables | tostring),
    "  Total de usuários: " + (.totalUsers | tostring),
    "  Total de admins: " + (.totalAdmins | tostring)
  ' 2>/dev/null
  
  echo ""
  echo "📋 Tabelas encontradas:"
  echo "$DB_STATUS_RESPONSE" | jq -r '.tables[] | "  - " + .table_name' 2>/dev/null
  
  echo ""
  echo "🔄 Versões do Flyway:"
  echo "$DB_STATUS_RESPONSE" | jq -r '.flywayVersions[] | "  - v\(.version): \(.description) (instalado em \(.installed_on))"' 2>/dev/null
  
else
  echo "❌ Erro: HTTP $HTTP_CODE"
  echo "Resposta: $DB_STATUS_RESPONSE"
fi

