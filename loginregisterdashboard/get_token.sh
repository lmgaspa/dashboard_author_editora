#!/bin/bash

# Script para obter o token JWT facilmente
# Uso: ./get_token.sh [email] [senha]

EMAIL="${1:-${ADMIN_EMAIL}}"
PASSWORD="${2:-${ADMIN_PASSWORD}}"
BASE_URL="http://localhost:8000"

echo "🔑 Obtendo Token JWT"
echo "===================="
echo ""

if [ -z "$EMAIL" ] || [ -z "$PASSWORD" ]; then
  echo "❌ Erro: Email e senha são obrigatórios"
  echo ""
  echo "Uso:"
  echo "  ./get_token.sh seu-email@exemplo.com sua-senha"
  echo ""
  echo "Ou defina as variáveis de ambiente:"
  echo "  export ADMIN_EMAIL=seu-email@exemplo.com"
  echo "  export ADMIN_PASSWORD=sua-senha"
  echo "  ./get_token.sh"
  exit 1
fi

echo "📧 Email: $EMAIL"
echo "🔗 URL: $BASE_URL/api/v1/auth/login"
echo ""

# Fazer login
echo "⏳ Fazendo login..."
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$EMAIL\",
    \"password\": \"$PASSWORD\"
  }")

# Separar body e status code
HTTP_BODY=$(echo "$RESPONSE" | head -n -1)
HTTP_CODE=$(echo "$RESPONSE" | tail -n 1)

echo ""
echo "📊 Status HTTP: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" != "200" ]; then
  echo "❌ Erro no login!"
  echo ""
  echo "Resposta:"
  echo "$HTTP_BODY" | jq '.' 2>/dev/null || echo "$HTTP_BODY"
  echo ""
  echo "Possíveis causas:"
  echo "  - Email ou senha incorretos"
  echo "  - Email não confirmado (verifique no banco)"
  echo "  - Usuário não existe"
  exit 1
fi

# Extrair token (o campo é 'accessToken' no LoginResponse)
TOKEN=$(echo "$HTTP_BODY" | jq -r '.accessToken // .access // empty' 2>/dev/null)

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo "❌ Erro: Token não encontrado na resposta"
  echo ""
  echo "Resposta completa:"
  echo "$HTTP_BODY" | jq '.' 2>/dev/null || echo "$HTTP_BODY"
  exit 1
fi

echo "✅ Login realizado com sucesso!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "🎟️  TOKEN JWT:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$TOKEN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Salvar token em arquivo
echo "$TOKEN" > /tmp/jwt_token.txt
echo "💾 Token salvo em: /tmp/jwt_token.txt"
echo ""

# Mostrar como usar
echo "📋 Como usar o token:"
echo ""
echo "1️⃣  Testar endpoint do banco:"
echo "   curl -X GET $BASE_URL/api/admin/database/status \\"
echo "     -H 'Authorization: Bearer $TOKEN' | jq '.'"
echo ""
echo "2️⃣  Ou use o script:"
echo "   ./test_database_simple.sh $TOKEN"
echo ""
echo "3️⃣  Ou copie e cole no Swagger UI:"
echo "   - Acesse: $BASE_URL/swagger"
echo "   - Clique em 'Authorize' (🔓)"
echo "   - Cole o token acima"
echo ""

# Opção de testar automaticamente
read -p "🧪 Testar endpoint do banco agora? (s/n) " -n 1 -r
echo ""
if [[ $REPLY =~ ^[SsYy]$ ]]; then
  echo ""
  echo "🔍 Testando endpoint /api/admin/database/status..."
  echo ""
  
  DB_RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/database/status" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  DB_HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/database/status" \
    -H "Authorization: Bearer $TOKEN")
  
  if [ "$DB_HTTP_CODE" = "200" ]; then
    echo "✅ Endpoint funcionando!"
    echo ""
    echo "📊 Resumo:"
    echo "$DB_RESPONSE" | jq -r '
      "  ✅ " + .message,
      "  📋 Total de tabelas: " + (.totalTables | tostring),
      "  👥 Total de usuários: " + (.totalUsers | tostring),
      "  👑 Total de admins: " + (.totalAdmins | tostring)
    ' 2>/dev/null
    echo ""
    echo "📋 Tabelas:"
    echo "$DB_RESPONSE" | jq -r '.tables[] | "  - " + .table_name' 2>/dev/null
  else
    echo "❌ Erro HTTP $DB_HTTP_CODE"
    echo "Resposta: $DB_RESPONSE"
  fi
fi

