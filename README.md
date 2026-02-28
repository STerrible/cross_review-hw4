# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

На текущем этапе реализован базовый каркас сервиса `bot` с обработкой команд `/start`, `/help` и ответом на неизвестные команды.

Полезную для разработки проекта информацию вы можете найти в файле [HELP.md](./HELP.md).

FYI: как запустить бота?
Чтобы запустить бота, нужно прописать в PowerShell в корне проекта $env:BOT_TOKEN="ВАШ_ТОКЕН", после чего запустить командой .\mvnw.cmd -f bot\pom.xml spring-boot:run.
Обратите внимание, что JDK должен быть версии 25 и выше.

## Запуск приложения

Из корня репозитория:

```powershell
.\mvnw.cmd -f bot\pom.xml spring-boot:run
```

## Запуск проверок

Локальный запуск тестов только модуля `bot`:

```powershell
.\mvnw.cmd -pl bot -am test
```

Проверка форматирования/статики (как в CI):

```powershell
.\mvnw.cmd compile -am spotless:check modernizer:modernizer spotbugs:check pmd:check pmd:cpd-check
```

## Структура модулей

- `bot` — Telegram Bot API и обработка команд
- `scrapper` — заготовка сервиса мониторинга источников
- `ai-agent` — заготовка AI-сервиса

