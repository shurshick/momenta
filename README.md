# Momenta / Момент

Момент — социальное Android-приложение для живых ежедневных моментов: задание дня, камера, лента, реакции, комментарии и профиль.

> Один момент. Все вместе.

Текущая стабильная версия сервера: **v0.2.70**.

## Что уже есть

- Android-приложение на Kotlin + Jetpack Compose.
- FastAPI backend: auth, posts, feed, challenges, comments, reactions, reports.
- Админка для пользователей, постов, жалоб, настроек, аудита и версии сервера.
- Docker/TrueNAS deployment: API, worker, PostgreSQL, Redis, MinIO.
- APK и Docker image публикуются через GitHub Actions.
- Проверка обновлений Android идет через публичный backend endpoint `/api/v1/app/latest`.

## Android

Основные экраны:

- **Момент дня** — ежедневное задание и лучший момент дня.
- **Мир сейчас** — лента постов, лайки, комментарии, избранное, жалобы и удаление своих постов.
- **Создать** — камера, галерея, эффекты, предпросмотр и публикация.
- **Профиль** — аватар, статистика, свои и избранные моменты.
- **Настройки** — API URL, информация о приложении, проверка и скачивание обновления.

Сборка APK:

```bash
cd android
./gradlew assembleProdDebug
./gradlew verifyInstallableProdApk
```

Готовый APK лежит в релизах GitHub:
[Latest release](https://github.com/shurshick/momenta/releases/latest)

## Бренд

Канонический официальный знак с прозрачным фоном: [assets/brand/momenta-logo-official.png](assets/brand/momenta-logo-official.png).
Android использует его копию из `drawable-nodpi`; CI проверяет прозрачность и полное совпадение файлов.

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
image: ghcr.io/shurshick/momenta:v0.2.70
```

Документация по обновлению TrueNAS: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)

В **v0.2.70** кеши разделены по аккаунтам, публикации переживают сбои сети, профиль загружает свои и избранные моменты страницами, а контейнер API применяет Alembic до запуска.

## Обновления Android

Android больше не ходит напрямую в GitHub Releases API. Приложение проверяет:

```http
GET /api/v1/app/latest
```

Сервер отдает `version_name`, `version_code`, `apk_url`, `apk_sha256`, `mandatory` и release notes. Метаданные генерирует Android release workflow, сервер кеширует последний релиз с `android-update.json`.

## Релизы

- Release: [v0.2.70](https://github.com/shurshick/momenta/releases/tag/v0.2.70)
- Docker image: `ghcr.io/shurshick/momenta:v0.2.70`
- Android APK: актуальная версия приложения `0.2.70`

## Документы

- Android: [android/README.md](android/README.md)
- Android architecture: [android/docs/ANDROID_ARCHITECTURE.md](android/docs/ANDROID_ARCHITECTURE.md)
- TrueNAS deploy: [docs/DEPLOY_TRUENAS.md](docs/DEPLOY_TRUENAS.md)
- API: [docs/API.md](docs/API.md)
- Release style: [docs/RELEASE_STYLE.md](docs/RELEASE_STYLE.md)
- Changelog: [CHANGELOG.md](CHANGELOG.md)

