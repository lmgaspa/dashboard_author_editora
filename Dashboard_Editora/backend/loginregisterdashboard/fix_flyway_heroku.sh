#!/bin/bash

# Script para corrigir o checksum do Flyway no Heroku
# Uso: ./fix_flyway_heroku.sh

APP_NAME="dashboard-painel-autores-vl"

echo "🔧 Corrigindo checksum do Flyway no Heroku..."
echo "App: $APP_NAME"
echo ""

# Verificar se o Heroku CLI está instalado
if ! command -v heroku &> /dev/null; then
    echo "❌ Erro: Heroku CLI não está instalado."
    echo "Instale em: https://devcenter.heroku.com/articles/heroku-cli"
    exit 1
fi

# Executar SQL para corrigir checksum
echo "🔨 Executando correção do checksum..."
echo ""

# Atualizar checksum
heroku pg:psql --app "$APP_NAME" -c "UPDATE flyway_schema_history SET checksum = 1901482821 WHERE version = '6';"

if [ $? -ne 0 ]; then
    echo "❌ Erro ao atualizar checksum. Verifique os logs acima."
    exit 1
fi

echo "✅ Checksum atualizado. Verificando..."
echo ""

# Verificar se foi atualizado
heroku pg:psql --app "$APP_NAME" -c "SELECT version, description, checksum, installed_on, success FROM flyway_schema_history WHERE version = '6';"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Checksum corrigido com sucesso no Heroku!"
    echo ""
    echo "⚠️  IMPORTANTE: Reinicie o app para aplicar a correção:"
    echo "   heroku restart --app $APP_NAME"
else
    echo ""
    echo "⚠️  Aviso: Não foi possível verificar, mas o update foi executado."
    echo "   Execute manualmente: heroku pg:psql --app $APP_NAME"
fi

