#!/bin/bash

# Script simples para testar o endpoint diretamente
# Primeiro faça login manualmente e copie o token

echo "🧪 Teste Simples do Endpoint de Banco de Dados"
echo ""
echo "Para testar, siga estes passos:"
echo ""
echo "1️⃣  Faça login primeiro:"
echo "   curl -X POST http://localhost:8000/api/v1/auth/login \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '{\"email\":\"seu-email@exemplo.com\",\"password\":\"sua-senha\"}'"
echo ""
echo "2️⃣  Copie o 'accessToken' da resposta"
echo ""
echo "3️⃣  Execute o teste:"
echo "   curl -X GET http://localhost:8000/api/admin/database/status \\"
echo "     -H 'Authorization: Bearer SEU_TOKEN_AQUI' \\"
echo "     -H 'Content-Type: application/json' | jq '.'"
echo ""
echo "Ou use este script passando o token:"
echo "   ./test_database_simple.sh SEU_TOKEN_AQUI"
echo ""

if [ -n "$1" ]; then
  TOKEN="$1"
  BASE_URL="http://localhost:8000"
  
  echo "🔍 Testando com o token fornecido..."
  echo ""
  
  RESPONSE=$(curl -s -X GET "$BASE_URL/api/admin/database/status" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json")
  
  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE_URL/api/admin/database/status" \
    -H "Authorization: Bearer $TOKEN")
  
  echo "HTTP Status: $HTTP_CODE"
  echo ""
  
  if [ "$HTTP_CODE" = "200" ]; then
    echo "✅ Sucesso! Resposta:"
    echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
    echo ""
    echo "📊 Resumo:"
    echo "$RESPONSE" | jq -r '
      "  ✅ " + .message,
      "  📋 Total de tabelas: " + (.totalTables | tostring),
      "  👥 Total de usuários: " + (.totalUsers | tostring),
      "  👑 Total de admins: " + (.totalAdmins | tostring)
    ' 2>/dev/null
  else
    echo "❌ Erro HTTP $HTTP_CODE"
    echo "Resposta: $RESPONSE"
  fi
fi

