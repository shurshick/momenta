# Momenta / Момент

Момент — социальное приложение для живых ежедневных моментов: задание дня, камера, лента, реакции, комментарии и профиль.

> Один момент. Все вместе.

Текущая стабильная версия: **v0.2.36**.

## Что уже есть

- Android-приложение на Kotlin + Jetpack Compose.
- FastAPI backend: auth, posts, feed, challenges, comments, reactions, reports.
- Админка для пользователей, постов, жалоб, настроек, аудита и версии сервера.
- Docker/TrueNAS deployment: API, worker, PostgreSQL, Redis, MinIO.
- APK и Docker image публикуются через GitHub Actions.

## Android

Основные экраны:

- **Момент дня** — ежедневное задание и лучший момент дня.
- **Мир сейчас** — лента постов, реальные пользователи в верхнем ряду, лайки, комментарии, жалобы, удаление своих постов.
- **Создать** — камера, галерея, эффекты, предпросмотр и публикация.
- **Профиль** — аватар, статистика, био, последние моменты.
- **Настройки** — API URL, информация о приложении, проверка и скачивание обновления.

Сборка APK:

```bash
cd android
./gradlew assembleProdDebug
./gradlew verifyInstallableProdApk
```

Готовый APK лежит в релизах GitHub:  
[Latest release](https://github.com/shurshick/momenta/releases/latest)

## Backend

Локальный запуск:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Проверки:

```bash
python -m compileall -q app tests
python -m pytest -q
```

## Docker / TrueNAS

Production image:

```yaml
image: ghcr.io/shurshick/momenta:v0.2.36
```

Документация по обновлению TrueNAS: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)

Для **v0.2.36** новых миграций БД нет.

## Релизы

- Release: [v0.2.36](https://github.com/shurshick/momenta/releases/tag/v0.2.36)
- Docker image: `ghcr.io/shurshick/momenta:v0.2.36`
- Android APK: `app-prod-debug.apk`

## Документы

- Android: [android/README.md](android/README.md)
- Android architecture: [android/docs/ANDROID_ARCHITECTURE.md](android/docs/ANDROID_ARCHITECTURE.md)
- TrueNAS deploy: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)
