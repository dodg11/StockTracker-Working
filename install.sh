#!/bin/bash
# StockTracker - автоматическая установка
# Запустите: bash install.sh

echo "📱 Создание StockTracker..."

# Создаём структуру
mkdir -p app/src/main/java/com/stocktracker/app
mkdir -p app/src/main/res/layout
mkdir -p app/src/main/res/values
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/mipmap-anydpi-v26
mkdir -p app/src/main/res/xml
mkdir -p .github/workflows
mkdir -p gradle/wrapper

echo "✅ Папки созданы"
echo "📋 Теперь скопируйте файлы из архива..."
