# 📱 StockTracker

Приложение для отслеживания акций с уведомлениями о критических изменениях цен.

## Функции

- 📊 Отслеживание купленных акций (цена, P/L, изменения)
- 👁 Список наблюдения для потенциальных покупок/продаж
- 🔔 Уведомления о критическом падении, стоп-лоссе, достижении целей
- ⚙️ Настраиваемый интервал мониторинга и пороги оповещений
- 🗄 Локальное хранение данных (SQLite)
- 🌙 Тёмная тема

## Сборка APK через GitHub Actions

### Автоматическая сборка (Debug)

1. Форкните или склонируйте репозиторий
2. Сделайте push в ветку `main`
3. Перейдите во вкладку **Actions** — сборка запустится автоматически
4. После завершения скачайте APK из **Artifacts**

### Подписанный Release

1. Создайте keystore локально:
```bash
keytool -genkey -v -keystore keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias stocktracker
```

2. Закодируйте в base64:
```bash
base64 keystore.jks > keystore_base64.txt
# Linux/Mac
# или
certutil -encode keystore.jks keystore_base64.txt
# Windows
```

3. Добавьте Secrets в GitHub (Settings → Secrets and variables → Actions):
   - `KEYSTORE_BASE64` — содержимое keystore_base64.txt
   - `KEYSTORE_PASSWORD` — пароль keystore
   - `KEY_ALIAS` — alias ключа
   - `KEY_PASSWORD` — пароль ключа

4. Создайте тег для релиза:
```bash
git tag v1.0.0
git push origin v1.0.0
```

5. APK появится в **Releases**

## Установка

1. Скачайте APK с GitHub Actions/Releases
2. Разрешите установку из неизвестных источников
3. Установите APK

## Использование

1. Добавьте акции через кнопку `+`
2. Укажите тикер, цену покупки, количество
3. Настройте стоп-лосс и целевые цены
4. Запустите мониторинг в настройках
5. Получайте уведомления!

## Технологии

- Java, Android SDK
- SQLite для хранения
- Yahoo Finance API для цен
- Foreground Service для фонового мониторинга
- Material Design 3

## Лицензия

MIT
