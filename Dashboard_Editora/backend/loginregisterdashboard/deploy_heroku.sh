#!/bin/bash

# Script para fazer deploy no Heroku
# App: dashboard-painel-autores-vl

APP_NAME="dashboard-painel-autores-vl"

echo "🚀 Iniciando deploy para Heroku..."
echo "App: $APP_NAME"
echo ""

# Verificar se está no diretório correto
if [ ! -f "pom.xml" ]; then
    echo "❌ Erro: pom.xml não encontrado. Execute este script na raiz do projeto."
    exit 1
fi

# Verificar se o Procfile existe
if [ ! -f "Procfile" ]; then
    echo "❌ Erro: Procfile não encontrado. Criando..."
    echo "web: java \$JAVA_OPTS -jar target/*.jar --server.port=\$PORT" > Procfile
fi

# Verificar se o remote heroku está configurado
if ! git remote | grep -q heroku; then
    echo "📡 Configurando remote do Heroku..."
    heroku git:remote --app "$APP_NAME"
fi

# Fazer commit se houver mudanças
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "📝 Fazendo commit das alterações..."
    git add .
    git commit -m "Deploy: Configuração Heroku com Procfile e porta"
fi

# Fazer build local (opcional, mas recomendado para verificar erros)
echo "🔨 Fazendo build do projeto..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Erro no build. Corrija os erros antes de fazer deploy."
    exit 1
fi

echo ""
echo "📤 Fazendo push para Heroku..."
echo "Isso pode levar alguns minutos..."
git push heroku master

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Deploy concluído com sucesso!"
    echo ""
    echo "Para verificar os logs:"
    echo "  heroku logs --tail --app $APP_NAME"
    echo ""
    echo "Para verificar o status:"
    echo "  heroku ps --app $APP_NAME"
else
    echo ""
    echo "❌ Erro no deploy. Verifique os logs acima."
    exit 1
fi

