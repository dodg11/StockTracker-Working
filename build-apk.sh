#!/bin/bash
# StockTracker - Сборка APK на Replit
# Этот скрипт автоматически установит Android SDK и соберёт APK

set -e

echo "╔══════════════════════════════════════════════╗"
echo "║   StockTracker - Сборка APK                  ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# Проверяем Java
echo "📋 Проверка Java..."
java -version 2>&1 || { echo "❌ Java не найден!"; exit 1; }
echo "✅ Java OK"
echo ""

# Устанавливаем Android SDK если нужно
SDK_DIR="$HOME/android-sdk"
if [ ! -d "$SDK_DIR" ]; then
    echo "📥 Установка Android SDK..."
    mkdir -p "$SDK_DIR/cmdline-tools"

    # Скачиваем command line tools
    cd /tmp
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
    unzip -q cmdline-tools.zip -d "$SDK_DIR/cmdline-tools"
    mv "$SDK_DIR/cmdline-tools/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
    rm cmdline-tools.zip

    echo "✅ Android SDK скачан"
    echo ""

    # Принимаем лицензии
    echo "📋 Принятие лицензий..."
    yes | "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true

    # Устанавливаем нужные компоненты
    echo "📥 Установка компонентов SDK..."
    "$SDK_DIR/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-35" "build-tools;35.0.0" > /dev/null 2>&1

    echo "✅ SDK установлен"
    echo ""
else
    echo "✅ Android SDK уже установлен"
    echo ""
fi

export ANDROID_HOME="$SDK_DIR"
export ANDROID_SDK_ROOT="$SDK_DIR"
export PATH="$PATH:$SDK_DIR/platform-tools:$SDK_DIR/build-tools/35.0.0"

# Создаём local.properties
echo "sdk.dir=$SDK_DIR" > local.properties

# Делаем gradlew исполняемым
chmod +x gradlew

# Собираем APK
echo "🔨 Сборка APK..."
echo "   Это может занять 5-10 минут..."
echo ""

./gradlew assembleDebug --stacktrace

if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "╔══════════════════════════════════════════════╗"
    echo "║   ✅ APK УСПЕШНО СОБРАН!                     ║"
    echo "╚══════════════════════════════════════════════╝"
    echo ""
    echo "📦 Файл: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "📥 Чтобы скачать:"
    echo "   1. Откройте папку app/build/outputs/apk/debug/"
    echo "   2. Нажмите на app-debug.apk"
    echo "   3. Нажмите Download"
    echo ""
    ls -lh app/build/outputs/apk/debug/app-debug.apk
else
    echo ""
    echo "❌ Ошибка сборки!"
    echo "Проверьте логи выше"
    exit 1
fi
